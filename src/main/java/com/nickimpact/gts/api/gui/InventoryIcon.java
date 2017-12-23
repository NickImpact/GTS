package com.nickimpact.gts.api.gui;

import org.spongepowered.api.event.item.inventory.AffectSlotEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class InventoryIcon {
    private int slot;
    private ItemStack display;
    private Map<Class<? extends InteractInventoryEvent>, Consumer<? extends InteractInventoryEvent>> listeners;

    public InventoryIcon(int slot, ItemStack display) {
        this.slot = slot;
        this.display = display;
        this.listeners = new HashMap<>();
    }

    /**
     * Returns the slot of this icon.
     *
     * @return The slot of this icon
     */
    public int getSlot() {
        return this.slot;
    }

    /**
     * Sets the slot of this icon
     *
     * @param slot The slot to apply to the icon
     */
    public void setSlot(int slot) {
        this.slot = slot;
    }

    /**
     * Returns the ItemStack display of the icon
     *
     * @return The ItemStack representation for the icon
     */
    public ItemStack getDisplay() {
        return this.display;
    }

    /**
     * Sets the ItemStack of this icon
     *
     * @param display The ItemStack to apply to the icon
     */
    public void setDisplay(ItemStack display) {
        this.display = display;
    }

    /**
     * Returns a copy of all listeners attached to this icon
     *
     * @param <E> An extension of the InteractInventoryEvent
     * @return A mapping of all listeners and their registered functions
     */
    public <E extends InteractInventoryEvent> Map<Class<E>, Consumer<E>> getListeners() {
        return this.listeners.getClass().cast(this.listeners);
    }

    /**
     * Appends a listener which extends the {@link InteractInventoryEvent} to an icon
     *
     * @param clazz The event to listen for
     * @param listener The function of the event
     * @param <E> An extension of the InteractInventoryEvent
     */
    public <E extends InteractInventoryEvent> void addListener(Class<E> clazz, Consumer<E> listener) {
        Consumer<E> consumer = event -> {
            if (event instanceof AffectSlotEvent) {
                AffectSlotEvent slotEvent = (AffectSlotEvent) event;
                for (SlotTransaction transaction : slotEvent.getTransactions()) {
                    Optional<SlotIndex> optIndex = transaction.getSlot().getProperty(SlotIndex.class, "slotindex");
                    if (optIndex.isPresent()) {
                        SlotIndex index = optIndex.get();
                        if (index.getValue() != null && index.getValue().equals(this.getSlot())) {
                            listener.accept(event);
                        }
                    }
                }
            } else {
                listener.accept(event);
            }
        };
        this.listeners.put(clazz, consumer);
    }
}
