package net.impactdev.gts.api.listings.prices

import net.impactdev.gts.api.data.ResourceManager
import net.impactdev.gts.api.listings.ui.EntryUI
import net.impactdev.gts.api.util.TriConsumer
import net.impactdev.impactor.api.gui.UI
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer

interface PriceManager<T, P> : ResourceManager<T> {
    fun process(): TriConsumer<P, EntryUI<*, *, *>?, BiConsumer<EntryUI<*, *, *>?, Price<*, *, *>?>?>?
    fun <U : UI<*, *, *, *>?> getSelector(
        viewer: P,
        price: Price<*, *, *>?,
        callback: Consumer<Any?>?
    ): Optional<PriceSelectorUI<U>?>?

    interface PriceSelectorUI<U : UI<*, *, *, *>?> {
        val display: U
        val callback: Consumer<Any?>?
    }
}