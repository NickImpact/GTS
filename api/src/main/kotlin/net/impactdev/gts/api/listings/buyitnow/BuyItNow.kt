package net.impactdev.gts.api.listings.buyitnow

import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.listings.Listing.ListingBuilder
import net.impactdev.gts.api.listings.prices.Price
import net.impactdev.impactor.api.Impactor
import java.util.*

/**
 * A QuickPurchase listing is the typical listing GTS provides. In general, this listing provides a one-time
 * purchase for users who wish to simply avoid participating in auctions. Therefore, the price set for
 * this listing option will be final and unmodifiable.
 */
interface BuyItNow : Listing {
    /**
     * Represents the price of this listing. This will be what a purchasing player must pay in order to buy this
     * listing off the GTS.
     *
     * @return The price of the listing
     */
    val price: Price<*, *, *>?

    /**
     * Represents the user who purchased a BIN listing
     *
     * @return The UUID of the user who purchased this BIN listing.
     */
    fun purchaser(): UUID?

    /**
     * Represents that this BIN listing exists only due to a failed attempt to redeem a listing
     * directly after a purchase. This listing should only attempt to return the entry, rather than
     * the price to the listing owner, who in this case will now be the seller.
     *
     * @return True if stashed for the purchaser, false otherwise
     */
    fun stashedForPurchaser(): Boolean

    /**
     * Specifies whether or not this listing has been purchased
     *
     * @return True if purchased, false otherwise
     */
    val isPurchased: Boolean

    /**
     * Marks a listing as purchased
     */
    fun markPurchased()
    interface BuyItNowBuilder : ListingBuilder<BuyItNow?, BuyItNowBuilder?> {
        /**
         * Sets the price of this BIN listing
         *
         * @param price The price for the listing
         * @return The updated builder
         */
        override fun price(price: Price<*, *, *>?): BuyItNowBuilder?

        /**
         * Indicates that the built listing has been purchased
         *
         * @return The updated builder
         */
        fun purchased(): BuyItNowBuilder?

        /**
         * Sets the user who purchased this listing
         *
         * @param purchaser The ID of the purchaser
         * @return The updated builder
         */
        fun purchaser(purchaser: UUID?): BuyItNowBuilder?

        /**
         * Sets this BIN listing as a listing specifically set to be returned to the purchaser,
         * likely due to a failure to reward the purchaser with what they purchased.
         *
         * @return The updated builder
         */
        fun stashedForPurchaser(): BuyItNowBuilder?
    }

    companion object {
        @kotlin.jvm.JvmStatic
		fun builder(): BuyItNowBuilder? {
            return Impactor.getInstance().registry.createBuilder(BuyItNowBuilder::class.java)
        }
    }
}