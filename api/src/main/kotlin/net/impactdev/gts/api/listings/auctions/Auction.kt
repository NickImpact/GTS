package net.impactdev.gts.api.listings.auctions

import com.google.common.collect.Multimap
import com.google.common.collect.TreeMultimap
import net.impactdev.gts.api.data.Storable
import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.listings.entries.Entry
import net.impactdev.impactor.api.Impactor
import net.impactdev.impactor.api.json.factory.JObject
import net.impactdev.impactor.api.utilities.Builder
import net.impactdev.impactor.api.utilities.mappings.Tuple
import java.time.LocalDateTime
import java.util.*

/**
 * An auction represents a listing which, instead of being directly purchasable, will be able to fluctuate its price
 * based on any user which desires to place a bid on the listing. These listings will still only have one winner by the
 * end of the expiration period, but it is not limited to just one user making an attempt to purchase the listing.
 *
 *
 * As a note, these types of listings must have an expiration time set. No auction can be marked permanent, as
 * they cannot be directly purchased.
 */
interface Auction : Listing {
    /**
     * Attempts to locate the highest bid, or any bid at all, placed by the user on this auction. Only the highest
     * bid will be returned here. If no bids exist for the user, this will return an empty Optional. Otherwise, this
     * call will report the amount of money they placed already on the bid.
     *
     * @param uuid The ID of the user placing the bid
     * @return An Optional value containing the bid amount placed by the user, or an empty Optional to signify no bid
     * has been placed by the user.
     */
    fun getCurrentBid(uuid: UUID?): Optional<Bid?>?

    /**
     * Specifies the number of bids that have been placed on this auction.
     *
     * @return The number of bids placed on the auction
     */
    val numberOfBidsPlaced: Int
        get() = bids.size()

    /**
     * Specifies if any bids have been placed on the auction
     *
     * @return True if any have been placed, false otherwise
     */
    fun hasAnyBidsPlaced(): Boolean {
        return numberOfBidsPlaced != 0
    }

    /**
     * Keeps track of the bids placed on this auction, with the highest bid being the first entry in the sorted map,
     * and the lowest being the last entry.
     *
     * @return A mapping of bids placed on this auction by a user and for how much they bid
     */
    val bids: TreeMultimap<UUID?, Bid?>

    /**
     * Returns a list of all users who have bid on this auction, without any information regarding what they have
     * bid.
     *
     * @return Each unique bidder
     */
    val uniqueBidders: List<UUID?>?
        get() = ArrayList(bids.keys())

    /**
     * Like [.getUniqueBidders], this method returns a set of all unique bidders for this auction, as well
     * as provides contextual information towards their highest bid.
     *
     * @return Each unique bidder paired with their highest bid
     */
    val uniqueBiddersWithHighestBids: Map<UUID?, Bid?>?

    /**
     * Returns the highest bid currently placed on this auction. The high bid at time of expiration marks the winner,
     * so having an easy call to this allows for simple access later on.
     *
     * @return The highest bidder paired with the amount they bid
     */
    val highBid: Optional<Tuple<UUID?, Bid?>?>?

    /**
     * Specifies the starting price of this auction. This is mainly here for tracking, and can be represented
     * by [.getCurrentPrice].
     *
     * @return The price of the auction when initially created
     */
    val startingPrice: Double

    /**
     * Specifies the current price of this auction. If no bids are currently placed, this will return the initial
     * starting price set on the auction. Otherwise, this will show the highest bid currently placed on the auction.
     *
     * @return The current price of the auction
     */
    val currentPrice: Double

    /**
     * Specifies the rate at which continuous bids will be applied. In other words, this value is a percentage value
     * meant to help scale the higher the price of the item gets. This will always round up to the next whole number,
     * so, if you start an auction with a price of $1, and have an increment rate of 5%, this will still lead to $2.
     *
     * @return The increment to apply to the next bid
     */
    val increment: Float

    /**
     * A convenience method meant to be able to determine how much a user will be bidding
     * on the auction should they decide to place a bid.
     *
     * @return the next bid requirement to place a bid on this auction
     */
    val nextBidRequirement: Double

    /**
     * Allows a user to bid on the listing for the amount specified. As a user could specify a custom amount to bid, this
     * call must accept a dynamic value for the amount bid. As such, this call should also verify if the amount bid is
     * actually valid.
     *
     * @param user The user placing the bid
     * @param amount The amount of money they are bidding
     * @return True if their bid was applied, false otherwise
     */
    fun bid(user: UUID?, amount: Double): Boolean
    interface AuctionBuilder : Builder<Auction?, AuctionBuilder?> {
        fun id(id: UUID?): AuctionBuilder?
        fun lister(lister: UUID?): AuctionBuilder?
        fun entry(entry: Entry<*, *>?): AuctionBuilder?
        fun published(published: LocalDateTime?): AuctionBuilder?
        fun expiration(expiration: LocalDateTime?): AuctionBuilder?
        fun start(amount: Double): AuctionBuilder?
        fun increment(rate: Float): AuctionBuilder?
        fun current(current: Double): AuctionBuilder?
        fun bids(bids: Multimap<UUID?, Bid?>?): AuctionBuilder?
    }

    class Bid : Storable, Comparable<Bid> {
        val amount: Double
        val timestamp: LocalDateTime

        constructor(amount: Double) {
            this.amount = amount
            timestamp = LocalDateTime.now()
        }

        private constructor(builder: BidBuilder) {
            amount = builder.amount
            timestamp = builder.timestamp
        }

        override fun getVersion(): Int {
            return 1
        }

        override fun serialize(): JObject? {
            return JObject()
                .add("amount", amount)
                .add("timestamp", timestamp.toString())
        }

        override fun compareTo(other: Bid): Int {
            return java.lang.Double.compare(amount, other.amount)
        }

        class BidBuilder : Builder<Bid, BidBuilder> {
            var amount = 0.0
            var timestamp = LocalDateTime.now()
            fun amount(amount: Double): BidBuilder {
                this.amount = amount
                return this
            }

            fun timestamp(timestamp: LocalDateTime): BidBuilder {
                this.timestamp = timestamp
                return this
            }

            override fun from(bid: Bid): BidBuilder {
                amount = bid.amount
                timestamp = bid.timestamp
                return this
            }

            override fun build(): Bid {
                return Bid(this)
            }
        }

        companion object {
            @kotlin.jvm.JvmStatic
			fun builder(): BidBuilder {
                return BidBuilder()
            }
        }
    }

    class BidContext(val bidder: UUID, val bid: Bid)
    companion object {
        @kotlin.jvm.JvmStatic
		fun builder(): AuctionBuilder? {
            return Impactor.getInstance().registry.createBuilder(AuctionBuilder::class.java)
        }
    }
}