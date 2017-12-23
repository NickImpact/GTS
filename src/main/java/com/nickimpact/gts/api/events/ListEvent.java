package com.nickimpact.gts.api.events;

import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.pricing.Price;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class ListEvent extends BaseEvent {

    private final Player player;
    private final Listing entry;
    private final Price price;
    private final Cause cause;

    public ListEvent(Player player, Listing entry, Price price, Cause cause){
        this.player = player;
        this.entry = entry;
        this.price = price;
        this.cause = cause;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    public Player getPlayer() {
        return player;
    }

    public Listing getEntry() {
        return entry;
    }

    public Price getPrice() {
    	return this.price;
    }
}
