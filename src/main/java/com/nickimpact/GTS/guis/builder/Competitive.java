package com.nickimpact.GTS.guis.builder;

import com.google.common.collect.Lists;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.guis.InventoryBase;
import com.nickimpact.GTS.guis.InventoryIcon;
import com.nickimpact.GTS.guis.SharedItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.text.DecimalFormat;
import java.util.List;

public class Competitive extends InventoryBase{

    private Player player;
    private BuilderBase base;
    private int[] evs = new int[]{0, 0, 0, 0, 0, 0};
    private int[] ivs = new int[]{0, 0, 0, 0, 0, 0};

    public Competitive(Player player, BuilderBase base) {
        super(6, Text.of(
                TextColors.RED, "GTS", TextColors.DARK_GRAY, " \u00bb ", TextColors.DARK_GREEN, "EVs/IVs Query"
        ));

        this.player = player;
        this.base = base;
        this.evs = base.evs;
        this.ivs = base.ivs;

        this.setupDisplay();
    }

    private void setupDisplay(){
        for(int y = 0; y < 6; y++){
            this.addIcon(SharedItems.forgeBorderIcon(1 + (9 * y), DyeColors.BLACK));
            this.addIcon(SharedItems.forgeBorderIcon(4 + (9 * y), DyeColors.BLACK));
            this.addIcon(SharedItems.forgeBorderIcon(7 + (9 * y), DyeColors.BLACK));
        }

        this.addIcon(SharedItems.forgeBorderIcon(8, DyeColors.BLACK));
        this.addIcon(SharedItems.forgeBorderIcon(53, DyeColors.BLACK));

        // Stat Icon Display
        this.addIcon(statIcon(0, StatsType.HP));
        this.addIcon(statIcon(9, StatsType.Attack));
        this.addIcon(statIcon(18, StatsType.Defence));
        this.addIcon(statIcon(27, StatsType.SpecialAttack));
        this.addIcon(statIcon(36, StatsType.SpecialDefence));
        this.addIcon(statIcon(45, StatsType.Speed));

        addModifiers();

        InventoryIcon back = new InventoryIcon(17, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:eject_button").orElse(ItemTypes.BARRIER))
                .keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "\u2190 Return to Spec Designer \u2190"))
                .build()
        );
        back.addListener(ClickInventoryEvent.class, e -> {
            this.base.evs = evs;
            this.base.ivs = ivs;

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.player.closeInventory(Cause.of(NamedCause.source(GTS.getInstance())));

                this.base.addIcon(this.base.statsIcon());
                this.base.updateContents();
                this.player.openInventory(this.base.getInventory(), Cause.of(NamedCause.source(GTS.getInstance())));
            }).delayTicks(1).submit(GTS.getInstance());
        });
        this.addIcon(back);

        InventoryIcon reset = SharedItems.cancelIcon(44);
        reset.addListener(ClickInventoryEvent.class, e -> {
            int[] baseStats = new int[]{0, 0, 0, 0, 0, 0};

            if(this.evs == baseStats && this.ivs == baseStats) return;


            this.evs = baseStats;
            this.ivs = baseStats;

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                addModifiers();
                for(int ordinal = 1, slot = 0; ordinal < 7; ordinal++, slot += 9){
                    this.addIcon(this.statIcon(slot, StatsType.values()[ordinal]));
                }
                this.updateContents();
            }).delayTicks(1).submit(GTS.getInstance());
        });
        this.addIcon(reset);
    }

    private InventoryIcon statIcon(int slot, StatsType st){
        String item;
        if(st.equals(StatsType.HP)){
            item = "pixelmon:power_weight";
        } else if(st.equals(StatsType.Attack)){
            item = "pixelmon:power_bracer";
        } else if(st.equals(StatsType.Defence)){
            item = "pixelmon:power_belt";
        } else if(st.equals(StatsType.SpecialAttack)){
            item = "pixelmon:power_lens";
        } else if(st.equals(StatsType.SpecialDefence)){
            item = "pixelmon:power_band";
        } else if(st.equals(StatsType.Speed)){
            item = "pixelmon:power_anklet";
        } else {
            item = "";
        }

        List<Text> lore = Lists.newArrayList(
                Text.of(TextColors.GRAY, st.name() + " Stats:"),
                Text.EMPTY,
                Text.of(TextColors.GRAY, "EVs: ", TextColors.YELLOW, this.evs[this.getIndex(st)]),
                Text.of(TextColors.GRAY, "IVs: ", TextColors.YELLOW, this.ivs[this.getIndex(st)])
        );

        return new InventoryIcon(slot, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, item).orElse(ItemTypes.BARRIER))
                .keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, st.name()))
                .keyValue(Keys.ITEM_LORE, lore)
                .build()
        );
    }

    private InventoryIcon incrementIcon(StatsType st, boolean evs){
        InventoryIcon icon = new InventoryIcon(getSlot(st, evs, true), ItemStack.builder()
                .itemType(ItemTypes.STAINED_HARDENED_CLAY)
                .keyValue(Keys.DYE_COLOR, DyeColors.LIME)
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.GREEN, TextStyles.BOLD, "Add " + st.name() + (evs ? " EVs" : " IVs")
                ))
                .keyValue(Keys.ITEM_LORE, this.getLore(st, evs, true))
                .build()
        );

        icon.addListener(ClickInventoryEvent.class, e -> {
            int max = (evs ? 510 : 186);
            int total = 0;
            if(evs)
                for(int ev : this.evs)
                    total += ev;
            else
                for(int iv : this.ivs)
                    total += iv;

            if(!(e instanceof ClickInventoryEvent.Shift)){
                if(evs) {
                    if (this.evs[this.getIndex(st)] + 1 <= 255 && total + 1 <= max)
                        this.evs[this.getIndex(st)] += 1;
                } else {
                    if (this.ivs[this.getIndex(st)] + 1 <= 31 && total + 1 <= max)
                        this.ivs[this.getIndex(st)] += 1;
                }
            } else {
                if(evs) {
                    if (this.evs[this.getIndex(st)] + 10 <= 255 && total + 10 <= max)
                        this.evs[this.getIndex(st)] += 10;
                } else {
                    if (this.ivs[this.getIndex(st)] + 10 <= 31 && total + 10 <= max)
                        this.ivs[this.getIndex(st)] += 10;
                }
            }

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                addModifiers();
                for(int ordinal = 1, slot = 0; ordinal < 7; ordinal++, slot += 9){
                    this.addIcon(this.statIcon(slot, StatsType.values()[ordinal]));
                }
                this.updateContents();
            }).delayTicks(1).submit(GTS.getInstance());
        });

        return icon;
    }

    private InventoryIcon decrementIcon(StatsType st, boolean evs){
        InventoryIcon icon = new InventoryIcon(getSlot(st, evs, false), ItemStack.builder()
                .itemType(ItemTypes.STAINED_HARDENED_CLAY)
                .keyValue(Keys.DYE_COLOR, DyeColors.RED)
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.RED, TextStyles.BOLD, "Subtract " + st.name() + (evs ? " EVs" : " IVs")
                ))
                .keyValue(Keys.ITEM_LORE, this.getLore(st, evs, false))
                .build()
        );

        icon.addListener(ClickInventoryEvent.class, e -> {
            int total = 0;
            if(evs)
                for(int ev : this.evs)
                    total += ev;
            else
                for(int iv : this.ivs)
                    total += iv;

            if(!(e instanceof ClickInventoryEvent.Shift)){
                if(evs) {
                    if (this.evs[this.getIndex(st)] - 1 >= 0 && total - 1 >= 0)
                        this.evs[this.getIndex(st)] -= 1;
                } else {
                    if (this.ivs[this.getIndex(st)] - 1 >= 0 && total - 1 >= 0)
                        this.ivs[this.getIndex(st)] -= 1;
                }
            } else {
                if(evs) {
                    if (this.evs[this.getIndex(st)] - 10 >= 0 && total - 10 >= 0)
                        this.evs[this.getIndex(st)] -= 10;
                } else {
                    if (this.ivs[this.getIndex(st)] - 10 >= 0 && total - 10 >= 0)
                        this.ivs[this.getIndex(st)] -= 10;
                }
            }

            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                addModifiers();
                for(int ordinal = 1, slot = 0; ordinal < 7; ordinal++, slot += 9){
                    this.addIcon(this.statIcon(slot, StatsType.values()[ordinal]));
                }
                this.updateContents();
            }).delayTicks(1).submit(GTS.getInstance());
        });

        return icon;
    }

    private int getIndex(StatsType st){
        if(st.equals(StatsType.HP))
            return 0;
        else if(st.equals(StatsType.Attack))
            return 1;
        else if(st.equals(StatsType.Defence))
            return 2;
        else if(st.equals(StatsType.SpecialAttack))
            return 3;
        else if(st.equals(StatsType.SpecialDefence))
            return 4;
        else if(st.equals(StatsType.Speed))
            return 5;

        return -1;
    }

    private List<Text> getLore(StatsType st, boolean evs, boolean inc){

        int evTotal = 0;
        for(int ev : this.evs)
            evTotal += ev;

        int ivTotal = 0;
        for(int iv : this.ivs)
            ivTotal += iv;

        DecimalFormat df = new DecimalFormat("#0.##");

        return Lists.newArrayList(
                    Text.of(TextColors.GRAY, "Click here to make an"),
                    Text.of(TextColors.GRAY, "increase to the total"),
                    Text.of(TextColors.GRAY, "amount of " + (evs ? "EV" : "IV") + " points"),
                    Text.of(TextColors.GRAY, "for the ", TextColors.YELLOW, st.name(), TextColors.GRAY, " stat"),
                    Text.EMPTY,
                    Text.of(
                            TextColors.GRAY, "Current: ", TextColors.YELLOW,
                            (evs ? this.evs[this.getIndex(st)] : this.ivs[this.getIndex(st)]),
                            TextColors.GRAY, "/", TextColors.YELLOW,
                            (evs ? "255" : "31"), TextColors.GRAY, " (", TextColors.GREEN,
                            df.format(evs ? (this.evs[this.getIndex(st)] / 510.0 * 100) : (this.ivs[this.getIndex(st)] / 186.0 * 100)) + "%", TextColors.GRAY, ")"
                    ),
                    Text.of(
                            TextColors.GRAY, "Curr Total: ", TextColors.YELLOW,
                            (evs ? evTotal : ivTotal), TextColors.GRAY, "/", TextColors.YELLOW,
                            (evs ? "510" : "186"), TextColors.GRAY, " (", TextColors.GREEN,
                            df.format(evs ? (evTotal / 510.0 * 100) : (ivTotal / 186.0 * 100)) + "%", TextColors.GRAY, ")"
                    ),
                    Text.EMPTY,
                    Text.of(TextColors.GREEN, "Note:"),
                    Text.of("  ", TextColors.GRAY, "Left Click: ", inc ? Text.of(TextColors.GREEN, "+1") : Text.of(TextColors.RED, "-1")),
                    Text.of("  ", TextColors.GRAY, "Left + Shift Click: ", inc ? Text.of(TextColors.GREEN, "+10") : Text.of(TextColors.RED, "-10"))
        );

    }

    private void addModifiers(){
        for(int ordinal = 1; ordinal < 7; ordinal++){
            this.addIcon(incrementIcon(StatsType.values()[ordinal], true));
            this.addIcon(incrementIcon(StatsType.values()[ordinal], false));
            this.addIcon(decrementIcon(StatsType.values()[ordinal], true));
            this.addIcon(decrementIcon(StatsType.values()[ordinal], false));
        }
    }

    private int getSlot(StatsType st, boolean evs, boolean inc){
        if(st.equals(StatsType.HP))
            if(evs)
                if(inc)
                    return 3;
                else
                    return 2;
            else
                if(inc)
                    return 6;
                else
                    return 5;
        else if(st.equals(StatsType.Attack))
            if(evs)
                if(inc)
                    return 12;
                else
                    return 11;
            else
                if(inc)
                    return 15;
                else
                    return 14;
        else if(st.equals(StatsType.Defence))
            if(evs)
                if(inc)
                    return 21;
                else
                    return 20;
            else
                if(inc)
                    return 24;
                else
                    return 23;
        else if(st.equals(StatsType.SpecialAttack))
            if(evs)
                if(inc)
                    return 30;
                else
                    return 29;
            else
                if(inc)
                    return 33;
                else
                    return 32;
        else if(st.equals(StatsType.SpecialDefence))
            if(evs)
                if(inc)
                    return 39;
                else
                    return 38;
            else
                if(inc)
                   return 42;
                else
                    return 41;
        else if(st.equals(StatsType.Speed))
            if(evs)
                if(inc)
                    return 48;
                else
                    return 47;
            else
                if(inc)
                    return 51;
                else
                    return 50;
        return -1;
    }
}
