/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package net.impactdev.gts.common.messaging;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.messaging.message.exceptions.MessagingException;
import net.impactdev.gts.api.messaging.message.type.MessageType;
import net.impactdev.gts.api.messaging.message.type.utility.PingMessage;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionClaimMessage;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionBidMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.purchase.BINPurchaseMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal.BINRemoveMessage;
import net.impactdev.gts.common.messaging.messages.utility.PingPongMessage;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.common.utils.future.CompletableFutureManager;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.gts.api.events.PingEvent;
import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.api.messaging.Messenger;
import net.impactdev.gts.api.messaging.MessengerProvider;
import net.impactdev.gts.api.messaging.message.OutgoingMessage;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class GTSMessagingService implements InternalMessagingService {

    private final GTSPlugin plugin;

    private final MessengerProvider messengerProvider;
    private final Messenger messenger;

    private Map<String, BiFunction<JsonElement, UUID, ? extends OutgoingMessage>> decoders = Maps.newHashMap();

    public static final Gson NORMAL = new GsonBuilder().disableHtmlEscaping().create();

    public GTSMessagingService(GTSPlugin plugin, MessengerProvider messengerProvider, IncomingMessageConsumer consumer) {
        this.plugin = plugin;

        this.messengerProvider = messengerProvider;
        this.messenger = messengerProvider.obtain(consumer);
        Objects.requireNonNull(this.messenger, "messenger");
    }

    @Override
    public String getName() {
        return this.messengerProvider.getName();
    }

    @Override
    public Messenger getMessenger() {
        return this.messenger;
    }

    @Override
    public MessengerProvider getMessengerProvider() {
        return this.messengerProvider;
    }

    @Override
    public void close() {
        this.messenger.close();
    }

    @Override
    public <T extends OutgoingMessage> void registerDecoder(String type, BiFunction<JsonElement, UUID, T> decoder) {
        this.decoders.put(type, decoder);
    }

    @Override
    public BiFunction<JsonElement, UUID, ? extends OutgoingMessage> getDecoder(String type) {
        return this.decoders.get(type);
    }

    @Override
    public UUID generatePingID() {
        UUID uuid = UUID.randomUUID();
        this.messenger.getMessageConsumer().cacheReceivedID(uuid);
        return uuid;
    }

    @Override
    public CompletableFuture<PingMessage.Pong> sendPing() {
        PrettyPrinter debugger = new PrettyPrinter(53).add("Ping/Pong Status").center().hr();
        final AtomicReference<PingMessage.Ping> reference = new AtomicReference<>();

        return CompletableFutureManager.makeFuture(() -> {
            PingMessage.Ping ping = new PingPongMessage.Ping(this.generatePingID());
            reference.set(ping);

            Impactor.getInstance().getEventBus().postAsync(PingEvent.class, reference, Instant.now());

            PingMessage.Pong response = this.await(ping);
            this.populate(debugger, reference.get(), response);

            return response;
        }, Impactor.getInstance().getScheduler().async()).applyToEither(
                this.timeoutAfter(5, TimeUnit.SECONDS),
                pong -> {
                    debugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG);
                    return pong;
                }
        ).exceptionally(e -> {
            ErrorCode error;
            if(e instanceof MessagingException) {
                error = ((MessagingException) e).getError();
            } else {
                error = ErrorCodes.FATAL_ERROR;
                ExceptionWriter.write(e);
            }

            PingMessage.Ping request = reference.get();
            PingMessage.Pong response = new PingPongMessage.Pong(UUID.randomUUID(), request.getID(), false, error);

            this.populate(debugger, request, response);
            debugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG);

            return response;
        });
    }

    @Override
    public CompletableFuture<AuctionMessage.Bid.Response> publishBid(UUID listing, UUID actor, double bid) {
        PrettyPrinter debugger = new PrettyPrinter(53).add("Bid Publishing Request").center().hr();
        final AtomicReference<AuctionMessage.Bid.Request> reference = new AtomicReference<>();

        return CompletableFutureManager.makeFuture(() -> {
            AuctionMessage.Bid.Request request = new AuctionBidMessage.Request(this.generatePingID(), listing, actor, bid);
            reference.set(request);

            AuctionMessage.Bid.Response response = this.await(request);
            this.populate(debugger, request, response);

            return response;
        }, Impactor.getInstance().getScheduler().async()).applyToEither(
                this.timeoutAfter(5, TimeUnit.SECONDS),
                response -> {
                    debugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG);
                    return response;
                }
        ).exceptionally(e -> {
            ErrorCode error;
            if(e instanceof MessagingException) {
                error = ((MessagingException) e).getError();
            } else {
                error = ErrorCodes.FATAL_ERROR;
                ExceptionWriter.write(e);
            }

            AuctionMessage.Bid.Request request = reference.get();
            AuctionMessage.Bid.Response response = new AuctionBidMessage.Response(
                    UUID.randomUUID(),
                    request.getID(),
                    listing,
                    actor,
                    bid,
                    false,
                    Listing.SERVER_ID,
                    Maps.newHashMap(),
                    error
            );

            this.populate(debugger, request, response);
            debugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG);

            return response;
        });
    }

    @Override
    public void requestAuctionCancellation(UUID listing, UUID actor) {

    }

    @Override
    public void requestAuctionClaim(UUID listing, UUID actor, boolean isLister, Consumer<AuctionMessage.Claim.Response> callback) {
        AuctionClaimMessage.ClaimRequest request = new AuctionClaimMessage.ClaimRequest(this.generatePingID(), listing, actor, isLister);
        this.getMessenger().getMessageConsumer().registerRequest(request.getID(), callback);
        this.messenger.sendOutgoingMessage(request);
    }

    @Override
    public CompletableFuture<BuyItNowMessage.Purchase.Response> requestBINPurchase(UUID listing, UUID actor, Object source) {
        PrettyPrinter debugger = new PrettyPrinter(53).add("Bid Publishing Request").center().hr();
        final AtomicReference<BuyItNowMessage.Purchase.Request> reference = new AtomicReference<>();

        return CompletableFutureManager.makeFuture(() -> {
            BuyItNowMessage.Purchase.Request request = new BINPurchaseMessage.Request(this.generatePingID(), listing, actor);
            reference.set(request);

            BuyItNowMessage.Purchase.Response response = this.await(request);
            this.populate(debugger, request, response);

            return response;
        }, Impactor.getInstance().getScheduler().async()).applyToEither(
                this.timeoutAfter(5, TimeUnit.SECONDS),
                response -> {
                    debugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG);
                    return response;
                }
        ).exceptionally(e -> {
            ErrorCode error;
            if(e instanceof MessagingException) {
                error = ((MessagingException) e).getError();
            } else {
                error = ErrorCodes.FATAL_ERROR;
                ExceptionWriter.write(e);
            }

            BuyItNowMessage.Purchase.Request request = reference.get();
            BuyItNowMessage.Purchase.Response response = new BINPurchaseMessage.Response(
                    UUID.randomUUID(), request.getID(), listing, actor, false, error
            );

            this.populate(debugger, request, response);
            debugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG);

            return response;
        });
    }

    @Override
    public CompletableFuture<BuyItNowMessage.Remove.Response> requestBINRemoveRequest(UUID listing, UUID actor, @Nullable UUID receiver, boolean shouldReceive) {
        PrettyPrinter debugger = new PrettyPrinter(53).add("Buy It Now Removal Request").center().hr();
        final AtomicReference<BuyItNowMessage.Remove.Request> reference = new AtomicReference<>();

        return CompletableFutureManager.makeFuture(() -> {
            BuyItNowMessage.Remove.Request request = new BINRemoveMessage.Request(this.generatePingID(), listing, actor, receiver, shouldReceive);
            reference.set(request);

            BuyItNowMessage.Remove.Response response = this.await(request);
            this.populate(debugger, request, response);

            return response;
        }).applyToEither(
            this.timeoutAfter(5, TimeUnit.SECONDS),
            response -> {
                debugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG);
                return response;
            }
        ).exceptionally(e -> {
            ErrorCode error;
            if(e instanceof MessagingException) {
                error = ((MessagingException) e).getError();
            } else {
                error = ErrorCodes.FATAL_ERROR;
                ExceptionWriter.write(e);
            }

            BuyItNowMessage.Remove.Request request = reference.get();
            BuyItNowMessage.Remove.Response response = new BINRemoveMessage.Response(
                    UUID.randomUUID(), request.getID(), listing, actor, receiver, shouldReceive, false, error
            );

            this.populate(debugger, request, response);
            debugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG);

            return response;
        });
    }

    public static String encodeMessageAsString(String type, UUID id, @Nullable JsonElement content) {
        JsonObject json = new JObject()
                .add("id", id.toString())
                .add("type", type)
                .consume(o -> {
                    if (content != null) {
                        o.add("content", content);
                    }
                })
                .toJson();

        return NORMAL.toJson(json);
    }

    private PrettyPrinter populate(PrettyPrinter printer, MessageType.Request<?> request, MessageType.Response response) {
        return printer.add()
                .add("Request Information:")
                .hr('-')
                .add(request)
                .add()
                .hr('=')
                .add()
                .add()
                .add("Response Information:")
                .hr('-')
                .add(response)
                .consume(response::finalizeReport)
                .add();
    }

}
