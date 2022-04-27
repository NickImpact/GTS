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

import com.google.gson.JsonElement;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.messaging.message.exceptions.MessagingException;
import net.impactdev.gts.api.messaging.message.type.MessageType;
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.api.messaging.message.type.deliveries.ClaimDelivery;
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage;
import net.impactdev.gts.api.messaging.message.type.utility.PingMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.common.utils.future.CompletableFutureManager;
import net.impactdev.gts.api.messaging.Messenger;
import net.impactdev.gts.api.messaging.MessengerProvider;
import net.impactdev.gts.api.messaging.message.OutgoingMessage;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public interface InternalMessagingService {

    /**
     * Gets the name of this messaging service
     *
     * @return the name of this messaging service
     */
    String getName();

    Messenger getMessenger();

    MessengerProvider getMessengerProvider();

    /**
     * Closes the messaging service
     */
    void close();

    <T extends OutgoingMessage> void registerDecoder(final String type, BiFunction<JsonElement, UUID, T> decoder);

    BiFunction<JsonElement, UUID, ? extends OutgoingMessage> getDecoder(final String type);

    /**
     * Generates a ping ID that'll represent the message being sent across the servers.
     *
     * @return The ID of the message that is being sent
     */
    UUID generatePingID();

    /**
     * More of a utility method, this method processes a request by placing it in the receiver queue for the hooked
     * incoming message receiver. It'll hold the active thread until a response has been received. Once received,
     * it is to return that value.
     *
     * @param request The request being made
     * @param <R> The type of request being made
     * @param <W> The intended return type
     * @return The response as soon as it's available
     */
    default <R extends MessageType.Request<?> & OutgoingMessage, W extends MessageType.Response> W await(R request) {
        AtomicReference<W> reference = new AtomicReference<>(null);
        long start = System.nanoTime();

        GTSPlugin.instance().messagingService().getMessenger().getMessageConsumer().registerRequest(request.getID(), reference::set);
        GTSPlugin.instance().messagingService().getMessenger().sendOutgoingMessage(request);
        while(reference.get() == null) {
            try {
                //noinspection BusyWait
                Thread.sleep(50);
            } catch (InterruptedException e) {
                ExceptionWriter.write(e);
            }
        }

        long finish = System.nanoTime();
        return reference.updateAndGet(response -> {
            response.setResponseTime((finish - start) / 1000000);
            return response;
        });
    }

    //------------------------------------------------------------------------------------
    //
    //  General Plugin Messages
    //
    //------------------------------------------------------------------------------------

    /**
     * Sends a ping to the proxy controller. The proxy will then respond with a pong message, assuming the message
     * is properly processed. This message will route between all servers due to its nature, but will only
     * be parsed by the server that meets the requirements of the pong message. Those being the server
     * address and port for which they were attached at the time of the message being sent.
     */
    CompletableFuture<PingMessage.Pong> sendPing();

    /**
     * Sends an update to the messaging service indicating a new listing has been published.
     *
     * @param listing The ID of the listing just published
     * @param actor The ID of the user who published the listing
     * @param auction Whether the listing is an auction or BIN listing
     * @return An empty value, likely just null
     */
    CompletableFuture<Void> sendPublishNotice(UUID listing, UUID actor, boolean auction);

    //------------------------------------------------------------------------------------
    //
    //  Auction Based Messages
    //
    //------------------------------------------------------------------------------------

    /**
     * Attempts to publish a bid to the central database for GTS. This message simply controls the process
     * of sending the initial message. Afterwords, the proxy handling the message will respond with a
     * {@link AuctionMessage.Bid.Response Bid Response} that'll specify all other required information
     * regarding the bid.
     *
     * @param listing The listing being bid on
     * @param actor   The user who placed the bid
     * @param bid     The amount the user has just bid on the auction for
     * @return A completable future wrapping a response message for a users bid request
     */
    CompletableFuture<AuctionMessage.Bid.Response> publishBid(UUID listing, UUID actor, double bid);

    /**
     *
     *
     * @param listing
     * @param actor
     */
    CompletableFuture<AuctionMessage.Cancel.Response> requestAuctionCancellation(UUID listing, UUID actor);

    /**
     *
     *
     * @param listing
     * @param actor
     * @param receiver
     * @param auction
     */
    CompletableFuture<ClaimMessage.Response> requestClaim(UUID listing, UUID actor, @Nullable UUID receiver, boolean auction);

    //------------------------------------------------------------------------------------
    //
    //  BuyItNow Based Messages
    //
    //------------------------------------------------------------------------------------

    CompletableFuture<BuyItNowMessage.Purchase.Response> requestBINPurchase(UUID listing, UUID actor, Object source);

    /**
     *
     *
     * @param listing
     * @param actor
     * @return
     */
    default CompletableFuture<BuyItNowMessage.Remove.Response> requestBINRemoveRequest(UUID listing, UUID actor) {
        return this.requestBINRemoveRequest(listing, actor, null, true);
    }

    CompletableFuture<BuyItNowMessage.Remove.Response> requestBINRemoveRequest(UUID listing, UUID actor, @Nullable UUID receiver, boolean shouldReceive);

    CompletableFuture<ForceDeleteMessage.Response> requestForcedDeletion(UUID listing, UUID actor, boolean give);

    CompletableFuture<ClaimDelivery.Response> requestDeliveryClaim(UUID delivery, UUID actor);

}
