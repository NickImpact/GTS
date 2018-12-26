package me.nickimpact.gts.api.events;

import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.impl.AbstractEvent;

/**
 * Represents an event for use with the Sponge event manager. These events implement
 * the {@link Cancellable} interface to state that they may declare a function to be
 * cancelled and avoided from executing.
 *
 * @author NickImpact
 */
public abstract class BaseEvent extends AbstractEvent implements Cancellable {

    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
