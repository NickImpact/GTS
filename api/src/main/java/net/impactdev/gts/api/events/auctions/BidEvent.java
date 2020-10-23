package net.impactdev.gts.api.events.auctions;

import net.impactdev.impactor.api.event.ImpactorEvent;
import net.impactdev.impactor.api.event.annotations.Param;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.impactor.api.event.type.Cancellable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public interface BidEvent extends ImpactorEvent, Cancellable {

    @Param(0)
    @NonNull UUID getBidder();

    @Param(1)
    @NonNull Listing getListing();

    @Param(2)
    double getAmountBid();

}
