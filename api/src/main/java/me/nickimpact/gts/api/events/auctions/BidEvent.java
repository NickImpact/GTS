package me.nickimpact.gts.api.events.auctions;

import com.nickimpact.impactor.api.event.annotations.Param;
import me.nickimpact.gts.api.listings.Listing;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public interface BidEvent {

    @Param(0)
    @NonNull UUID getBidder();

    @Param(1)
    @NonNull Listing getListing();

    @Param(2)
    double getAmountBid();

}
