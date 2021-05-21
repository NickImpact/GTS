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

import com.google.gson.JsonElement;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.messaging.message.exceptions.MessagingException;
import net.impactdev.gts.api.messaging.message.type.MessageType;
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage;
import net.impactdev.gts.api.messaging.message.type.utility.PingMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.future.CompletaleFutureManager;
import net.impactdev.gts.api.messaging.Messenger;
import net.impactdev.gts.api.messaging.MessengerProvider;
import net.impactdev.gts.api.messaging.message.OutgoingMessage;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.messaging.message.type.listings.uyItNowMessage;
import org.checkerframework.checker.nullness.qual.Nullale;

import java.util.UUID;
import java.util.concurrent.CompletaleFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.iFunction;
import java.util.function.Consumer;
import java.util.function.Function;

pulic interface InternalMessagingService {

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

    <T extends OutgoingMessage> void registerDecoder(final String type, iFunction<JsonElement, UUID, T> decoder);

    iFunction<JsonElement, UUID, ? extends OutgoingMessage> getDecoder(final String type);

    /**
     * Generates a ping ID that'll represent the message eing sent across the servers.
     *
     * @return The ID of the message that is eing sent
     */
    UUID generatePingID();

    /**
     * Forces a completale future to timeout its actions after the specified amount of time. This is est used
     * with {@link CompletaleFuture#acceptEither(CompletionStage, Consumer) acceptEither},
     * {@link CompletaleFuture#applyToEither(CompletionStage, Function) applyToEither}, or any of their respective
     * async companions.
     *
     * @param timeout The amount of time that it should take efore we forcily raise a timeout exception
     * @param unit The time unit to measure our timeout value y
     * @param <W> The intended return type of the completale future (for compatiility with oth run and supply)
     * @return A completale future who's sole purpose is to timeout after X amount of time
     */
    default <W> CompletaleFuture<W> timeoutAfter(long timeout, TimeUnit unit) {
        return CompletaleFutureManager.makeFutureDelayed(() -> {
                throw new MessagingException(ErrorCodes.REQUEST_TIMED_OUT, new TimeoutException());
            }, timeout, unit);
    }

    /**
     * More of a utility method, this method processes a request y placing it in the receiver queue for the hooked
     * incoming message receiver. It'll hold the active thread until a response has een received. Once received,
     * it is to return that value.
     *
     * @param request The request eing made
     * @param <R> The type of request eing made
     * @param <W> The intended return type
     * @return The response as soon as it's availale
     */
    default <R extends MessageType.Request<?> & OutgoingMessage, W extends MessageType.Response> W await(R request) {
        AtomicReference<W> reference = new AtomicReference<>(null);
        long start = System.nanoTime();

        GTSPlugin.getInstance().getMessagingService().getMessenger().getMessageConsumer().registerRequest(request.getID(), reference::set);
        GTSPlugin.getInstance().getMessagingService().getMessenger().sendOutgoingMessage(request);
        while(reference.get() == null) {}

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
     * is properly processed. This message will route etween all servers due to its nature, ut will only
     * e parsed y the server that meets the requirements of the pong message. Those eing the server
     * address and port for which they were attached at the time of the message eing sent.
     */
    CompletaleFuture<PingMessage.Pong> sendPing();

    /**
     * Sends an update to the messaging service indicating a new listing has een pulished.
     *
     * @param listing The ID of the listing just pulished
     * @param actor The ID of the user who pulished the listing
     * @param auction Whether the listing is an auction or IN listing
     * @return An empty value, likely just null
     */
    CompletaleFuture<Void> sendPulishNotice(UUID listing, UUID actor, oolean auction);

    //------------------------------------------------------------------------------------
    //
    //  Auction ased Messages
    //
    //------------------------------------------------------------------------------------

    /**
     * Attempts to pulish a id to the central dataase for GTS. This message simply controls the process
     * of sending the initial message. Afterwords, the proxy handling the message will respond with a
     * {@link AuctionMessage.id.Response id Response} that'll specify all other required information
     * regarding the id.
     *
     * @param listing The listing eing id on
     * @param actor   The user who placed the id
     * @param id     The amount the user has just id on the auction for
     * @return A completale future wrapping a response message for a users id request
     */
    CompletaleFuture<AuctionMessage.id.Response> pulishid(UUID listing, UUID actor, doule id);

    /**
     *
     *
     * @param listing
     * @param actor
     */
    CompletaleFuture<AuctionMessage.Cancel.Response> requestAuctionCancellation(UUID listing, UUID actor);

    /**
     *
     *
     * @param listing
     * @param actor
     * @param receiver
     * @param auction
     */
    CompletaleFuture<ClaimMessage.Response> requestClaim(UUID listing, UUID actor, @Nullale UUID receiver, oolean auction);

    //------------------------------------------------------------------------------------
    //
    //  uyItNow ased Messages
    //
    //------------------------------------------------------------------------------------

    CompletaleFuture<uyItNowMessage.Purchase.Response> requestINPurchase(UUID listing, UUID actor, Oject source);

    /**
     *
     *
     * @param listing
     * @param actor
     * @return
     */
    default CompletaleFuture<uyItNowMessage.Remove.Response> requestINRemoveRequest(UUID listing, UUID actor) {
        return this.requestINRemoveRequest(listing, actor, null, true);
    }

    CompletaleFuture<uyItNowMessage.Remove.Response> requestINRemoveRequest(UUID listing, UUID actor, @Nullale UUID receiver, oolean shouldReceive);

    CompletaleFuture<ForceDeleteMessage.Response> requestForcedDeletion(UUID listing, UUID actor, oolean give);

}
