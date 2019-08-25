package me.nickimpact.gts.generations.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.api.plugin.PluginInstance;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.generations.GenerationsBridge;
import me.nickimpact.gts.generations.config.PokemonConfigKeys;
import me.nickimpact.gts.generations.config.PokemonMsgConfigKeys;
import me.nickimpact.gts.generations.entries.EnumHidableDetail;
import me.nickimpact.gts.generations.entries.KeyDetailHolder;
import me.nickimpact.gts.generations.entries.PokemonEntry;
import me.nickimpact.gts.sponge.SpongeListing;
import me.nickimpact.gts.sponge.SpongePlugin;
import me.nickimpact.gts.sponge.TextParsingUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PixelmonUI implements EntryUI<Player> {

	private SpongeUI display;
	private Player viewer;

	private EntityPixelmon selection;
	private PlayerStorage party;

	private double min;
	private double amount;

	public PixelmonUI() {}

	private PixelmonUI(Player player) {
		this.viewer = player;
		this.display = SpongeUI.builder()
				.title(((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils().fetchAndParseMsg(
						player, GenerationsBridge.getInstance().getMsgConfig(), PokemonMsgConfigKeys.UI_TITLES_POKEMON, null, null
				))
				.dimension(InventoryDimension.of(9, 6))
				.build()
				.define(this.forgeLayout());
	}

	@Override
	public PixelmonUI createFor(Player player) {
		return new PixelmonUI(player);
	}

	@Override
	public SpongeUI getDisplay() {
		return this.display;
	}

	private SpongeLayout forgeLayout() {
		SpongeLayout.SpongeLayoutBuilder slb = SpongeLayout.builder();
		slb.rows(SpongeIcon.BORDER, 0, 2);
		slb.slots(SpongeIcon.BORDER, 9, 18, 16, 34, 43, 52);

		EconomyService economy = ((SpongePlugin) PluginInstance.getInstance()).getEconomy();
		Config config = PluginInstance.getInstance().getConfiguration();
		Config msgConfig = PluginInstance.getInstance().getMsgConfig();
		TextParsingUtils parser = ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils();
		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("gts_button_currency_left_click", src -> Optional.of(economy.getDefaultCurrency().format(new BigDecimal(config.get(ConfigKeys.PRICING_LEFTCLICK_BASE)))));
		tokens.put("gts_button_currency_right_click", src -> Optional.of(economy.getDefaultCurrency().format(new BigDecimal(config.get(ConfigKeys.PRICING_RIGHTCLICK_BASE)))));
		tokens.put("gts_button_currency_shift_left_click", src -> Optional.of(economy.getDefaultCurrency().format(new BigDecimal(config.get(ConfigKeys.PRICING_LEFTCLICK_SHIFT)))));
		tokens.put("gts_button_currency_shift_right_click", src -> Optional.of(economy.getDefaultCurrency().format(new BigDecimal(config.get(ConfigKeys.PRICING_RIGHTCLICK_SHIFT)))));
		tokens.put("gts_min_price", src -> Optional.of(economy.getDefaultCurrency().format(new BigDecimal(1))));
		tokens.put("gts_max_price", src -> Optional.of(economy.getDefaultCurrency().format(new BigDecimal(config.get(ConfigKeys.MAX_MONEY_PRICE)))));

		slb.slot(this.moneyDecIcon(tokens), 37);
		slb.slot(this.moneyIcon(tokens), 39);
		slb.slot(this.moneyIncIcon(tokens), 41);

		this.party = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID(this.viewer.getUniqueId()).get();
		int index = 0;
		for(NBTTagCompound nbt : party.partyPokemon) {
			if (nbt == null) {
				index++;
				continue;
			}

			EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) this.viewer.getWorld());

			Map<String, Object> variables = Maps.newHashMap();
			variables.put("pokemon", pokemon);

			ItemStack display = PixelmonIcons.pokemonDisplay(pokemon, pokemon.getForm());
			display.offer(Keys.DISPLAY_NAME, parser.fetchAndParseMsg(
					this.viewer,
					GenerationsBridge.getInstance().getMsgConfig(),
					pokemon.isEgg ? PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_TITLE_EGG : PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_TITLE,
					null,
					variables
			));
			this.addLore(pokemon, display, Lists.newArrayList(GenerationsBridge.getInstance().getMsgConfig().get(PokemonMsgConfigKeys.POKEMON_PREVIEW_LORE)), this.viewer, variables);
			SpongeIcon icon = new SpongeIcon(display);
			icon.addListener(clickable -> {
				this.selection = pokemon;
				this.display.setSlot(17, new SpongeIcon(display));
				this.min = this.calcMin(pokemon);
				this.amount = this.min;
				this.display.setSlot(39, this.moneyIcon(tokens));
			});
			slb.slot(icon, 10 + index++);
		}

		SpongeIcon confirm = new SpongeIcon(ItemStack.builder().itemType(ItemTypes.DYE)
				.add(Keys.DISPLAY_NAME, parser.fetchAndParseMsg(
						this.viewer,
						msgConfig,
						MsgConfigKeys.CONFIRM_SELECTION,
						null,
						null
				))
				.add(Keys.DYE_COLOR, DyeColors.LIME)
				.build()
		);
		confirm.addListener(clickable -> {
			if(this.selection != null) {
				this.display.close(clickable.getPlayer());
				if(this.party.countTeam() == 1) {
					clickable.getPlayer().sendMessage(parser.fetchAndParseMsg(clickable.getPlayer(), GenerationsBridge.getInstance().getMsgConfig(), PokemonMsgConfigKeys.ERROR_LAST_MEMBER, null, null));
					return;
				}

				SpongeListing listing = SpongeListing.builder()
						.entry(new PokemonEntry(this.selection))
						.price(this.amount)
						.id(UUID.randomUUID())
						.owner(clickable.getPlayer().getUniqueId())
						.expiration(LocalDateTime.now().plusSeconds(config.get(ConfigKeys.LISTING_TIME)))
						.build();

				listing.publish(PluginInstance.getInstance(), clickable.getPlayer().getUniqueId());
			}
		});
		slb.slot(confirm, 35);

		SpongeIcon cancel = new SpongeIcon(ItemStack.builder().itemType(ItemTypes.DYE)
				.add(Keys.DISPLAY_NAME, parser.fetchAndParseMsg(
						this.viewer,
						msgConfig,
						MsgConfigKeys.CANCEL,
						null,
						null
				))
				.add(Keys.DYE_COLOR, DyeColors.RED)
				.build()
		);
		cancel.addListener(clickable -> {
			this.display.close(clickable.getPlayer());
		});
		slb.slot(cancel, 53);


		return slb.build();
	}

	private void addLore(EntityPixelmon pokemon, ItemStack icon, List<String> template, Player player, Map<String, Object> variables) {
		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		for (EnumHidableDetail detail : EnumHidableDetail.values()) {
			if (detail.getCondition().test(pokemon)) {
				KeyDetailHolder holder = detail.getField().apply(pokemon);
				template.addAll(GenerationsBridge.getInstance().getMsgConfig().get(holder.getKey()));
				if(holder.getTokens() != null) {
					tokens.putAll(holder.getTokens());
				}
			}
		}

		List<Text> translated = template.stream().map(str -> ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils().fetchAndParseMsg(player, str, tokens, variables)).collect(Collectors.toList());
		icon.offer(Keys.ITEM_LORE, translated);
	}

	private SpongeIcon moneyIncIcon(Map<String, Function<CommandSource, Optional<Text>>> tokens) {
		Config config = GenerationsBridge.getInstance().getConfig();
		Config msgConfig = PluginInstance.getInstance().getMsgConfig();
		TextParsingUtils parser = ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils();
		ItemStack inc = ItemStack.builder()
				.itemType(ItemTypes.DYE)
				.add(Keys.DYE_COLOR, DyeColors.LIME)
				.add(Keys.DISPLAY_NAME, ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils().fetchAndParseMsg(
						viewer, PluginInstance.getInstance().getMsgConfig(), MsgConfigKeys.BUTTONS_INCREASE_CURRENCY_TITLE, null, null
				))
				.add(Keys.ITEM_LORE,
						parser.fetchAndParseMsgs(this.viewer, msgConfig, MsgConfigKeys.BUTTONS_INCREASE_CURRENCY_LORE, tokens, null)
				)
				.build();
		SpongeIcon icon = new SpongeIcon(inc);
		icon.addListener(clickable -> {
			ClickInventoryEvent event = clickable.getEvent();
			if(event instanceof ClickInventoryEvent.Shift) {
				if(event instanceof ClickInventoryEvent.Shift.Secondary) {
					this.amount = Math.min(PluginInstance.getInstance().getConfiguration().get(ConfigKeys.MAX_MONEY_PRICE), this.amount + config.get(PokemonConfigKeys.PRICING_RIGHTCLICK_SHIFT));
				} else {
					this.amount = Math.min(PluginInstance.getInstance().getConfiguration().get(ConfigKeys.MAX_MONEY_PRICE), this.amount + config.get(PokemonConfigKeys.PRICING_LEFTCLICK_SHIFT));
				}
			} else {
				if(event instanceof ClickInventoryEvent.Secondary) {
					this.amount = Math.min(PluginInstance.getInstance().getConfiguration().get(ConfigKeys.MAX_MONEY_PRICE), this.amount + config.get(PokemonConfigKeys.PRICING_RIGHTCLICK_BASE));
				} else {
					this.amount = Math.min(PluginInstance.getInstance().getConfiguration().get(ConfigKeys.MAX_MONEY_PRICE), this.amount + config.get(PokemonConfigKeys.PRICING_LEFTCLICK_BASE));
				}
			}
			this.display.setSlot(39, this.moneyIcon(tokens));
		});

		return icon;
	}

	private SpongeIcon moneyIcon(Map<String, Function<CommandSource, Optional<Text>>> tokens) {
		EconomyService economy = ((SpongePlugin) PluginInstance.getInstance()).getEconomy();
		tokens.put("gts_price", src -> Optional.of(economy.getDefaultCurrency().format(new BigDecimal(this.amount))));
		Config msgConfig = PluginInstance.getInstance().getMsgConfig();
		TextParsingUtils parser = ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils();
		ItemStack inc = ItemStack.builder()
				.itemType(ItemTypes.GOLD_INGOT)
				.add(Keys.DISPLAY_NAME, ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils().fetchAndParseMsg(
						viewer, PluginInstance.getInstance().getMsgConfig(), MsgConfigKeys.PRICE_DISPLAY_TITLE, null, null
				))
				.add(Keys.ITEM_LORE,
						parser.fetchAndParseMsgs(this.viewer, msgConfig, MsgConfigKeys.PRICE_DISPLAY_LORE, tokens, null)
				)
				.build();
		return new SpongeIcon(inc);
	}

	private SpongeIcon moneyDecIcon(Map<String, Function<CommandSource, Optional<Text>>> tokens) {
		Config config = GenerationsBridge.getInstance().getConfig();
		Config msgConfig = PluginInstance.getInstance().getMsgConfig();
		TextParsingUtils parser = ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils();
		ItemStack inc = ItemStack.builder()
				.itemType(ItemTypes.DYE)
				.add(Keys.DYE_COLOR, DyeColors.RED)
				.add(Keys.DISPLAY_NAME, ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils().fetchAndParseMsg(
						viewer, PluginInstance.getInstance().getMsgConfig(), MsgConfigKeys.BUTTONS_DECREASE_CURRENCY_TITLE, null, null
				))
				.add(Keys.ITEM_LORE,
						parser.fetchAndParseMsgs(this.viewer, msgConfig, MsgConfigKeys.BUTTONS_DECREASE_CURRENCY_LORE, tokens, null)
				)
				.build();
		SpongeIcon icon = new SpongeIcon(inc);
		icon.addListener(clickable -> {
			ClickInventoryEvent event = clickable.getEvent();
			if(event instanceof ClickInventoryEvent.Shift) {
				if(event instanceof ClickInventoryEvent.Shift.Secondary) {
					this.amount = Math.max(min, this.amount - config.get(PokemonConfigKeys.PRICING_RIGHTCLICK_SHIFT));
				} else {
					this.amount = Math.max(min, this.amount - config.get(PokemonConfigKeys.PRICING_LEFTCLICK_SHIFT));
				}
			} else {
				if(event instanceof ClickInventoryEvent.Secondary) {
					this.amount = Math.max(min, this.amount - config.get(PokemonConfigKeys.PRICING_RIGHTCLICK_BASE));
				} else {
					this.amount = Math.max(min, this.amount - config.get(PokemonConfigKeys.PRICING_LEFTCLICK_BASE));
				}
			}
			this.display.setSlot(39, this.moneyIcon(tokens));
		});

		return icon;
	}

	private double calcMin(EntityPixelmon pokemon) {
		double price = GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_BASE);
		boolean isLegend = EnumPokemon.legendaries.contains(pokemon.getSpecies().name());
		if (isLegend && pokemon.getIsShiny()) {
			price += GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_LEGEND) + GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_SHINY);
		} else if (isLegend) {
			price += GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_LEGEND);
		} else if (pokemon.getIsShiny()) {
			price += GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_SHINY);
		}

		for (int iv : pokemon.stats.IVs.getArray()) {
			if (iv >= GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_IVS_MINVAL)) {
				price += GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_IVS_PRICE);
			}
		}

		if (pokemon.getAbilitySlot() == 2) {
			price += GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_HA);
		}

		return price;
	}
}
