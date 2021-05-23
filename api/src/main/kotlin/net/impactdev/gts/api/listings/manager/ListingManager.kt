package net.impactdev.gts.api.listings.manager

import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.listings.auctions.Auction
import net.impactdev.gts.api.listings.buyitnow.BuyItNow
import net.impactdev.impactor.api.services.Service
import java.util.*
import java.util.concurrent.CompletableFuture

interface ListingManager<L : Listing?, A : Auction?, B : BuyItNow?> : Service {
    fun list(lister: UUID?, listing: L): CompletableFuture<Boolean?>?
    fun bid(bidder: UUID?, listing: A, amount: Double): CompletableFuture<Boolean?>?
    fun <S> purchase(buyer: UUID?, listing: B, source: S): CompletableFuture<Boolean?>?
    fun deleteListing(listing: L): CompletableFuture<Boolean?>?
    fun hasMaxListings(lister: UUID?): CompletableFuture<Boolean?>?
    fun fetchListings(): CompletableFuture<List<L>?>?
}