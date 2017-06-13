package com.nickimpact.GTS.events;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

import java.math.BigDecimal;

public class PurchaseEvent extends BaseEvent {

    private final Player player;
    private BigDecimal price;
    private Cause cause;

    public PurchaseEvent(Player player, BigDecimal price, Cause cause){
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
