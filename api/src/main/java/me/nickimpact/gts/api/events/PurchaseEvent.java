package me.nickimpact.gts.api.events;

import lombok.Getter;
import me.nickimpact.gts.api.listings.Listing;

/**
 * This PurchaseEvent represents the action of a Player purchasing any type of listing from the GTS
 * market, whether it be through auction or simplistic purchase. To access the data of the listing,
 * just simply parse through the fields of the listing variable provided by the event. While it may
 * not seem like it, this event does have getter methods thanks to lombok's {@link Getter} annotation.
 *
 * @author NickImpact
 */
public interface PurchaseEvent<T> {

    T getPlayer();

    Listing getListing();

    boolean isCancelled();

    void setCancelled(boolean flag);

}
