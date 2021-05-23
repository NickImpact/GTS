package net.impactdev.gts.api.listings.makeup

interface Display<T> {
    /**
     * Represents the display that'll be used when this listing is queried in a GTS user interface. This will realistically
     * be an ItemStack based on the server system used.
     *
     * @return The instance used to display this listing
     */
    fun get(): T
}