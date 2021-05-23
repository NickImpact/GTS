package net.impactdev.gts.api.listings.ui

import java.util.*

abstract class AbstractEntryUI<P, E, I>(protected val viewer: P) : EntryUI<P, E, I> {
    @kotlin.jvm.JvmField
    protected var chosen: E? = null
    override val chosenOption: Optional<E>?
        get() = Optional.ofNullable(chosen)

    abstract fun setChosen(chosen: E)
}