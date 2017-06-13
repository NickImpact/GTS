package com.nickimpact.GTS.guis.builder;

import com.google.common.collect.Lists;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.guis.InventoryBase;
import com.nickimpact.GTS.guis.InventoryIcon;
import com.nickimpact.GTS.guis.SharedItems;
import com.nickimpact.GTS.utils.LotUtils;
import com.pixelmonmod.pixelmon.enums.forms.EnumUnown;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

public class Form extends InventoryBase {

    private Player player;
    private BuilderBase base;
    private int form;
    private int slot;
    private int totalForms;

    public Form(Player player, BuilderBase base, int size) {
        super(size, Text.of(
                TextColors.RED, "GTS", TextColors.DARK_GRAY, " \u00bb ", TextColors.DARK_GREEN, "Form Query"
        ));

        this.player = player;
        this.base = base;
        this.form = base.form;
        this.totalForms = base.pokemon.getNumForms();

        this.setupDisplay(size);
    }

    private void setupDisplay(int size) {
        if(size == 5){
            mainDisplay();
        } else {
            unownDisplay();
        }
    }

    private void mainDisplay(){
        this.drawBorder(5, DyeColors.BLACK);

        for(int x = 1, y = 2; x < 8; x++){
            this.addIcon(SharedItems.forgeBorderIcon(x + (9 * y), DyeColors.BLACK));
        }

        this.addIcon(SharedItems.forgeBorderIcon(13, DyeColors.BLACK));

        this.addIcon(selectedIcon(10));
        this.addIcon(mapIcon(12));
        this.addIcon(backIcon(14));
        this.addIcon(resetIcon(16, false));

        int numForms = base.pokemon.getNumForms();
        if(numForms < 5){
            int start;
            if(numForms == 2) start = 30;
            else if(numForms == 3) start = 29;
            else start = 28;

            for(int form = 0; form < numForms; form++)
                this.addIcon(this.formIcon(form * 2 + start, form, false));
        } else {
            for(int form = 0; form < numForms; form++)
                this.addIcon(this.formIcon(form + 28, form, false));
        }
    }

    private void unownDisplay(){
        for(int y = 0; y < 6; y++){
            this.addIcon(SharedItems.forgeBorderIcon(1 + (9 * y), DyeColors.BLACK));
            this.addIcon(SharedItems.forgeBorderIcon(7 + (9 * y), DyeColors.BLACK));
        }

        this.addIcon(SharedItems.forgeBorderIcon(0, DyeColors.BLACK));
        this.addIcon(SharedItems.forgeBorderIcon(8, DyeColors.BLACK));
        this.addIcon(SharedItems.forgeBorderIcon(45, DyeColors.BLACK));
        this.addIcon(SharedItems.forgeBorderIcon(53, DyeColors.BLACK));

        this.addIcon(selectedIcon(9));
        this.addIcon(mapIcon(36));
        this.addIcon(backIcon(17));
        this.addIcon(resetIcon(44, true));

        int x = 2, ordinal = 0;
        for(int y = 0; y < 6; y++){
            for(; x < 7 && ordinal < EnumUnown.values().length; x++, ordinal++){
                this.addIcon(this.formIcon(x + (9 * y), ordinal, true));
            }
            x = 2;
        }
    }

    private InventoryIcon formIcon(int slot, int ordinal, boolean unown){
        InventoryIcon icon = new InventoryIcon(slot, SharedItems.pokemonDisplay(base.pokemon, ordinal));
        icon.getDisplay().offer(Keys.DISPLAY_NAME, Text.of(
                TextColors.DARK_AQUA, base.pokemon.getName(), TextColors.GRAY, " (", TextColors.YELLOW,
                LotUtils.capitalize(SpriteHelper.getSpriteExtra(base.pokemon.getName(), ordinal).substring(1)), TextColors.GRAY, ")"
        ));

        if(ordinal == this.form){
            this.slot = slot;
            icon.getDisplay().offer(Keys.ITEM_ENCHANTMENTS, Lists.newArrayList(
                    new ItemEnchantment(Enchantments.UNBREAKING, 0)
            ));
            icon.getDisplay().offer(Keys.HIDE_ENCHANTMENTS, true);
        }

        icon.addListener(ClickInventoryEvent.class, e -> {

            int oldForm = this.form;
            int oldSlot = this.slot;
            this.form = ordinal;
            this.slot = slot;

            if(oldForm != -1){
                this.addIcon(this.formIcon(oldSlot, oldForm, unown));
            }

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.addIcon(this.formIcon(slot, ordinal, unown));
                this.addIcon(selectedIcon(unown ? 9 : 10));
                this.updateContents();
            }).delayTicks(1).submit(GTS.getInstance());

        });

        return icon;
    }

    private InventoryIcon selectedIcon(int slot) {
        InventoryIcon icon = new InventoryIcon(slot, SharedItems.pokemonDisplay(base.pokemon, this.form));
        icon.getDisplay().offer(Keys.DISPLAY_NAME, Text.of(
                TextColors.DARK_AQUA, "Selected Form"
        ));
        icon.getDisplay().offer(Keys.ITEM_LORE, Lists.newArrayList(
                Text.of(TextColors.GRAY, "Current: ", TextColors.YELLOW,
                        LotUtils.capitalize(SpriteHelper.getSpriteExtra(base.pokemon.getName(), this.form).substring(1))
                )
        ));

        return icon;
    }

    private InventoryIcon mapIcon(int slot) {
        return new InventoryIcon(slot, ItemStack.builder()
                .itemType(ItemTypes.FILLED_MAP)
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_AQUA, TextStyles.BOLD, "Form Info"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "To pick a form to search"),
                        Text.of(TextColors.GRAY, "for, simply click the representative"),
                        Text.of(TextColors.GRAY, "form display of the pokemon")
                ))
                .build()
        );
    }

    private InventoryIcon backIcon(int slot){
        InventoryIcon back = new InventoryIcon(slot, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").orElse(ItemTypes.BARRIER))
                .keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "\u2190 Return to Spec Designer \u2190"))
                .build()
        );
        back.addListener(ClickInventoryEvent.class, e -> {
            this.base.form = form;

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.player.closeInventory(Cause.of(NamedCause.source(GTS.getInstance())));

                this.base.addIcon(this.base.formIcon());
                this.base.updateContents();
                this.player.openInventory(this.base.getInventory(), Cause.of(NamedCause.source(GTS.getInstance())));
            }).delayTicks(1).submit(GTS.getInstance());
        });

        return back;
    }

    private InventoryIcon resetIcon(int slot, boolean unown){
        InventoryIcon reset = SharedItems.cancelIcon(slot);
        reset.addListener(ClickInventoryEvent.class, e -> {
            if(this.form == -1) return;

            int oldForm = this.form;
            this.form = -1;

            if(unown){
                this.addIcon(formIcon(getUnownSlot(oldForm), oldForm, true));
            } else {
                this.addIcon(formIcon(getSlot(oldForm), oldForm, false));
            }

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.addIcon(selectedIcon(unown ? 9 : 10));
                updateContents();
            }).delayTicks(1).submit(GTS.getInstance());
        });

        return reset;
    }

    private int getSlot(int form){
        if(this.totalForms == 2) return form * 2 + 30;
        if(this.totalForms == 3) return form * 2 + 29;
        if(this.totalForms == 4) return form * 2 + 28;
        else return form + 28;
    }

    private int getUnownSlot(int form){
        if(form >= 25){
            return form + 22;
        } else if(form >= 20){
            return form + 18;
        } else if(form >= 15){
            return form + 14;
        } else if(form >= 10){
            return form + 10;
        } else if(form >= 5){
            return form + 6;
        } else {
            return form + 2;
        }
    }
}
