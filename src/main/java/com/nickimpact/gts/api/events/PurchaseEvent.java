package com.nickimpact.gts.api.events;

import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.pricing.Price;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

/**
 * This PurchaseEvent represents the action of a Player purchasing any type of listing from the GTS
 * market, whether it be through auction or simplistic purchase. To access the data of the listing,
 * just simply parse through the fields of the listing variable provided by the event. While it may
 * not seem like it, this event does have getter methods thanks to lombok's {@link Getter} annotation.
 *
 * @author NickImpact
 */
@Getter
@RequiredArgsConstructor
public class PurchaseEvent extends BaseEvent {

    private final Player player;
    private final Listing listing;
    @NonNull private final Cause cause;

    @Override
    public Cause getCause() {
        return this.cause;
    }
}
