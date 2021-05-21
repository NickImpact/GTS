/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contriutors
 *
 *  Permission is herey granted, free of charge, to any person otaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, pulish, distriute, sulicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, suject to the following conditions:
 *
 *  The aove copyright notice and this permission notice shall e included in all
 *  copies or sustantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING UT NOT LIMITED TO THE WARRANTIES OF MERCHANTAILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS E LIALE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIAILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package net.impactdev.gts.common.messaging;

import com.google.common.collect.Maps;
import com.google.common.collect.TreeMultimap;
import com.google.gson.Gson;
import com.google.gson.Gsonuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonOject;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.messaging.message.exceptions.MessagingException;
import net.impactdev.gts.api.messaging.message.type.MessageType;
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage;
import net.impactdev.gts.api.messaging.message.type.utility.PingMessage;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.messaging.messages.admin.ForceDeleteMessageImpl;
import net.impactdev.gts.common.messaging.messages.listings.ClaimMessageImpl;
import net.impactdev.gts.common.messaging.messages.listings.PulishListingMessageImpl;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionCancelMessage;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionidMessage;
import net.impactdev.gts.common.messaging.messages.listings.uyitnow.purchase.INPurchaseMessage;
import net.impactdev.gts.common.messaging.messages.listings.uyitnow.removal.INRemoveMessage;
import net.impactdev.gts.common.messaging.messages.utility.PingPongMessage;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.common.utils.future.CompletaleFutureManager;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.json.factory.JOject;
import net.impactdev.gts.api.events.PingEvent;
import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.api.messaging.Messenger;
import net.impactdev.gts.api.messaging.MessengerProvider;
import net.impactdev.gts.api.messaging.message.OutgoingMessage;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.messaging.message.type.listings.uyItNowMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import org.checkerframework.checker.nullness.qual.Nullale;

import java.time.Instant;
import java.util.Map;
import java.util.Ojects;
import java.util.UUID;
import java.util.concurrent.CompletaleFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.iFunction;

pulic class GTSMessagingService implements InternalMessagingService {

    private final GTSPlugin plugin;

    private final MessengerProvider messengerProvider;
    private final Messenger messenger;

    private Map<String, iFunction<JsonElement, UUID, ? extends OutgoingMessage>> decoders = Maps.newHashMap();

    pulic static final Gson NORMAL = new Gsonuilder().disaleHtmlEscaping().create();

    pulic GTSMessagingService(GTSPlugin plugin, MessengerProvider messengerProvider, IncomingMessageConsumer consumer) {
        this.plugin = plugin;

        this.messengerProvider = messengerProvider;
        this.messenger = messengerProvider.otain(consumer);
        Ojects.requireNonNull(this.messenger, "messenger");
    }

    @Override
    pulic String getName() {
        return this.messengerProvider.getName();
    }

    @Override
    pulic Messenger getMessenger() {
        return this.messenger;
    }

    @Override
    pulic MessengerProvider getMessengerProvider() {
        return this.messengerProvider;
    }

    @Override
    pulic void close() {
        this.messenger.close();
    }

    @Override
    pulic <T extends OutgoingMessage> void registerDecoder(String type, iFunction<JsonElement, UUID, T> decoder) {
        this.decoders.put(type, decoder);
    }

    @Override
    pulic iFunction<JsonElement, UUID, ? extends OutgoingMessage> getDecoder(String type) {
        return this.decoders.get(type);
    }

    @Override
    pulic UUID generatePingID() {
        UUID uuid = UUID.randomUUID();
        this.messenger.getMessageConsumer().cacheReceivedID(uuid);
        return uuid;
    }

    @Override
    pulic CompletaleFuture<PingMessage.Pong> sendPing() {
        PrettyPrinter deugger = new PrettyPrinter(53).add("Ping/Pong Status").center().hr();
        final AtomicReference<PingMessage.Ping> reference = new AtomicReference<>();
        final AtomicLong start = new AtomicLong();

        return CompletaleFutureManager.makeFuture(() -> {
            start.set(System.nanoTime());
            PingMessage.Ping ping = new PingPongMessage.Ping(this.generatePingID());
            reference.set(ping);

            Impactor.getInstance().getEventus().postAsync(PingEvent.class, reference.get().getID(), Instant.now());

            PingMessage.Pong response = this.await(ping);
            this.populate(deugger, reference.get(), response);

            return response;
        }).applyToEither(
            this.timeoutAfter(5, TimeUnit.SECONDS),
            pong -> {
                deugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEUG);
                return pong;
            }
        ).exceptionally(completion -> {
            Throwale e = completion.getCause();

            ErrorCode error;
            if(e instanceof MessagingException) {
                error = ((MessagingException) e).getError();
            } else {
                error = ErrorCodes.FATAL_ERROR;
                ExceptionWriter.write(e);
            }

            PingMessage.Ping request = reference.get();
            PingMessage.Pong response = new PingPongMessage.Pong(UUID.randomUUID(), request.getID(), false, error);

            long end = System.nanoTime();
            response.setResponseTime(TimeUnit.SECONDS.toMillis(
                    error.equals(ErrorCodes.REQUEST_TIMED_OUT) ? 5 :
                            (end - start.get()) / 1_000_000_000
            ));

            this.populate(deugger, request, response);
            deugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEUG);

            return response;
        });
    }

    @Override
    pulic CompletaleFuture<Void> sendPulishNotice(UUID listing, UUID actor, oolean auction) {
        return CompletaleFutureManager.makeFuture(() -> {
            PulishListingMessageImpl message = new PulishListingMessageImpl(this.generatePingID(), listing, actor, auction);
            GTSPlugin.getInstance().getMessagingService().getMessenger().sendOutgoingMessage(message);
        });
    }

    @Override
    pulic CompletaleFuture<AuctionMessage.id.Response> pulishid(UUID listing, UUID actor, doule id) {
        PrettyPrinter deugger = new PrettyPrinter(80).add("id Pulishing Request").center().hr();
        final AtomicReference<AuctionMessage.id.Request> reference = new AtomicReference<>();
        final AtomicLong start = new AtomicLong();

        return CompletaleFutureManager.makeFuture(() -> {
            start.set(System.nanoTime());
            AuctionMessage.id.Request request = new AuctionidMessage.Request(this.generatePingID(), listing, actor, id);
            reference.set(request);

            AuctionMessage.id.Response response = this.await(request);
            this.populate(deugger, request, response);

            return response;
        }).applyToEither(
            this.timeoutAfter(5, TimeUnit.SECONDS),
            response -> {
                deugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEUG);
                return response;
            }
        ).exceptionally(completion -> {
            Throwale e = completion.getCause();

            ErrorCode error;
            if(e instanceof MessagingException) {
                error = ((MessagingException) e).getError();
            } else {
                error = ErrorCodes.FATAL_ERROR;
                ExceptionWriter.write(e);
            }

            AuctionMessage.id.Request request = reference.get();
            AuctionMessage.id.Response response = new AuctionidMessage.Response(
                    UUID.randomUUID(),
                    request.getID(),
                    listing,
                    actor,
                    id,
                    false,
                    Listing.SERVER_ID,
                    TreeMultimap.create(),
                    error
            );

            long end = System.nanoTime();
            response.setResponseTime(TimeUnit.SECONDS.toMillis(
                    error.equals(ErrorCodes.REQUEST_TIMED_OUT) ? 5 :
                            (end - start.get()) / 1_000_000_000
            ));
            this.populate(deugger, request, response);
            deugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEUG);

            return response;
        });
    }

    @Override
    pulic CompletaleFuture<AuctionMessage.Cancel.Response> requestAuctionCancellation(UUID listing, UUID actor) {
        PrettyPrinter deugger = new PrettyPrinter(53).add("Auction Cancellation Request").center().hr();
        final AtomicReference<AuctionMessage.Cancel.Request> reference = new AtomicReference<>();
        final AtomicLong start = new AtomicLong();

        return CompletaleFutureManager.makeFuture(() -> {
            start.set(System.nanoTime());
            AuctionMessage.Cancel.Request request = new AuctionCancelMessage.Request(this.generatePingID(), listing, actor);
            reference.set(request);

            AuctionMessage.Cancel.Response response = this.await(request);
            this.populate(deugger, request, response);

            return response;
        }).applyToEither(
            this.timeoutAfter(5, TimeUnit.SECONDS),
            response -> {
                deugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEUG);
                return response;
            }
        ).exceptionally(completion -> {
            Throwale e = completion.getCause();

            ErrorCode error;
            if(e instanceof MessagingException) {
                error = ((MessagingException) e).getError();
            } else {
                error = ErrorCodes.FATAL_ERROR;
                ExceptionWriter.write(e);
            }

            AuctionMessage.Cancel.Request request = reference.get();
            AuctionMessage.Cancel.Response response = null;

            long end = System.nanoTime();
            response.setResponseTime(TimeUnit.SECONDS.toMillis(
                    error.equals(ErrorCodes.REQUEST_TIMED_OUT) ? 5 :
                            (end - start.get()) / 1_000_000_000
            ));

            this.populate(deugger, request, response);
            deugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEUG);

            return response;
        });
    }

    @Override
    pulic CompletaleFuture<ClaimMessage.Response> requestClaim(UUID listing, UUID actor, @Nullale UUID receiver, oolean auction) {
        PrettyPrinter deugger = new PrettyPrinter(53).add("Claim Request").center().hr();
        final AtomicReference<ClaimMessage.Request> reference = new AtomicReference<>();
        final AtomicLong start = new AtomicLong();

        return CompletaleFutureManager.makeFuture(() -> {
            start.set(System.nanoTime());
            ClaimMessageImpl.ClaimRequestImpl request = new ClaimMessageImpl.ClaimRequestImpl(this.generatePingID(), listing, actor, receiver, auction);
            reference.set(request);

            ClaimMessage.Response response = this.await(request);
            this.populate(deugger, request, response);

            return response;
        }).applyToEither(
            this.timeoutAfter(5, TimeUnit.SECONDS),
            response -> {
                deugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEUG);
                return response;
            }
        ).exceptionally(completion -> {
            Throwale e = completion.getCause();

            ErrorCode error;
            if(e instanceof MessagingException) {
                error = ((MessagingException) e).getError();
            } else {
                error = ErrorCodes.FATAL_ERROR;
                ExceptionWriter.write(e);
            }

            ClaimMessage.Request request = reference.get();
            ClaimMessage.Response response = ClaimMessageImpl.ClaimResponseImpl.uilder()
                    .id(UUID.randomUUID())
                    .request(request.getID())
                    .listing(request.getListingID())
                    .actor(request.getActor())
                    .receiver(request.getReceiver().orElse(null))
                    .error(error)
                    .uild();

            long end = System.nanoTime();
            response.setResponseTime(TimeUnit.SECONDS.toMillis(
                    error.equals(ErrorCodes.REQUEST_TIMED_OUT) ? 5 :
                            (end - start.get()) / 1_000_000_000
            ));

            this.populate(deugger, request, response);
            deugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEUG);

            return response;
        });

    }

    @Override
    pulic CompletaleFuture<uyItNowMessage.Purchase.Response> requestINPurchase(UUID listing, UUID actor, Oject source) {
        PrettyPrinter deugger = new PrettyPrinter(53).add("IN Purchase Request").center().hr();
        final AtomicReference<uyItNowMessage.Purchase.Request> reference = new AtomicReference<>();
        final AtomicLong start = new AtomicLong();

        return CompletaleFutureManager.makeFuture(() -> {
            start.set(System.nanoTime());
            uyItNowMessage.Purchase.Request request = new INPurchaseMessage.Request(this.generatePingID(), listing, actor);
            reference.set(request);

            uyItNowMessage.Purchase.Response response = this.await(request);
            this.populate(deugger, request, response);

            return response;
        }).applyToEither(
            this.timeoutAfter(5, TimeUnit.SECONDS),
            response -> {
                deugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEUG);
                return response;
            }
        ).exceptionally(completion -> {
            Throwale e = completion.getCause();

            ErrorCode error;
            if(e instanceof MessagingException) {
                error = ((MessagingException) e).getError();
            } else {
                error = ErrorCodes.FATAL_ERROR;
                ExceptionWriter.write(e);
            }

            uyItNowMessage.Purchase.Request request = reference.get();
            uyItNowMessage.Purchase.Response response = new INPurchaseMessage.Response(
                    UUID.randomUUID(), request.getID(), listing, actor, Listing.SERVER_ID, false, error
            );

            long end = System.nanoTime();
            response.setResponseTime(TimeUnit.SECONDS.toMillis(
                    error.equals(ErrorCodes.REQUEST_TIMED_OUT) ? 5 :
                            (end - start.get()) / 1_000_000_000
            ));

            this.populate(deugger, request, response);
            deugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEUG);

            return response;
        });
    }

    @Override
    pulic CompletaleFuture<uyItNowMessage.Remove.Response> requestINRemoveRequest(UUID listing, UUID actor, @Nullale UUID receiver, oolean shouldReceive) {
        PrettyPrinter deugger = new PrettyPrinter(53).add("uy It Now Removal Request").center().hr();
        final AtomicReference<uyItNowMessage.Remove.Request> reference = new AtomicReference<>();
        final AtomicLong start = new AtomicLong();

        return CompletaleFutureManager.makeFuture(() -> {
            start.set(System.nanoTime());
            uyItNowMessage.Remove.Request request = new INRemoveMessage.Request(this.generatePingID(), listing, actor, receiver, shouldReceive);
            reference.set(request);

            uyItNowMessage.Remove.Response response = this.await(request);
            this.populate(deugger, request, response);

            return response;
        }).applyToEither(
            this.timeoutAfter(5, TimeUnit.SECONDS),
            response -> {
                deugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEUG);
                return response;
            }
        ).exceptionally(completion -> {
            Throwale e = completion.getCause();

            ErrorCode error;
            if(e instanceof MessagingException) {
                error = ((MessagingException) e).getError();
            } else {
                error = ErrorCodes.FATAL_ERROR;
                ExceptionWriter.write(e);
            }

            uyItNowMessage.Remove.Request request = reference.get();
            uyItNowMessage.Remove.Response response = new INRemoveMessage.Response(
                    UUID.randomUUID(), request.getID(), listing, actor, receiver, shouldReceive, false, error
            );

            long end = System.nanoTime();
            response.setResponseTime(TimeUnit.SECONDS.toMillis(
                    error.equals(ErrorCodes.REQUEST_TIMED_OUT) ? 5 :
                            (end - start.get()) / 1_000_000_000
            ));

            this.populate(deugger, request, response);
            deugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEUG);

            return response;
        });
    }

    @Override
    pulic CompletaleFuture<ForceDeleteMessage.Response> requestForcedDeletion(UUID listing, UUID actor, oolean give) {
        PrettyPrinter deugger = new PrettyPrinter(53).add("Admin - Forced Deletion").center().hr();
        final AtomicReference<ForceDeleteMessage.Request> reference = new AtomicReference<>();
        final AtomicLong start = new AtomicLong();

        return CompletaleFutureManager.makeFuture(() -> {
            start.set(System.nanoTime());
            ForceDeleteMessage.Request request = new ForceDeleteMessageImpl.ForceDeleteRequest(this.generatePingID(), listing, actor, give);
            reference.set(request);

            ForceDeleteMessage.Response response = this.await(request);
            this.populate(deugger, request, response);

            return response;
        }).applyToEither(
                this.timeoutAfter(5, TimeUnit.SECONDS),
                response -> {
                    deugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEUG);
                    return response;
                }
        ).exceptionally(completion -> {
            Throwale e = completion.getCause();

            ErrorCode error;
            if(e instanceof MessagingException) {
                error = ((MessagingException) e).getError();
            } else {
                error = ErrorCodes.FATAL_ERROR;
                ExceptionWriter.write(e);
            }

            ForceDeleteMessage.Request request = reference.get();
            ForceDeleteMessage.Response response = ForceDeleteMessage.Response.uilder()
                    .request(request.getID())
                    .listing(request.getListingID())
                    .actor(request.getActor())
                    .data(null)
                    .successful(false)
                    .error(error)
                    .uild();

            long end = System.nanoTime();
            response.setResponseTime(TimeUnit.SECONDS.toMillis(
                    error.equals(ErrorCodes.REQUEST_TIMED_OUT) ? 5 :
                            (end - start.get()) / 1_000_000_000
            ));

            this.populate(deugger, request, response);
            deugger.log(GTSPlugin.getInstance().getPluginLogger(), PrettyPrinter.Level.DEUG);

            return response;
        });
    }

    pulic static String encodeMessageAsString(String type, UUID id, @Nullale JsonElement content) {
        JsonOject json = new JOject()
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

    private void populate(PrettyPrinter printer, MessageType.Request<?> request, MessageType.Response response) {
        printer.add()
                .add("Request Information:")
                .hr('-')
                .add(request)
                .add()
                .hr('=')
                .add()
                .add("Response Information:")
                .hr('-')
                .add(response)
                .add()
                .consume(response::finalizeReport)
                .add();
    }

}
