package net.impactdev.gts.api.elements.listings.models;

import com.google.common.collect.TreeMultimap;
import net.impactdev.gts.api.elements.content.Price;
import net.impactdev.gts.api.elements.listings.Listing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Auction extends Listing {

    /**
     * Specifies the starting price of an Auction. An auction can only work with an incremental price,
     * given a price is not a static figure and should be capable of being updated.
     *
     * @return
     */
    Price.Incremental starting();

    Price.Incremental next();

    Price.Incremental current();

    TreeMultimap<UUID, Bid> bids();

    default Set<UUID> bidders() {
        return this.bids().keySet();
    }

    default Optional<Bid> highestBid() {
        return this.bids().entries().stream()
                .max(Comparator.comparing(value -> value.getValue().amount()))
                .map(Map.Entry::getValue);
    }

    // TODO - Change boolean return type to some sort of transaction that holds result and additional
    // TODO - error information should any be available
    CompletableFuture<Boolean> bid(UUID bidder, BigDecimal amount);

    interface Bid {

        UUID bidder();

        Price.Incremental amount();

        LocalDateTime timestamp();

    }

}
