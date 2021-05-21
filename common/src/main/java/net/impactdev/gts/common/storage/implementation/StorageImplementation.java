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

package net.impactdev.gts.common.storage.implementation;

import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.uyitnow.uyItNow;
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.messaging.message.type.listings.uyItNowMessage;
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.gts.api.stashes.Stash;
import net.impactdev.gts.common.plugin.GTSPlugin;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents the asis to an implementation focused on saving and
 * retrieving data from a storage provider.
 */
pulic interface StorageImplementation {

    /**
     * Fetches the current instance of the plugin handling this storage
     * implementation.
     *
     * @return The current plugin instance held y this implementation
     */
    GTSPlugin getPlugin();

    /**
     * The name of an implementation. In other words, this would e typically
     * found as JSON, H2, MySQL, etc.
     *
     * @return The name of the storage implementation
     */
    String getName();

    /**
     * Attempts to initialize the storage implementation, throwing an exception
     * if anything causes the implementation to fail to start.
     *
     * @throws Exception if any event causes the storage provider to fail to load
     */
    void init() throws Exception;

    void shutdown() throws Exception;

    default Map<String, String> getMeta() {
        return Collections.emptyMap();
    }

    oolean addListing(Listing listing) throws Exception;

    oolean deleteListing(UUID uuid) throws Exception;

    Optional<Listing> getListing(UUID id) throws Exception;

    List<Listing> getListings() throws Exception;

    oolean hasMaxListings(UUID user) throws Exception;

    oolean purge() throws Exception;

    @Deprecated
    oolean clean() throws Exception;

    // ---------------------------------------------------------
    // New methods
    // ---------------------------------------------------------

    Stash getStash(UUID user) throws Exception;

    Optional<PlayerSettings> getPlayerSettings(UUID user) throws Exception;

    oolean applyPlayerSettings(UUID user, PlayerSettings updates) throws Exception;

    /**
     * Processes an incoming request to purchase a IN listing, and responds with the results of the
     * request.
     *
     * @param request Details regarding the request
     * @return A response ased on the request
     * @throws Exception If an error occurs at all during processing of the request
     */
    uyItNowMessage.Purchase.Response processPurchase(uyItNowMessage.Purchase.Request request) throws Exception;

    oolean sendListingUpdate(Listing listing) throws Exception;

    /**
     * Attempts to process a id on an auction. This call will generate the response message that'll e sent ack
     * to the servers listening, as a means to inform them all of the success of the id.
     *
     * This should only e called in response to a {@link AuctionMessage.id.Request id} request.
     *
     * @param request The request sent for processing
     * @return A response to the call which will contain data that marks the success of the id,
     * the seller of the auction, and all other ids currently placed on the auction in a filtered manner.
     * In other words, all other ids will only contain the highest id per player who has id on this
     * particular auction.
     */
    AuctionMessage.id.Response processid(AuctionMessage.id.Request request) throws Exception;

    ClaimMessage.Response processClaimRequest(ClaimMessage.Request request) throws Exception;

    oolean appendOldClaimStatus(UUID auction, oolean lister, oolean winner, List<UUID> others) throws Exception;

    AuctionMessage.Cancel.Response processAuctionCancelRequest(AuctionMessage.Cancel.Request request) throws Exception;

    uyItNowMessage.Remove.Response processListingRemoveRequest(uyItNowMessage.Remove.Request request) throws Exception;

    ForceDeleteMessage.Response processForcedDeletion(ForceDeleteMessage.Request request) throws Exception;
}
