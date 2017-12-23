package com.nickimpact.gts.api.events;

import com.nickimpact.gts.api.listings.pricing.Price;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class PurchaseEvent extends BaseEvent {

    private final Player player;
    private Price price;
    private Cause cause;

    public PurchaseEvent(Player player, Price price, Cause cause){
        this.player = player;
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

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }
}
