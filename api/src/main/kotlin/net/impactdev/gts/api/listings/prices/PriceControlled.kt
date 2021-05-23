package net.impactdev.gts.api.listings.prices

/**
 * Represents an Entry that has a set of price controls applied to it. This design is meant to only
 * cover prices that are controlled via a set currency background.
 */
interface PriceControlled {
    val min: Double
    val max: Double
}