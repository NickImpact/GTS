package net.impactdev.gts.common.tasks

import net.impactdev.gts.api.listings.Listing

interface ListingTasks<T : Listing?> {
    fun createExpirationTask()
    fun expire(listing: T): Boolean
}