package net.impactdev.gts.api.searching

import net.impactdev.gts.api.listings.Listing

interface Searcher {
    fun parse(listing: Listing?, input: String?): Boolean
}