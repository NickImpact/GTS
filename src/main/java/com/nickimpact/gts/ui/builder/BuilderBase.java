package com.nickimpact.gts.ui.builder;

import java.util.HashMap;
import java.util.List;

import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.gui.InventoryBase;
import com.nickimpact.gts.api.gui.InventoryIcon;
import com.nickimpact.gts.trades.PokeRequest;
import com.nickimpact.gts.ui.shared.SharedItems;
import com.nickimpact.gts.utils.LotUtils;
import com.nickimpact.gts.utils.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;

import net.minecraft.world.World;

public class BuilderBase extends InventoryBase {

	private Player player;

	EntityPixelmon pokemon;
	String name;
	int slot;
	String note;
	boolean expires;
	long time;

	int level = 1;
	int form = -1;
	int[] evs = new int[]{0, 0, 0, 0, 0, 0};
	int[] ivs = new int[]{0, 0, 0, 0, 0, 0};
	String ability = "N/A";
	String growth = "N/A";
	String nature = "N/A";
	String gender = "N/A";
	String pokeball = "N/A";
	private boolean shiny = false;

	private List<Text> lore = Lists.newArrayList(
			Text.of(TextColors.GRAY, "Please use the functions to the"),
			Text.of(TextColors.GRAY, "right to further modify the"),
			Text.of(TextColors.GRAY, "pokemon you have requested."),
			Text.EMPTY,
			Text.of(TextColors.GRAY, "Once you are satisfied, click"),
			Text.of(TextColors.GRAY, "the ", TextColors.GREEN, "green ", TextColors.GRAY, "dye to confirm"),
			Text.of(TextColors.GRAY, "your request query.")
			);

	public BuilderBase(Player player,
			String name,
			HashMap<String, Object> specs,
			int slot,
			String note,
			boolean expires,
			long time) {
		super(player, 5, Text.of(
				TextColors.RED, "gts", TextColors.DARK_GRAY, " \u00bb ", TextColors.DARK_GREEN, "Spec Designer"
				));

		this.player = player;
		this.name = name;
		this.pokemon = (EntityPixelmon) PixelmonEntityList.createEntityByName(this.name,
				(World) this.player.getWorld());
		this.slot = slot;
		this.note = note;
		this.expires = expires;
		this.time = time;

		fillSpecs(specs);

		setupDesign();
	}

	private void setupDesign() {
		for (int x = 0, y = 0; y < 5; x++) {
			if (x > 8) {
				x = 0;
				y += 4;
			}
			if (y >= 5) break;

			this.addIcon(SharedItems.forgeBorderIcon(x + (9 * y), DyeColors.BLACK));
		}
		for (int x = 1, y = 0; y < 4; x += 6) {
			if (x > 7) {
				x = 1;
				y++;
			}
			if (y >= 4) break;

			this.addIcon(SharedItems.forgeBorderIcon(x + (9 * y), DyeColors.BLACK));
		}

		this.addIcon(new InventoryIcon(18, ItemStack.builder().from(SharedItems.pokemonDisplay(
				pokemon, this.form)
				)
				.keyValue(Keys.DISPLAY_NAME, Text.of(
						TextColors.YELLOW, TextStyles.BOLD, EnumPokemon.getFromNameAnyCase(this.name).name()
						))
				.keyValue(Keys.ITEM_LORE, this.lore)
				.build()
				));

		InventoryIcon confirm = SharedItems.confirmIcon(17);
		confirm.addListener(ClickInventoryEvent.class, e -> {
			Sponge.getScheduler().createTaskBuilder().execute(() -> {
				player.closeInventory();

				PokeRequest request = PokeRequest.builder()
						.pokemon(this.name)
						.ability(this.ability)
						.growth(this.growth)
						.gender(this.gender)
						.nature(this.nature)
						.pokeball(this.pokeball)
						.level(this.level)
						.form(this.form)
						.evs(this.evs)
						.ivs(this.ivs)
						.shiny(this.shiny)
						.build();
				//LotUtils.addPokemon4Pokemon(this.player, this.slot, this.note, request, this.expires, this.time);
			}).delayTicks(1).submit(GTS.getInstance());
		});
		this.addIcon(confirm);

		InventoryIcon deny = SharedItems.denyIcon(35);
		deny.addListener(ClickInventoryEvent.class, e -> {
			Sponge.getScheduler().createTaskBuilder().execute(() -> {
				this.player.closeInventory();
			})
			.delayTicks(1)
			.submit(GTS.getInstance());
		});
		this.addIcon(deny);

		this.addIcon(levelIcon());
		this.addIcon(abilityIcon());
		this.addIcon(natureIcon());
		this.addIcon(statsIcon());
		this.addIcon(genderIcon(pokemon.gender != com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender.None));
		this.addIcon(growthIcon());
		this.addIcon(shinyIcon());
		this.addIcon(pokeballIcon());
		this.addIcon(formIcon());
	}

	InventoryIcon levelIcon() {
		InventoryIcon icon = new InventoryIcon(11, ItemStack.builder()
				.itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:rare_candy").orElse(ItemTypes.BARRIER))
				.keyValue(Keys.DISPLAY_NAME, Text.of(
						TextColors.DARK_AQUA, TextStyles.BOLD, "LEVEL"
						))
				.keyValue(Keys.ITEM_LORE, Lists.newArrayList(
						Text.of(TextColors.GRAY, "Click here if you wish to"),
						Text.of(TextColors.GRAY, "modify the ", TextColors.YELLOW, "minimum level"),
						Text.of(TextColors.GRAY, "requirement for your query"),
						Text.EMPTY,
						Text.of(TextColors.GRAY, "Query: ", TextColors.YELLOW,
								(level < PixelmonConfig.maxLevel ? level + "+" : level))
						))
				.build()
				);
		icon.addListener(ClickInventoryEvent.class, e -> {
			Sponge.getScheduler().createTaskBuilder().execute(() -> {
				player.closeInventory();

				player.openInventory(new Levels(this.player, this).getInventory());
			}).delayTicks(1).submit(GTS.getInstance());
		});
		return icon;
	}

	InventoryIcon abilityIcon() {
		InventoryIcon icon = new InventoryIcon(13, ItemStack.builder()
				.itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:ability_capsule").orElse(
						ItemTypes.BARRIER))
				.keyValue(Keys.DISPLAY_NAME, Text.of(
						TextColors.DARK_AQUA, TextStyles.BOLD, "ABILITY"
						))
				.keyValue(Keys.ITEM_LORE, Lists.newArrayList(
						Text.of(TextColors.GRAY, "Click here if you wish to"),
						Text.of(TextColors.GRAY, "modify the ", TextColors.YELLOW, "ability ", TextColors.GRAY,
								"requirement"),
						Text.of(TextColors.GRAY, "for your query"),
						Text.EMPTY,
						Text.of(TextColors.GRAY, "Query: ", TextColors.YELLOW, this.ability)
						))
				.build()
				);
		icon.addListener(ClickInventoryEvent.class, e -> {
			Sponge.getScheduler().createTaskBuilder().execute(() -> {
				player.closeInventory();

				player.openInventory(new Ability(this.player, this).getInventory());
			}).delayTicks(1).submit(GTS.getInstance());
		});
		return icon;
	}

	InventoryIcon natureIcon() {
		InventoryIcon icon = new InventoryIcon(15, ItemStack.builder()
				.itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:ever_stone").orElse(ItemTypes.BARRIER))
				.keyValue(Keys.DISPLAY_NAME, Text.of(
						TextColors.DARK_AQUA, TextStyles.BOLD, "NATURE"
						))
				.keyValue(Keys.ITEM_LORE, Lists.newArrayList(
						Text.of(TextColors.GRAY, "Click here if you wish to"),
						Text.of(TextColors.GRAY, "modify the ", TextColors.YELLOW, "nature ", TextColors.GRAY,
								"requirement"),
						Text.of(TextColors.GRAY, "for your query"),
						Text.EMPTY,
						Text.of(TextColors.GRAY, "Query: ", TextColors.YELLOW, this.nature)
						))
				.build()
				);
		icon.addListener(ClickInventoryEvent.class, e -> {
			Sponge.getScheduler().createTaskBuilder().execute(() -> {
				player.closeInventory();

				player.openInventory(new Nature(this.player, this).getInventory());
			}).delayTicks(1).submit(GTS.getInstance());
		});
		return icon;
	}

	InventoryIcon statsIcon() {
		InventoryIcon icon = new InventoryIcon(20, ItemStack.builder()
				.itemType(
						Sponge.getRegistry().getType(ItemType.class, "pixelmon:destiny_knot").orElse(ItemTypes.BARRIER))
				.keyValue(Keys.DISPLAY_NAME, Text.of(
						TextColors.DARK_AQUA, TextStyles.BOLD, "EVs/IVs"
						))
				.keyValue(Keys.ITEM_LORE, Lists.newArrayList(
						Text.of(TextColors.GRAY, "Click here if you wish to"),
						Text.of(TextColors.GRAY, "modify the ", TextColors.YELLOW, "EVs/IVs ", TextColors.GRAY,
								"requirement"),
						Text.of(TextColors.GRAY, "for your query"),
						Text.EMPTY,
						Text.of(TextColors.GRAY, "Query: "),
						Text.of("  ", TextColors.GRAY, "HP: ", TextColors.YELLOW, this.evs[0], TextColors.GRAY, "/",
								TextColors.YELLOW, this.ivs[0]),
						Text.of("  ", TextColors.GRAY, "Atk: ", TextColors.YELLOW, this.evs[1], TextColors.GRAY, "/",
								TextColors.YELLOW, this.ivs[1]),
						Text.of("  ", TextColors.GRAY, "Def: ", TextColors.YELLOW, this.evs[2], TextColors.GRAY, "/",
								TextColors.YELLOW, this.ivs[2]),
						Text.of("  ", TextColors.GRAY, "SpAtk: ", TextColors.YELLOW, this.evs[3], TextColors.GRAY, "/",
								TextColors.YELLOW, this.ivs[3]),
						Text.of("  ", TextColors.GRAY, "SpDef: ", TextColors.YELLOW, this.evs[4], TextColors.GRAY, "/",
								TextColors.YELLOW, this.ivs[4]),
						Text.of("  ", TextColors.GRAY, "Speed: ", TextColors.YELLOW, this.evs[5], TextColors.GRAY, "/",
								TextColors.YELLOW, this.ivs[5])
						))
				.build()
				);
		icon.addListener(ClickInventoryEvent.class, e -> {
			Sponge.getScheduler().createTaskBuilder().execute(() -> {
				player.closeInventory();

				player.openInventory(new Competitive(this.player, this).getInventory());
			}).delayTicks(1).submit(GTS.getInstance());
		});
		return icon;
	}

	InventoryIcon genderIcon(boolean valid) {
		InventoryIcon icon;

		if (valid) {
			icon = new InventoryIcon(22, ItemStack.builder()
					.itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:rose_incense").orElse(
							ItemTypes.BARRIER))
					.keyValue(Keys.DISPLAY_NAME, Text.of(
							TextColors.DARK_AQUA, TextStyles.BOLD, "GENDER"
							))
					.keyValue(Keys.ITEM_LORE, Lists.newArrayList(
							Text.of(TextColors.GRAY, "Click here if you wish to"),
							Text.of(TextColors.GRAY, "modify the ", TextColors.YELLOW, "gender ", TextColors.GRAY,
									"requirement"),
							Text.of(TextColors.GRAY, "for your query"),
							Text.EMPTY,
							Text.of(TextColors.GRAY, "Query: ", TextColors.YELLOW, this.gender)
							))
					.build()
					);
			icon.addListener(ClickInventoryEvent.class, e -> {
				Sponge.getScheduler().createTaskBuilder().execute(() -> {
					player.closeInventory();

					player.openInventory(new Gender(this.player, this).getInventory());
				}).delayTicks(1).submit(GTS.getInstance());
			});
		} else {
			icon = new InventoryIcon(22, ItemStack.builder()
					.itemType(ItemTypes.BARRIER)
					.keyValue(Keys.DISPLAY_NAME, Text.of(
							TextColors.DARK_AQUA, TextStyles.BOLD, "GENDER"
							))
					.keyValue(Keys.ITEM_LORE, Lists.newArrayList(
							Text.of(TextColors.RED, "No possible genders...")
							))
					.build()
					);
		}
		return icon;
	}

	InventoryIcon growthIcon() {
		InventoryIcon icon = new InventoryIcon(24, ItemStack.builder()
				.itemType(ItemTypes.DYE)
				.keyValue(Keys.DYE_COLOR, DyeColors.WHITE)
				.keyValue(Keys.DISPLAY_NAME, Text.of(
						TextColors.DARK_AQUA, TextStyles.BOLD, "GROWTH"
						))
				.keyValue(Keys.ITEM_LORE, Lists.newArrayList(
						Text.of(TextColors.GRAY, "Click here if you wish to"),
						Text.of(TextColors.GRAY, "modify the ", TextColors.YELLOW, "growth ", TextColors.GRAY,
								"requirement"),
						Text.of(TextColors.GRAY, "for your query"),
						Text.EMPTY,
						Text.of(TextColors.GRAY, "Query: ", TextColors.YELLOW, this.growth)
						))
				.build()
				);
		icon.addListener(ClickInventoryEvent.class, e -> {
			Sponge.getScheduler().createTaskBuilder().execute(() -> {
				player.closeInventory();

				player.openInventory(new Growth(this.player, this).getInventory());
			}).delayTicks(1).submit(GTS.getInstance());
		});
		return icon;
	}

	private InventoryIcon shinyIcon() {
		InventoryIcon icon = new InventoryIcon(29, ItemStack.builder()
				.itemType(ItemTypes.NETHER_STAR)
				.keyValue(Keys.DISPLAY_NAME, Text.of(
						TextColors.DARK_AQUA, TextStyles.BOLD, "Shininess"
						))
				.keyValue(Keys.ITEM_LORE, Lists.newArrayList(
						Text.of(TextColors.GRAY, "Click here if you wish to"),
						Text.of(TextColors.GRAY, "modify the ", TextColors.YELLOW, "shininess ", TextColors.GRAY,
								"requirement"),
						Text.of(TextColors.GRAY, "for your query"),
						Text.EMPTY,
						Text.of(TextColors.GRAY, "Query: ", TextColors.YELLOW,
								String.valueOf(this.shiny).replace('t', 'T').replace('f', 'F'))
						))
				.build()
				);
		icon.addListener(ClickInventoryEvent.class, e -> {
			this.shiny = !this.shiny;

			Sponge.getScheduler().createTaskBuilder().execute(() -> {
				this.addIcon(shinyIcon());
				this.updateContents();
			}).delayTicks(1).submit(GTS.getInstance());
		});

		return icon;
	}

	InventoryIcon pokeballIcon() {
		InventoryIcon icon = new InventoryIcon(31, ItemStack.builder()
				.itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:poke_ball").orElse(ItemTypes.BARRIER))
				.keyValue(Keys.DISPLAY_NAME, Text.of(
						TextColors.DARK_AQUA, TextStyles.BOLD, "Pokeball"
						))
				.keyValue(Keys.ITEM_LORE, Lists.newArrayList(
						Text.of(TextColors.GRAY, "Click here if you wish to"),
						Text.of(TextColors.GRAY, "modify the ", TextColors.YELLOW, "pokeball ", TextColors.GRAY,
								"requirement"),
						Text.of(TextColors.GRAY, "for your query"),
						Text.EMPTY,
						Text.of(TextColors.GRAY, "Query: ", TextColors.YELLOW, (this.pokeball.equals("N/A") ?
								this.pokeball :
									this.pokeball.substring(0, this.pokeball.indexOf("Ball")) + " Ball")
								)
						))
				.build()
				);
		icon.addListener(ClickInventoryEvent.class, e -> {
			Sponge.getScheduler().createTaskBuilder().execute(() -> {
				player.closeInventory();

				player.openInventory(new Pokeball(this.player, this).getInventory());
			}).delayTicks(1).submit(GTS.getInstance());
		});
		return icon;
	}

	InventoryIcon formIcon() {
		InventoryIcon icon = new InventoryIcon(33, ItemStack.builder()
				.itemType(this.pokemon.getNumForms() > 0 ?
						Sponge.getRegistry().getType(ItemType.class, "pixelmon:meteorite").orElse(ItemTypes.BARRIER) :
							ItemTypes.BARRIER
						)
				.keyValue(Keys.DISPLAY_NAME, Text.of(
						TextColors.DARK_AQUA, TextStyles.BOLD, "FORM"
						))
				.keyValue(Keys.ITEM_LORE,
						this.pokemon.getNumForms() > 0 ?
								Lists.newArrayList(
										Text.of(TextColors.GRAY, "Click here if you wish to"),
										Text.of(TextColors.GRAY, "modify the ", TextColors.YELLOW, "form ",
												TextColors.GRAY, "requirement"),
										Text.of(TextColors.GRAY, "for your query"),
										Text.EMPTY,
										Text.of(TextColors.GRAY, "Query: ", TextColors.YELLOW,
												this.form == -1 ? "N/A" :
													StringUtils.capitalize(SpriteHelper.getSpriteExtra(this.name, this.form).substring(1))
												)
										) :
											Lists.newArrayList(
													Text.of(TextColors.RED, "The pokemon has no forms")
													)
						)
				.build()
				);

		if (this.pokemon.getNumForms() > 0)
			icon.addListener(ClickInventoryEvent.class, e -> {
				Sponge.getScheduler().createTaskBuilder().execute(() -> {
					player.closeInventory();

					player.openInventory(new Form(this.player, this, getNumRows(this.pokemon)).getInventory());
				}).delayTicks(1).submit(GTS.getInstance());
			});
		return icon;
	}

	private int getNumRows(EntityPixelmon pokemon) {
		if (pokemon.getNumForms() > 0 && !pokemon.getName().equalsIgnoreCase("Unown")) return 5;

		return 6;
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
			case "pb":
			case "pokeball":
				this.pokeball = String.valueOf(specs.get(key));
				break;
			case "ge":
			case "gender":
				this.gender = String.valueOf(specs.get(key));
				break;
			}
		}
	}
}
