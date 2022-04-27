package net.impactdev.gts.api.events.auctions;

import net.impactdev.impactor.api.event.ImpactorEvent;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.impactor.api.event.type.Cancellable;
import org.spongepowered.api.util.annotation.eventgen.GenerateFactoryMethod;

import java.util.UUID;

@GenerateFactoryMethod
public interface BidEvent extends ImpactorEvent, Cancellable {

    UUID getBidder();

    Listing getListing();

    double getAmountBid();

}
