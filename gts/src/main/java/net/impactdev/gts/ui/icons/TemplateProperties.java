package net.impactdev.gts.ui.icons;

import net.impactdev.gts.ui.icons.functions.IconFunction;
import net.impactdev.impactor.api.items.types.ItemType;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.util.List;

public final class TemplateProperties {

    public ItemType type;
    public String title;
    public List<String> lore;
    public CompoundBinaryTag nbt;
    public IconTemplate.SlotResolver slots;
    public IconFunction function;

    public IconTemplate generate() {
        return new IconTemplate(
                type,
                title,
                lore,
                nbt,
                slots,
                function
        );
    }
}
