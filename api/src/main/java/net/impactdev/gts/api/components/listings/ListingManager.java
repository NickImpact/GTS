package net.impactdev.gts.api.components.listings;

import net.impactdev.gts.api.components.listings.models.Auction;
import net.impactdev.gts.api.components.listings.models.BuyItNow;
import net.impactdev.gts.api.components.listings.models.Listing;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * The listing manager represents the forefront for processing listing based actions. Additionally,
 * this service provides a means of accessing the listings without directly relying on the storage
 * provider. The goal of the manager is to handle the majority of the logic behind the many actions
 * of users, and delegate the specific actions to the proper controllers where necessary.
 *
 * <p>Each action is expected to run asynchronously from the main server thread. This is due
 * to the many jumps the plugin must make in individual calls where an action might not be
 * readily available at the moment the call is executed.
 */
public interface ListingManager {

    /**
     * Attempts to locate a set of listings with the given filter. It is expected that,
     * while the argument accepts a singular predicate, that children predicates are
     * attached to the argument itself. So, if you wish to look for only auctions and
     * have a publishing year of 2022, you could run the following code:
     * <pre>
     *     Predicate&ltListing&gt filter = listing -> listing instanceof Auction;
     *     filter.and(listing -> listing.published().getYear() > 2021;
     *     ListingManager#listings(filter)...</pre>
     *
     * The given result features an ordered set, following the rules of the comparable
     * inheritance on a listing. This result type is meant to help indicate that all
     * listings are unique.
     *
     * @return An ordered set of all known listings
     */
    CompletableFuture<Set<Listing>> listings(Predicate<Listing> filter);

    /**
     * Appends the listing to the market, should the actor meet the necessary conditions
     * to be able to list a new listing.
     *
     * @param actor The UUID of the user making the listing attempt
     * @param listing The actual listing data that should be written to memory
     * @return A completable future indicating the success result of the action
     */
    CompletableFuture<TriState> list(UUID actor, Listing listing);

    /**
     * Attempts to process a purchase request by the given actor for the specified listing. In some
     * cases, a listing's price might additionally require an argument to be specified which
     * indicates the source of the payment. This is meant to handle prices which extend beyond
     * the normal expectations, and carry data relative to itself that should be guaranteed to be
     * passed on.
     *
     * @param actor The UUID of the user making the request
     * @param listing The actual listing data for the purchased listing
     * @param selection A possibly null value responsible for price types requiring an argument
     *                  from the player
     * @return A completable future indicating the success result of the action
     */
    // TODO - Rather than the selection object, consider some context method or rethink the method
    // TODO - of running a purchase
    CompletableFuture<TriState> purchase(UUID actor, BuyItNow listing, @Nullable Object selection);

    CompletableFuture<TriState> bid(UUID actor, Auction auction, BigDecimal amount);

    CompletableFuture<TriState> remove(UUID actor, UUID listing);

    CompletableFuture<TriState> hasMax(UUID target);

}
