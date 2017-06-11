package com.nickimpact.GTS.events;

import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;

public class TradeEvent extends BaseEvent {

    private final User player;
    private final User trader;
    private final EntityPixelmon received;
    private final Cause cause;

    public TradeEvent(User player, User trader, EntityPixelmon received, Cause cause){
        this.player = player;
        this.trader = trader;
        this.received = received;
        this.cause = cause;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    public User getPlayer() {
        return player;
    }

    public User getTrader() {
        return trader;
    }

    public EntityPixelmon getReceived() {
        return received;
    }
}
