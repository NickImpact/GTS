package com.nickimpact.GTS.api.events;

import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class ListEvent extends BaseEvent {

    private final Player player;
    private final EntityPixelmon pokemon;
    private final Cause cause;

    public ListEvent(Player player, EntityPixelmon pokemon, Cause cause){
        this.player = player;
        this.pokemon = pokemon;
        this.cause = cause;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    public Player getPlayer() {
        return player;
    }

    public EntityPixelmon getPokemon() {
        return pokemon;
    }
}
