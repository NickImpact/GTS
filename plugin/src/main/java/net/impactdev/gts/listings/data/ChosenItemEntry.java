package net.impactdev.gts.listings.data;

import net.impactdev.gts.api.listings.ui.EntrySelection;
import net.impactdev.gts.listings.SpongeItemEntry;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class ChosenItemEntry implements EntrySelection<SpongeItemEntry> {

    private final ItemStackSnapshot selection;
    private final int slot;

    public ChosenItemEntry(ItemStackSnapshot selection, int slot) {
        this.selection = selection;
        this.slot = slot;
    }

    public ItemStackSnapshot getSelection() {
        return this.selection;
    }

    public int getSlot() {
        return this.slot;
    }

    @Override
    public SpongeItemEntry createFromSelection() {
        return new SpongeItemEntry(this.selection, this.slot);
    }

}
