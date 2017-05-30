package com.nickimpact.GTS.guis.builder;

import com.google.common.collect.Lists;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.GTSInfo;
import com.nickimpact.GTS.guis.InventoryBase;
import com.nickimpact.GTS.guis.InventoryIcon;
import com.nickimpact.GTS.guis.SharedItems;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import net.minecraft.world.World;
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

import java.util.HashMap;
import java.util.List;

public class BuilderBase extends InventoryBase {

    private Player player;
    protected String pokemon;

    int level = 1;
    int form = 0;
    int[] evs = new int[]{0, 0, 0, 0, 0, 0};
    int[] ivs = new int[]{0, 0, 0, 0, 0, 0};
    String ability = "N/A";
    String growth = "N/A";
    String nature = "N/A";
    String gender = "N/A";
    String particle = "N/A";
    boolean shiny = false;

    private List<Text> lore = Lists.newArrayList(
            Text.of(TextColors.GRAY, "Please use the functions to the"),
            Text.of(TextColors.GRAY, "right to further modify the"),
            Text.of(TextColors.GRAY, "pokemon you have requested."),
            Text.EMPTY,
            Text.of(TextColors.GRAY, "Once you are satisfied, click"),
            Text.of(TextColors.GRAY, "the ", TextColors.GREEN, "green ", TextColors.GRAY, "dye to confirm"),
            Text.of(TextColors.GRAY, "your request query.")
    );

    public BuilderBase(Player player, String pokemon, HashMap<String, Object> specs) {
        super(5, Text.of(
                TextColors.RED, "GTS", TextColors.DARK_GRAY, " \u00bb ", TextColors.DARK_GREEN, "Spec Designer"
        ));

        this.player = player;
        this.pokemon = pokemon;
        fillSpecs(specs);

        List<Text> debug = Lists.newArrayList(
                Text.of("Spec Designer (Phase 1):"),
                Text.of("  Pokemon: " + this.pokemon),
                Text.of("  Specs:"),
                Text.of("    Level: " + this.level),
                Text.of("    Ability: " + this.ability),
                Text.of("    Growth: " + this.growth),
                Text.of("    Nature: " + this.nature),
                Text.of("    Gender: " + this.gender),
                Text.of("    Shiny: " + this.shiny),
                Text.of("    Particle: " + this.particle),
                Text.of("    Form: " + this.form),
                Text.of("    EVs/IVs:"),
                Text.of("      HP: " + this.evs[0] + " | " + this.ivs[0]),
                Text.of("      Atk: " + this.evs[1] + " | " + this.ivs[1]),
                Text.of("      Def: " + this.evs[2] + " | " + this.ivs[2]),
                Text.of("      SpAtk: " + this.evs[3] + " | " + this.ivs[3]),
                Text.of("      SpDef: " + this.evs[4] + " | " + this.ivs[4]),
                Text.of("      Speed: " + this.evs[5] + " | " + this.ivs[5])
        );

        for (Text text : debug) {
            GTS.getInstance().getConsole().sendMessage(Text.of(
                    GTSInfo.DEBUG_PREFIX, text
            ));
        }

        setupDesign();
    }

    private void setupDesign() {
        for (int x = 0, y = 0; y < 5; x++) {
            if(x > 8){
                x = 0;
                y += 4;
            }
            if(y >= 5) break;

            this.addIcon(SharedItems.forgeBorderIcon(x + (9 * y), DyeColors.BLACK));
        }
        for (int x = 1, y = 0; y < 4; x += 6) {
            if (x > 7) {
                x = 1;
                y++;
            }
            if(y >= 4) break;

            this.addIcon(SharedItems.forgeBorderIcon(x + (9 * y), DyeColors.BLACK));
        }

        this.addIcon(new InventoryIcon(18, ItemStack.builder().from(SharedItems.pokemonDisplay(
                    (EntityPixelmon) PixelmonEntityList.createEntityByName(this.pokemon, (World) this.player.getWorld()), this.form)
                )
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.YELLOW, TextStyles.BOLD, EnumPokemon.getFromNameAnyCase(this.pokemon).name()
                ))
                .keyValue(Keys.ITEM_LORE, this.lore)
                .build()
        ));

        InventoryIcon confirm = SharedItems.confirmIcon(17);
        confirm.addListener(ClickInventoryEvent.class, e -> {
            // TODO - Add listing to market, close inventory
        });
        this.addIcon(confirm);

        InventoryIcon deny = SharedItems.denyIcon(35);
        deny.addListener(ClickInventoryEvent.class, e -> {
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                this.player.closeInventory(Cause.of(NamedCause.source(GTS.getInstance())));
            })
            .delayTicks(1)
            .submit(GTS.getInstance());
        });
        this.addIcon(deny);

        this.addIcon(levelIcon());
        this.addIcon(abilityIcon());
        this.addIcon(natureIcon());
        //this.addIcon(modifierIcon(15, "pixelmon:ever_stone", 0, "Natures", "nature"));
        //this.addIcon(modifierIcon(20, "pixelmon:destiny_knot", 0, "EVs / IVs", "minimum EVs / IVs"));
        //this.addIcon(modifierIcon(22, "pixelmon:rose_incense", 0, "Genders", "gender"));
        //this.addIcon(modifierIcon(24, "minecraft:dye", 15, "Genders", "gender"));
        //this.addIcon(modifierIcon(29, "minecraft:nether_star", 0, "Shininess", "shininess"));
        //this.addIcon(modifierIcon(31, "minecraft:prismarine_crystals", 0, "Particles", "particle"));
        //this.addIcon(modifierIcon(33, "pixelmon:meteorite", 0, "Forms", "form"));
    }

    InventoryIcon levelIcon(){
        InventoryIcon icon = new InventoryIcon(11, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:rare_candy").orElse(ItemTypes.BARRIER))
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_AQUA, TextStyles.BOLD, "Levels"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Click here if you wish to"),
                        Text.of(TextColors.GRAY, "modify the ", TextColors.YELLOW, "minimum level"),
                        Text.of(TextColors.GRAY, "requirement for your query"),
                        Text.EMPTY,
                        Text.of(TextColors.GRAY, "Query: ", TextColors.YELLOW, (level < PixelmonConfig.maxLevel ? level + "+" : level))
                ))
                .build()
        );
        icon.addListener(ClickInventoryEvent.class, e -> {
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                player.closeInventory(Cause.of(NamedCause.source(GTS.getInstance())));

                player.openInventory(new Levels(this.player, this).getInventory(), Cause.of(NamedCause.source(GTS.getInstance())));
            })
            .delayTicks(1)
            .submit(GTS.getInstance());
        });
        return icon;
    }

    InventoryIcon abilityIcon(){
        InventoryIcon icon = new InventoryIcon(13, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:ability_capsule").orElse(ItemTypes.BARRIER))
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_AQUA, TextStyles.BOLD, "Abilities"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Click here if you wish to"),
                        Text.of(TextColors.GRAY, "modify the ", TextColors.YELLOW, "ability ", TextColors.GRAY, "requirement"),
                        Text.of(TextColors.GRAY, "for your query"),
                        Text.EMPTY,
                        Text.of(TextColors.GRAY, "Query: ", TextColors.YELLOW, this.ability)
                ))
                .build()
        );
        icon.addListener(ClickInventoryEvent.class, e -> {
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                player.closeInventory(Cause.of(NamedCause.source(GTS.getInstance())));

                player.openInventory(new Ability(this.player, this).getInventory(), Cause.of(NamedCause.source(GTS.getInstance())));
            })
            .delayTicks(1)
            .submit(GTS.getInstance());
        });
        return icon;
    }

    InventoryIcon natureIcon(){
        InventoryIcon icon = new InventoryIcon(15, ItemStack.builder()
                .itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:ever_stone").orElse(ItemTypes.BARRIER))
                .keyValue(Keys.DISPLAY_NAME, Text.of(
                        TextColors.DARK_AQUA, TextStyles.BOLD, "Natures"
                ))
                .keyValue(Keys.ITEM_LORE, Lists.newArrayList(
                        Text.of(TextColors.GRAY, "Click here if you wish to"),
                        Text.of(TextColors.GRAY, "modify the ", TextColors.YELLOW, "nature ", TextColors.GRAY, "requirement"),
                        Text.of(TextColors.GRAY, "for your query"),
                        Text.EMPTY,
                        Text.of(TextColors.GRAY, "Query: ", TextColors.YELLOW, this.nature)
                ))
                .build()
        );
        icon.addListener(ClickInventoryEvent.class, e -> {
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                player.closeInventory(Cause.of(NamedCause.source(GTS.getInstance())));

                player.openInventory(new Nature(this.player, this).getInventory(), Cause.of(NamedCause.source(GTS.getInstance())));
            })
            .delayTicks(1)
            .submit(GTS.getInstance());
        });
        return icon;
    }

    private void fillSpecs(HashMap<String, Object> specs) {
        for (String key : specs.keySet()) {
            switch (key) {
                case "lvl":
                case "level":
                    this.level = (Integer) (specs.get(key));
                    break;
                case "ab":
                case "ability":
                    this.ability = String.valueOf(specs.get(key));
                    break;
                case "na":
                case "nature":
                    this.nature = String.valueOf(specs.get(key));
                    break;
                case "evHP":
                    this.evs[0] = (Integer) specs.get(key);
                    break;
                case "evAtk":
                    this.evs[1] = (Integer) specs.get(key);
                    break;
                case "evDef":
                    this.evs[2] = (Integer) specs.get(key);
                    break;
                case "evSpAtk":
                    this.evs[3] = (Integer) specs.get(key);
                    break;
                case "evSpDef":
                    this.evs[4] = (Integer) specs.get(key);
                    break;
                case "evSpeed":
                    this.evs[5] = (Integer) specs.get(key);
                    break;
                case "ivHP":
                    this.ivs[0] = (Integer) specs.get(key);
                    break;
                case "ivAtk":
                    this.ivs[1] = (Integer) specs.get(key);
                    break;
                case "ivDef":
                    this.ivs[2] = (Integer) specs.get(key);
                    break;
                case "ivSpAtk":
                    this.ivs[3] = (Integer) specs.get(key);
                    break;
                case "ivSpDef":
                    this.ivs[4] = (Integer) specs.get(key);
                    break;
                case "ivSpeed":
                    this.ivs[5] = (Integer) specs.get(key);
                    break;
                case "f":
                case "form":
                    this.form = (Integer) specs.get(key);
                    break;
                case "size":
                case "growth":
                    this.growth = String.valueOf(specs.get(key));
                    break;
                case "s":
                case "shiny":
                    this.shiny = (Boolean) specs.get(key);
                    break;
                case "p":
                case "particle":
                    this.particle = String.valueOf(specs.get(key));
                    break;
                case "ge":
                case "gender":
                    this.gender = String.valueOf(specs.get(key));
                    break;
            }
        }
    }
}
