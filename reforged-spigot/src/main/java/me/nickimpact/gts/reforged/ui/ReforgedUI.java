package me.nickimpact.gts.reforged.ui;

import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.spigot.ui.SpigotIcon;
import com.nickimpact.impactor.spigot.ui.SpigotLayout;
import com.nickimpact.impactor.spigot.ui.SpigotUI;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.api.plugin.PluginInstance;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.reforged.ReforgedBridge;
import me.nickimpact.gts.reforged.config.ReforgedMsgConfigKeys;
import me.nickimpact.gts.reforged.entry.ReforgedEntry;
import me.nickimpact.gts.reforged.utils.SpriteItemUtil;
import me.nickimpact.gts.spigot.MessageUtils;
import me.nickimpact.gts.spigot.SpigotGTSPlugin;
import me.nickimpact.gts.spigot.SpigotListing;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReforgedUI implements EntryUI<Player> {

	private Player viewer;
	private SpigotUI ui;

	private Pokemon selection;
	private PlayerPartyStorage party;

	private double min;
	private double amount;

	public ReforgedUI() {}

	private ReforgedUI(Player player) {
		this.viewer = player;
		this.ui = this.createDisplay();
	}

	@Override
	public ReforgedUI createFor(Player player) {
		return new ReforgedUI(player);
	}

	@Override
	public SpigotUI getDisplay() {
		return this.ui;
	}

	private SpigotUI createDisplay() {
		return SpigotUI.builder()
				.title(((SpigotGTSPlugin) PluginInstance.getInstance()).getTokenService().process(
						ReforgedBridge.getInstance().getMsgConfig(), ReforgedMsgConfigKeys.UI_TITLES_POKEMON, this.viewer, null, null
				))
				.size(54)
				.build()
				.define(this.formatDisplay());
	}

	private SpigotLayout formatDisplay() {
		SpigotLayout.SpigotLayoutBuilder slb = SpigotLayout.builder();
		slb.rows(SpigotIcon.BORDER, 0, 2);
		slb.column(SpigotIcon.BORDER, 7);
		slb.slots(SpigotIcon.BORDER, 9);

		Config config = PluginInstance.getInstance().getConfiguration();
		this.party = Pixelmon.storageManager.getParty(this.viewer.getUniqueId());
		int index = 0;
		for(Pokemon pokemon : this.party.getAll()) {
			if(pokemon == null) {
				index++;
				continue;
			}

			ItemStack item = SpriteItemUtil.createPicture(pokemon);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', String.format(
					!pokemon.isEgg() ? "&3%s &7| &aLvl %d" : "&3%s Egg",
					pokemon.getSpecies().getLocalizedName(),
					pokemon.getLevel()))
			);

			List<String> details = SpriteItemUtil.getDetails(pokemon);
			meta.setLore(details.subList(0, details.size() - 1).stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
			item.setItemMeta(meta);
			SpigotIcon icon = new SpigotIcon(item);
			icon.addListener(clickable -> {
				this.selection = pokemon;
				SpigotIcon i = new SpigotIcon(item);
				this.getDisplay().setSlot(17, i);
			});
			slb.slot(icon, 10 + index++);
		}

		ItemStack conf = new ItemStack(Material.INK_SACK, 1, (short) 10);
		ItemMeta cm = conf.getItemMeta();
		cm.setDisplayName(ChatColor.GREEN + "Confirm Selection");
		SpigotIcon confirm = new SpigotIcon(conf);
		confirm.addListener(clickable -> {
			if(this.selection != null) {
				this.ui.close(clickable.getPlayer());
				if(this.party.countPokemon() == 1) {
					clickable.getPlayer().sendMessage(MessageUtils.parse("You can't sell your last pokemon...", true));
					return;
				}

				SpigotListing listing = SpigotListing.builder()
						.entry(new ReforgedEntry(this.selection))
						.price(this.amount)
						.id(UUID.randomUUID())
						.owner(clickable.getPlayer().getUniqueId())
						.expiration(LocalDateTime.now().plusSeconds(config.get(ConfigKeys.LISTING_TIME)))
						.build();

				listing.publish(PluginInstance.getInstance(), clickable.getPlayer().getUniqueId());
			}
		});
		slb.slot(confirm, 35);

		ItemStack can = new ItemStack(Material.INK_SACK, 1, (short) 10);
		ItemMeta caM = can.getItemMeta();
		caM.setDisplayName(ChatColor.RED + "Cancel");
		can.setItemMeta(caM);
		SpigotIcon cancel = new SpigotIcon(can);
		cancel.addListener(clickable -> {
			this.ui.close(clickable.getPlayer());
		});
		slb.slot(cancel, 53);

		return slb.build();
	}

//	private SpongeIcon moneyIncIcon() {
//		Config config = ReforgedBridge.getInstance().getConfiguration();
//		Config msgConfig = PluginInstance.getInstance().getMsgConfig();
//		ItemStack inc = new ItemStack(Material.INK_SACK, 1, (short) 10);
//		ItemMeta meta = inc.getItemMeta();
//		meta.setDisplayName(ChatColor.GREEN + "Increase Price Requested");
//
//		ItemStack inc = ItemStack.builder()
//				.itemType(ItemTypes.DYE)
//				.add(Keys.DYE_COLOR, DyeColors.LIME)
//				.add(Keys.DISPLAY_NAME, ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils().fetchAndParseMsg(
//						viewer, PluginInstance.getInstance().getMsgConfig(), MsgConfigKeys.BUTTONS_INCREASE_CURRENCY_TITLE, null, null
//				))
//				.add(Keys.ITEM_LORE,
//						parser.fetchAndParseMsgs(this.viewer, msgConfig, MsgConfigKeys.BUTTONS_INCREASE_CURRENCY_LORE, tokens, null)
//				)
//				.build();
//		SpongeIcon icon = new SpongeIcon(inc);
//		icon.addListener(clickable -> {
//			InventoryClickEvent event = clickable.getEvent();
//			if(event instanceof ClickInventoryEvent.Shift) {
//				if(event instanceof ClickInventoryEvent.Shift.Secondary) {
//					this.amount = Math.min(PluginInstance.getInstance().getConfiguration().get(ConfigKeys.MAX_MONEY_PRICE), this.amount + config.get(PokemonConfigKeys.PRICING_RIGHTCLICK_SHIFT));
//				} else {
//					this.amount = Math.min(PluginInstance.getInstance().getConfiguration().get(ConfigKeys.MAX_MONEY_PRICE), this.amount + config.get(PokemonConfigKeys.PRICING_LEFTCLICK_SHIFT));
//				}
//			} else {
//				if(event instanceof ClickInventoryEvent.Secondary) {
//					this.amount = Math.min(PluginInstance.getInstance().getConfiguration().get(ConfigKeys.MAX_MONEY_PRICE), this.amount + config.get(PokemonConfigKeys.PRICING_RIGHTCLICK_BASE));
//				} else {
//					this.amount = Math.min(PluginInstance.getInstance().getConfiguration().get(ConfigKeys.MAX_MONEY_PRICE), this.amount + config.get(PokemonConfigKeys.PRICING_LEFTCLICK_BASE));
//				}
//			}
//			this.display.setSlot(39, this.moneyIcon(tokens));
//		});
//
//		return icon;
//	}
//
//	private SpongeIcon moneyIcon() {
//		EconomyService economy = ((SpongePlugin) PluginInstance.getInstance()).getEconomy();
//		tokens.put("gts_price", src -> Optional.of(economy.getDefaultCurrency().format(new BigDecimal(this.amount))));
//		Config msgConfig = PluginInstance.getInstance().getMsgConfig();
//		TextParsingUtils parser = ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils();
//		ItemStack inc = ItemStack.builder()
//				.itemType(ItemTypes.GOLD_INGOT)
//				.add(Keys.DISPLAY_NAME, ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils().fetchAndParseMsg(
//						viewer, PluginInstance.getInstance().getMsgConfig(), MsgConfigKeys.PRICE_DISPLAY_TITLE, null, null
//				))
//				.add(Keys.ITEM_LORE,
//						parser.fetchAndParseMsgs(this.viewer, msgConfig, MsgConfigKeys.PRICE_DISPLAY_LORE, tokens, null)
//				)
//				.build();
//		return new SpongeIcon(inc);
//	}
//
//	private SpongeIcon moneyDecIcon(Map<String, Function<CommandSource, Optional<Text>>> tokens) {
//		Config config = ReforgedBridge.getInstance().getConfig();
//		Config msgConfig = PluginInstance.getInstance().getMsgConfig();
//		TextParsingUtils parser = ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils();
//		ItemStack inc = ItemStack.builder()
//				.itemType(ItemTypes.DYE)
//				.add(Keys.DYE_COLOR, DyeColors.RED)
//				.add(Keys.DISPLAY_NAME, ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils().fetchAndParseMsg(
//						viewer, PluginInstance.getInstance().getMsgConfig(), MsgConfigKeys.BUTTONS_DECREASE_CURRENCY_TITLE, null, null
//				))
//				.add(Keys.ITEM_LORE,
//						parser.fetchAndParseMsgs(this.viewer, msgConfig, MsgConfigKeys.BUTTONS_DECREASE_CURRENCY_LORE, tokens, null)
//				)
//				.build();
//		SpongeIcon icon = new SpongeIcon(inc);
//		icon.addListener(clickable -> {
//			ClickInventoryEvent event = clickable.getEvent();
//			if(event instanceof ClickInventoryEvent.Shift) {
//				if(event instanceof ClickInventoryEvent.Shift.Secondary) {
//					this.amount = Math.max(min, this.amount - config.get(PokemonConfigKeys.PRICING_RIGHTCLICK_SHIFT));
//				} else {
//					this.amount = Math.max(min, this.amount - config.get(PokemonConfigKeys.PRICING_LEFTCLICK_SHIFT));
//				}
//			} else {
//				if(event instanceof ClickInventoryEvent.Secondary) {
//					this.amount = Math.max(min, this.amount - config.get(PokemonConfigKeys.PRICING_RIGHTCLICK_BASE));
//				} else {
//					this.amount = Math.max(min, this.amount - config.get(PokemonConfigKeys.PRICING_LEFTCLICK_BASE));
//				}
//			}
//			this.display.setSlot(39, this.moneyIcon(tokens));
//		});
//
//		return icon;
//	}
//
//	private double calcMin(Pokemon pokemon) {
//		if(!PluginInstance.getInstance().getConfiguration().get(ConfigKeys.MIN_PRICING_ENABLED)) {
//			return 1.0;
//		}
//
//		double price = ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_BASE);
//		boolean isLegend = EnumSpecies.legendaries.contains(pokemon.getSpecies().name());
//		if (isLegend && pokemon.isShiny()) {
//			price += ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_LEGEND) + ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_SHINY);
//		} else if (isLegend) {
//			price += ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_LEGEND);
//		} else if (pokemon.isShiny()) {
//			price += ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_SHINY);
//		}
//
//		for (int iv : pokemon.getStats().ivs.getArray()) {
//			if (iv >= ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_IVS_MINVAL)) {
//				price += ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_IVS_PRICE);
//			}
//		}
//
//		if (pokemon.getAbilitySlot() == 2) {
//			price += ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_HA);
//		}
//
//		return price;
//	}
}
