package net.impactdev.gts.api.listings.ui

import java.util.*

interface EntryUI<P, E, I> {
    val chosenOption: Optional<E>?
    fun open(user: P)
    fun generateWaitingIcon(auction: Boolean): I
    fun generateConfirmIcon(): I
    fun createNoneChosenIcon(): I
    fun createChosenIcon(): I
    fun createPriceIcon(): I
    fun createTimeIcon(): I
    fun style(selected: Boolean)
}