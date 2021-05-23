package net.impactdev.gts.api.storage

import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage.Purchase
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage
import net.impactdev.gts.api.player.PlayerSettings
import net.impactdev.gts.api.stashes.Stash
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate

interface GTSStorage {
    fun init()
    fun shutdown()
    val meta: CompletableFuture<Map<String?, String?>?>?
    fun publishListing(listing: Listing?): CompletableFuture<Boolean?>?
    fun getListing(listing: UUID?): CompletableFuture<Optional<Listing?>?>?

    /**
     * Fetches all listings, with no filters applied. This is to allow all listings to be processed and accessible,
     * despite expiration of a listing or another filter that would be typically applied against it.
     *
     * @return Every listing currently stored in the database
     */
    fun fetchListings(): CompletableFuture<List<Listing?>?>? {
        return this.fetchListings(emptyList())
    }

    /**
     * Retrieves all listings, and applies any filters against the returned set. These filters can be situations
     * like retrieving only listings that have not yet expired.
     *
     * @param filters Any filters to apply against the received set of listings.
     * @return Every listing meeting the conditions specified by the passed in filters
     */
    fun fetchListings(filters: Collection<Predicate<Listing?>?>?): CompletableFuture<List<Listing?>?>?

    /**
     * Retrieves the stash of the user who holds it. The stash of a user contains items purchased
     * that they couldn't receive at the time, or listings of theirs that have expired over time.
     *
     * @param user The user representing the holder of the stash
     * @return The stash as it is currently
     */
    fun getStash(user: UUID?): CompletableFuture<Stash?>?
    fun getPlayerSettings(uuid: UUID?): CompletableFuture<Optional<PlayerSettings?>?>?
    fun applyPlayerSettings(uuid: UUID?, settings: PlayerSettings?): CompletableFuture<Boolean?>?
    fun hasMaxListings(user: UUID?): CompletableFuture<Boolean?>?
    fun purge(): CompletableFuture<Boolean?>?

    /**
     * Purges remaining data from the legacy database if it persists.
     *
     * @return True if the table existed and was deleted, false otherwise
     */
    @Deprecated("This is a temporary function, and is scheduled to be removed at earliest convenience")
    fun clean(): CompletableFuture<Boolean?>?
    //------------------------------------------------------------------------------------------------------------------
    //
    //  Any and all actions that might require a message to transmit the request to the storage provider
    //
    //------------------------------------------------------------------------------------------------------------------
    /**
     * Attempts to send a request to the database to process a bid on an auction. Given that the request and response
     * should feature raw json data, the return type of a message response allows for the data to be passed along
     * to the requesting server.
     *
     * @param request The bid request being made by a connected server
     * @return A response indicating the success or failure of the bid request, fit with all necessary information
     */
    fun processBid(request: AuctionMessage.Bid.Request?): CompletableFuture<AuctionMessage.Bid.Response?>?

    /**
     * Indicates a user is attempting to claim something from a listing within their stash. This message
     * is common to both auctions and BIN listings, with auctions receiving an extended response, via
     * [ClaimMessage.Response.AuctionResponse].
     *
     * @param request
     * @return
     */
    fun processClaimRequest(request: ClaimMessage.Request?): CompletableFuture<ClaimMessage.Response?>?
    fun appendOldClaimStatus(
        auction: UUID?,
        lister: Boolean,
        winner: Boolean,
        others: List<UUID?>?
    ): CompletableFuture<Boolean?>?

    fun processAuctionCancelRequest(request: AuctionMessage.Cancel.Request?): CompletableFuture<AuctionMessage.Cancel.Response?>?
    fun processListingRemoveRequest(request: BuyItNowMessage.Remove.Request?): CompletableFuture<BuyItNowMessage.Remove.Response?>?
    fun processPurchase(request: Purchase.Request?): CompletableFuture<Purchase.Response?>?

    //------------------------------------------------------------------------------------------------------------------
    //
    //  Admin based message processing
    //
    //------------------------------------------------------------------------------------------------------------------
    fun processForcedDeletion(request: ForceDeleteMessage.Request?): CompletableFuture<ForceDeleteMessage.Response?>?
}