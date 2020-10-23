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
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionClaimMessage;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.BidMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.purchase.BINPurchaseRequestMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal.BuyItNowRemoveRequestMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal.BuyItNowRemoveResponseMessage;
import net.impactdev.gts.common.messaging.messages.utility.GTSPongMessage;
import net.impactdev.gts.common.utils.debug.Debugger;
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
import net.impactdev.gts.common.messaging.messages.utility.GTSPingMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
    public CompletableFuture<GTSPongMessage> sendPing() {
        return CompletableFutureManager.makeFuture(() -> {
            UUID request = this.generatePingID();
            this.plugin.getPluginLogger().info("[Messaging] Sending ping with id: " + request);
            Impactor.getInstance().getEventBus().postAsync(PingEvent.class, request, Instant.now());

            long start = System.nanoTime();
            GTSPongMessage response = this.await(request, new GTSPingMessage(request));
            long finish = System.nanoTime();
            response.setResponseTime((finish - start) / 1000000);

            this.plugin.getPluginLogger().info("[Messaging] Pong received, took: " + response.getResponseTime() + " ms");
            return response;
        }, Impactor.getInstance().getScheduler().async()).applyToEither(
                this.timeoutAfter(5, TimeUnit.SECONDS),
                pong -> pong
        ).exceptionally(e -> {
            this.plugin.getPluginLogger().error("[Messaging] Failed to receive pong response in time, ensure connection is setup correctly!");
            return null;
        });
    }

    @Override
    public void publishAuctionListing(UUID auction, UUID actor, String broadcast) {

    }

    @Override
    public CompletableFuture<AuctionMessage.Bid.Response> publishBid(UUID listing, UUID actor, double bid) {
        return CompletableFutureManager.makeFuture(() -> {
            BidMessage request = new BidMessage(this.generatePingID(), listing, actor, bid);
            return this.<AuctionMessage.Bid.Request, AuctionMessage.Bid.Response>await(request.getID(), request);
        }, Impactor.getInstance().getScheduler().async()).applyToEither(
                this.timeoutAfter(5, TimeUnit.SECONDS),
                response -> response
        ).exceptionally(e -> {
            this.plugin.getPluginLogger().error("[Messaging] Failed to receive bid response in time, ensure connection is setup correctly!");
            return null;
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
        PrettyPrinter debugger = Debugger.create();
        debugger.add("Request Information:")
                .kv("Listing ID", listing);

        return CompletableFutureManager.makeFuture(() -> {
            BINPurchaseRequestMessage request = new BINPurchaseRequestMessage(this.generatePingID(), listing, actor);
            return this.<BuyItNowMessage.Purchase.Request, BuyItNowMessage.Purchase.Response>await(request.getID(), request);
        }, Impactor.getInstance().getScheduler().async()).applyToEither(
                this.timeoutAfter(5, TimeUnit.SECONDS),
                response -> response
        ).thenApply(response -> {
            this.plugin.getPluginLogger().debug("[Messaging] BIN Purchase Response Received");
            return response;
        }).exceptionally(e -> {
            this.plugin.getPluginLogger().error("[Messaging] Failed to receive BIN Purchase response in time, ensure connection is setup correctly!");
            return null;
        });
    }

    @Override
    public CompletableFuture<BuyItNowMessage.Remove.Response> requestBINRemoveRequest(UUID listing, UUID actor, @Nullable UUID receiver, boolean shouldReceive) {
        PrettyPrinter debugger = new PrettyPrinter(53).add("Buy It Now Removal Request").center().hr();
        final AtomicReference<BuyItNowMessage.Remove.Request> reference = new AtomicReference<>();

        return CompletableFutureManager.makeFuture(() -> {
            long start = System.nanoTime();
            BuyItNowMessage.Remove.Request request = new BuyItNowRemoveRequestMessage(this.generatePingID(), listing, actor, receiver, shouldReceive);
            reference.set(request);

            BuyItNowMessage.Remove.Response response = this.await(request.getID(), request);
            long finish = System.nanoTime();
            response = BuyItNowRemoveResponseMessage.builder()
                    .from((BuyItNowRemoveResponseMessage) response)
                    .responseTime((finish - start) / 1000000)
                    .build();

            debugger.add()
                    .add(request)
                    .add()
                    .hr('=')
                    .add()
                    .add(response)
                    .add();
            return response;
        }).applyToEither(
            this.timeoutAfter(5, TimeUnit.SECONDS),
            response -> {
                debugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG);
                return response;
            }
        ).exceptionally(e -> {
            BuyItNowMessage.Remove.Response response = null;

            BuyItNowRemoveResponseMessage.ResponseBuilder builder = BuyItNowRemoveResponseMessage.builder()
                    .id(Listing.SERVER_ID)
                    .request(reference.get().getID())
                    .listing(listing)
                    .actor(actor)
                    .receiver(receiver)
                    .shouldReceive(shouldReceive)
                    .successful(false);
            if(e instanceof TimeoutException) {
                response = builder.error(ErrorCodes.REQUEST_TIMED_OUT)
                        .responseTime(TimeUnit.SECONDS.toMillis(5))
                        .build();
                debugger.add().add(response);
            } else {
                debugger.add()
                        .add("Encountered an unexpected exception, see below:")
                        .add(e);

                if(!GTSPlugin.getInstance().inDebugMode()) {
                    ExceptionWriter.write(e);
                }
            }
            debugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG);
            return response == null ? builder.build() : response;
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

}
