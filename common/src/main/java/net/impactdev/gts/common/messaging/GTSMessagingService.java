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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.TreeMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.event.factory.GTSEventFactory;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.communication.message.errors.ErrorCode;
import net.impactdev.gts.api.communication.message.errors.ErrorCodes;
import net.impactdev.gts.api.communication.message.exceptions.MessagingException;
import net.impactdev.gts.api.communication.message.type.MessageType;
import net.impactdev.gts.api.communication.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.api.communication.message.type.deliveries.ClaimDelivery;
import net.impactdev.gts.api.communication.message.type.listings.ClaimMessage;
import net.impactdev.gts.api.communication.message.type.utility.PingMessage;
import net.impactdev.gts.api.util.TriConsumer;
import net.impactdev.gts.common.messaging.messages.admin.ForceDeleteMessageImpl;
import net.impactdev.gts.common.messaging.messages.deliveries.ClaimDeliveryImpl;
import net.impactdev.gts.common.messaging.messages.listings.ClaimMessageImpl;
import net.impactdev.gts.common.messaging.messages.listings.PublishListingMessageImpl;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionCancelMessage;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionBidMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.purchase.BINPurchaseMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal.BINRemoveMessage;
import net.impactdev.gts.common.messaging.messages.utility.PingPongMessage;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.common.utils.future.CompletableFutureManager;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.gts.api.communication.IncomingMessageConsumer;
import net.impactdev.gts.api.communication.Messenger;
import net.impactdev.gts.api.communication.MessengerProvider;
import net.impactdev.gts.api.communication.message.OutgoingMessage;
import net.impactdev.gts.api.communication.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.communication.message.type.listings.BuyItNowMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
        PingMessage.Ping ping = new PingPongMessage.Ping(this.generatePingID());
        return new MessageProcessor<PingMessage.Pong, PingMessage.Ping>(
                "Ping Request",
                ping,
                () -> this.await(ping),
                error -> new PingPongMessage.Pong(
                        UUID.randomUUID(),
                        ping.getID(),
                        false,
                        error
                )
        ).process();
    }

    @Override
    public CompletableFuture<Void> sendPublishNotice(UUID listing, UUID actor, boolean auction) {
        return CompletableFutureManager.makeFuture(() -> {
            PublishListingMessageImpl message = new PublishListingMessageImpl(this.generatePingID(), listing, actor, auction);
            GTSPlugin.instance().messagingService().getMessenger().sendOutgoingMessage(message);
        });
    }

    @Override
    public CompletableFuture<AuctionMessage.Bid.Response> publishBid(UUID listing, UUID actor, double bid) {
        AuctionMessage.Bid.Request request = new AuctionBidMessage.Request(this.generatePingID(), listing, actor, bid);
        return new MessageProcessor<AuctionMessage.Bid.Response, AuctionMessage.Bid.Request>(
                "Bid Publishing Request",
                request,
                () -> this.await(request),
                error -> new AuctionBidMessage.Response(
                        UUID.randomUUID(),
                        request.getID(),
                        request.getAuctionID(),
                        request.getActor(),
                        request.getAmountBid(),
                        false,
                        false,
                        Listing.SERVER_ID,
                        TreeMultimap.create(),
                        error
                )
        ).process();
    }

    @Override
    public CompletableFuture<AuctionMessage.Cancel.Response> requestAuctionCancellation(UUID listing, UUID actor) {
        AuctionMessage.Cancel.Request request = new AuctionCancelMessage.Request(this.generatePingID(), listing, actor);
        return new MessageProcessor<AuctionMessage.Cancel.Response, AuctionMessage.Cancel.Request>(
                "Auction Cancellation Request",
                request,
                () -> this.await(request),
                error -> new AuctionCancelMessage.Response(
                        UUID.randomUUID(),
                        request.getID(),
                        null,
                        request.getAuctionID(),
                        request.getActor(),
                        ImmutableList.of(),
                        false,
                        error
                )
        ).process();
    }

    @Override
    public CompletableFuture<ClaimMessage.Response> requestClaim(UUID listing, UUID actor, @Nullable UUID receiver, boolean auction) {
        ClaimMessageImpl.ClaimRequestImpl request = new ClaimMessageImpl.ClaimRequestImpl(this.generatePingID(), listing, actor, receiver, auction);
        return new MessageProcessor<ClaimMessage.Response, ClaimMessage.Request>(
                "Claim Request",
                request,
                () -> this.await(request),
                error -> ClaimMessageImpl.ClaimResponseImpl.builder()
                        .id(UUID.randomUUID())
                        .request(request.getID())
                        .listing(request.getListingID())
                        .actor(request.getActor())
                        .receiver(request.getReceiver().orElse(null))
                        .error(error)
                        .build()
        ).process();
    }

    @Override
    public CompletableFuture<BuyItNowMessage.Purchase.Response> requestBINPurchase(UUID listing, UUID actor, Object source) {
        BuyItNowMessage.Purchase.Request request = new BINPurchaseMessage.Request(this.generatePingID(), listing, actor);
        return new MessageProcessor<BuyItNowMessage.Purchase.Response, BuyItNowMessage.Purchase.Request>(
                "BIN Purchase Request",
                request,
                () -> this.await(request),
                error -> new BINPurchaseMessage.Response(
                        UUID.randomUUID(),
                        request.getID(),
                        listing, actor,
                        Listing.SERVER_ID,
                        false,
                        error
                )
        ).process();
    }

    @Override
    public CompletableFuture<BuyItNowMessage.Remove.Response> requestBINRemoveRequest(UUID listing, UUID actor, @Nullable UUID receiver, boolean shouldReceive) {
        BuyItNowMessage.Remove.Request request = new BINRemoveMessage.Request(this.generatePingID(), listing, actor, receiver, shouldReceive);

        return new MessageProcessor<BuyItNowMessage.Remove.Response, BuyItNowMessage.Remove.Request>(
                "BIN Removal Request",
                request,
                () -> this.await(request),
                error -> new BINRemoveMessage.Response(
                        UUID.randomUUID(),
                        request.getID(),
                        listing,
                        actor,
                        receiver,
                        shouldReceive,
                        false,
                        error
                )
        ).process();
    }

    @Override
    public CompletableFuture<ForceDeleteMessage.Response> requestForcedDeletion(UUID listing, UUID actor, boolean give) {
        ForceDeleteMessage.Request request = new ForceDeleteMessageImpl.ForceDeleteRequest(this.generatePingID(), listing, actor, give);

        return new MessageProcessor<ForceDeleteMessage.Response, ForceDeleteMessage.Request>(
                "Admin - Forced Deletion",
                request,
                () -> this.await(request),
                error -> ForceDeleteMessage.Response.builder()
                    .request(request.getID())
                    .listing(request.getListingID())
                    .actor(request.getActor())
                    .data(null)
                    .successful(false)
                    .error(error)
                    .build()
        ).process();
    }

    @Override
    public CompletableFuture<ClaimDelivery.Response> requestDeliveryClaim(UUID delivery, UUID actor) {
        ClaimDelivery.Request request = new ClaimDeliveryImpl.ClaimDeliveryRequestImpl(this.generatePingID(), delivery, actor);

        return new MessageProcessor<ClaimDelivery.Response, ClaimDelivery.Request>(
                "Delivery - Claim Request",
                request,
                () -> this.await(request),
                error -> ClaimDeliveryImpl.ClaimDeliveryResponseImpl.builder()
                        .request(request.getID())
                        .delivery(request.getDeliveryID())
                        .actor(request.getActor())
                        .error(error)
                        .build()
        ).process();
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

    private static class MessageProcessor<T extends MessageType.Response, E extends MessageType.Request<T>> {

        private final String name;

        private final E request;
        private final Supplier<T> supplier;
        private final Function<ErrorCode, T> error;

        MessageProcessor(String name, E request, Supplier<T> supplier, Function<ErrorCode, T> error) {
            this.name = name;
            this.request = request;
            this.supplier = supplier;
            this.error = error;
        }

        CompletableFuture<T> process() {
            PrettyPrinter debugger = new PrettyPrinter(53).add(this.name).center().hr();
            final AtomicLong start = new AtomicLong();

            return CompletableFutureManager.makeFuture(() -> {
                start.set(System.nanoTime());

                T response = this.supplier.get();
                this.populate(debugger, request, response);

                return response;
            }).applyToEither(
                    CompletableFutureManager.timeoutAfter(5, TimeUnit.SECONDS),
                    response -> {
                        debugger.log(GTSPlugin.instance().logger(), PrettyPrinter.Level.DEBUG);
                        return response;
                    }
            ).exceptionally(completion -> {
                Throwable e = completion.getCause();

                ErrorCode error;
                if(e instanceof MessagingException) {
                    error = ((MessagingException) e).getError();
                } else {
                    error = ErrorCodes.FATAL_ERROR;
                    ExceptionWriter.write(e);
                }

                T response = this.error.apply(error);

                long end = System.nanoTime();
                response.setResponseTime(TimeUnit.SECONDS.toMillis(
                        error.equals(ErrorCodes.REQUEST_TIMED_OUT) ? 5 :
                                (end - start.get()) / 1_000_000_000
                ));

                this.populate(debugger, request, response);
                debugger.log(GTSPlugin.instance().logger(), PrettyPrinter.Level.DEBUG);

                return response;
            });
        }

        private void populate(PrettyPrinter printer, MessageType.Request<?> request, MessageType.Response response) {
            printer.newline()
                    .add("Request Information:")
                    .hr('-')
                    .add(request)
                    .newline()
                    .hr('=')
                    .newline()
                    .add("Response Information:")
                    .hr('-')
                    .add(response)
                    .newline()
                    .consume(response::finalizeReport)
                    .newline();
        }

    }
}
