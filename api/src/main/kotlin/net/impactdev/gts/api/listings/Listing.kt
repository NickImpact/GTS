package net.impactdev.gts.api.listings

import net.impactdev.gts.api.data.Storable
import net.impactdev.gts.api.listings.entries.Entry
import net.impactdev.gts.api.listings.makeup.Display
import net.impactdev.gts.api.listings.prices.Price
import net.impactdev.impactor.api.Impactor
import net.impactdev.impactor.api.utilities.Builder
import java.time.LocalDateTime
import java.util.*
import java.util.function.Predicate

/**
 * A listing represents the overall information of something listed onto the GTS.
 */
interface Listing : Storable {
    fun <T : Listing?> `as`(type: Class<T>): T {
        return type.cast(this)
    }

    /**
     * Represents the ID of this listing. This is independent of the lister's UUID, which serves as a reference
     * to the player or system that listed the listing.
     *
     * @return The unique ID of this listing
     */
    val iD: UUID?

    /**
     * Represents the user listing this Listing. If this is a player, the UUID will be that of the player.
     * If the server creates the listing, then this UUID will match the generic zeroed out ID.
     *
     * @return The ID of the lister who created this listing
     */
    val lister: UUID?

    /**
     * Represents the actual component of the listing that will be contained by this listing. This is what a user will
     * be purchasing should they purchase the listing.
     *
     * @return The entry making up this listing.
     */
    val entry: Entry<*, *>

    /**
     * Represents the display of the listing. This is essentially how the listing will be displayed to the user
     * when queried in-game.
     *
     * @return The display parameters of this listing
     */
    fun getDisplay(viewer: UUID?): Display<*>? {
        return entry.getDisplay(viewer, this)
    }

    /**
     * Details the exact time at which a listing was published to the GTS market. This is namely helpful
     * for tracking purposes.
     *
     * @return The time this listing was published
     */
    val publishTime: LocalDateTime?

    /**
     * Represents the time where this listing will expire.
     *
     * @return The time this listing will expire
     */
    val expiration: LocalDateTime?

    /**
     * Attempts to verify whether or not a listing has expired. If a listing has no expiration, this call will
     * always be false. If an expiration does exist, this call will verify its validity based on the marked
     * expiration time with the current time at the time of the call.
     *
     * @return True if the listing has an expiration and said expiration is before the current system time,
     * or false if the expiration is still after the current system time, or this listing has no expiration.
     */
    fun hasExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiration)
    }

    interface ListingBuilder<L : Listing?, B : ListingBuilder<*, *>?> : Builder<L, B> {
        /**
         * Represents the ID of a listing. If not specified, this will be auto-generated at the time of constructing.
         *
         * @param id The ID to use for the listing
         * @return The builder modified with this value
         */
        fun id(id: UUID?): B

        /**
         * Specifies the seller of this listing. The seller is simply a mapping to a
         *
         * @param lister The individual creating this listing
         * @return The builder modified with this value
         */
        fun lister(lister: UUID?): B
        fun entry(entry: Entry<*, *>?): B
        fun price(price: Price<*, *, *>?): B
        fun expiration(expiration: LocalDateTime?): B
    }

    companion object {
        fun builder(): ListingBuilder<*, *>? {
            return Impactor.getInstance().registry.createBuilder(ListingBuilder::class.java)
        }

        /** The UUID to use when the server itself creates a listing  */
		@kotlin.jvm.JvmField
		val SERVER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000")
        val NON_EXPIRED_LISTINGS = Predicate { listing: Listing -> !listing.hasExpired() }
    }
}