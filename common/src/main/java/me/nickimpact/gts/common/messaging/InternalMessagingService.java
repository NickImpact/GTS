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

package me.nickimpact.gts.common.messaging;

import com.google.gson.JsonElement;
import me.nickimpact.gts.api.messaging.Messenger;
import me.nickimpact.gts.api.messaging.MessengerProvider;
import me.nickimpact.gts.api.messaging.message.OutgoingMessage;
import me.nickimpact.gts.api.messaging.message.type.auctions.AuctionMessage;
import me.nickimpact.gts.common.cache.BufferedRequest;

import java.util.UUID;
import java.util.function.BiFunction;

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
    void sendPing();

    //------------------------------------------------------------------------------------
    //
    //  Auction Based Messages
    //
    //------------------------------------------------------------------------------------

    /**
     *
     *
     * @param auction
     * @param actor
     * @param broadcast
     */
    void publishAuctionListing(UUID auction, UUID actor, String broadcast);

    /**
     * Attempts to publish a bid to the central database for GTS. This message simply controls the process
     * of sending the initial message. Afterwords, the proxy handling the message will respond with a
     * {@link AuctionMessage.Bid.Response Bid Response} that'll
     * specify all other required information regarding the bid.
     *
     * @param listing The listing being bid on
     * @param actor   The user who placed the bid
     * @param bid     The amount the user has just bid on the auction for
     */
    void publishBid(UUID listing, UUID actor, double bid);

    /**
     *
     *
     * @param listing
     * @param actor
     */
    void requestAuctionCancellation(UUID listing, UUID actor);

    //------------------------------------------------------------------------------------
    //
    //  Quick Purchase Based Messages
    //
    //------------------------------------------------------------------------------------

    /**
     *
     *
     * @param listing
     * @param actor
     * @param broadcast
     */
    void publishQuickPurchaseListing(UUID listing, UUID actor, String broadcast);


}
