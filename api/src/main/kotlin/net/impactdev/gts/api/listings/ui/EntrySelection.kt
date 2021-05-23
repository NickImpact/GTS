package net.impactdev.gts.api.listings.ui

import net.impactdev.gts.api.listings.entries.Entry

interface EntrySelection<T : Entry<*, *>?> {
    fun createFromSelection(): T
}