package me.nickimpact.gts.pixelmon.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.gui.v2.Icon;
import com.nickimpact.impactor.gui.v2.Layout;
import com.nickimpact.impactor.gui.v2.UI;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.entries.prices.MoneyPrice;
import me.nickimpact.gts.internal.TextParsingUtils;
import me.nickimpact.gts.pixelmon.ReforgedBridge;
import me.nickimpact.gts.pixelmon.config.PokemonConfigKeys;
import me.nickimpact.gts.pixelmon.config.PokemonMsgConfigKeys;
import me.nickimpact.gts.pixelmon.entries.EnumHidableDetail;
import me.nickimpact.gts.pixelmon.entries.ReforgedEntry;
import me.nickimpact.gts.ui.SellUI;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PixelmonUI extends EntryUI {

	private UI display;

	private Pokemon selection;
	private PlayerPartyStorage storage;
	private double min;

	public PixelmonUI() {
	}

	private PixelmonUI(Player player) {
		super(player);
		this.display = this.createUI(player);
	}

	@Override
	public PixelmonUI createFor(Player player) {
		return new PixelmonUI(player);
	}

	@Override
	public UI getDisplay() {
		return this.display;
	}

	@Override
	protected UI createUI(Player player) {
		UI display = UI.builder()
				.title(Text.of(TextColors.RED, "GTS ", TextColors.GRAY, "(", TextColors.DARK_AQUA, "Pixelmon", TextColors.GRAY, ")"))
				.dimension(InventoryDimension.of(9, 6))
				.build(ReforgedBridge.getInstance());
		return display.define(this.forgeLayout(player));
	}

	@Override
	protected Layout forgeLayout(Player player) {
		Layout.Builder lb = Layout.builder();
		lb.row(Icon.BORDER, 0).row(Icon.BORDER, 2);
		lb.slots(Icon.BORDER, 9, 18, 16, 34, 43, 52);

		lb.slot(this.increase, 29);
		lb.slot(this.money, 38);
		lb.slot(this.decrease, 47);

		lb.slot(this.timeInc, 31);
		lb.slot(this.timeIcon, 40);
		lb.slot(this.timeDec, 49);

		List<Icon> party = Lists.newArrayList();
		this.storage = Pixelmon.storageManager.getParty(player.getUniqueId());
		if (storage != null) {
			for (Pokemon pokemon : storage.getAll()) {
				if(pokemon == null) continue;
				Map<String, Object> variables = Maps.newHashMap();
				variables.put("pokemon", pokemon);

				ItemStack display = PixelmonIcons.pokemonDisplay(pokemon, pokemon.getForm());
				display.offer(Keys.DISPLAY_NAME, TextParsingUtils.fetchAndParseMsg(player, PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_TITLE, null, variables));

				List<String> template = Lists.newArrayList();
				template.addAll(ReforgedBridge.getInstance().getMsgConfig().get(PokemonMsgConfigKeys.POKEMON_SELLER_PREVIEW));
				this.addLore(pokemon, display, template, player, variables);

				Icon icon = Icon.from(display);
				icon.addListener(clickable -> {
					this.selection = pokemon;
					this.display.setSlot(17, Icon.from(display));
					this.min = this.calcMin(pokemon);

					this.amount = new BigDecimal(this.min);

					this.update();
				});
				party.add(icon);
			}
		}

		for (int i = 10; i < 16 && i - 10 < party.size(); i++) {
			lb.slot(party.get(i - 10), i);
		}

		Icon confirm = Icon.from(Icon.CONFIRM.getDisplay());
		confirm.addListener(clickable -> {
			if(this.selection != null) {
				this.display.close(clickable.getPlayer());
				if(this.storage.countPokemon() == 1) {
					clickable.getPlayer().sendMessage(TextParsingUtils.fetchAndParseMsg(clickable.getPlayer(), PokemonMsgConfigKeys.POKEMON_LAST_MEMBER, null, null));
					return;
				}

				Listing listing = Listing.builder()
						.entry(new ReforgedEntry(this.selection, new MoneyPrice(this.amount)))
						.doesExpire()
						.player(clickable.getPlayer())
						.expiration(this.time)
						.build();
				
				listing.publish(clickable.getPlayer());
			}
		});
		lb.slot(confirm, 35);

		Icon cancel = Icon.from(Icon.CANCEL.getDisplay());
		cancel.getDisplay().offer(Keys.DYE_COLOR, DyeColors.RED);
		cancel.addListener(clickable -> {
			this.display.close(clickable.getPlayer());
			new SellUI(clickable.getPlayer()).open(clickable.getPlayer(), 1);
		});
		lb.slot(cancel, 53);

		return lb.build();
	}

	@Override
	protected double getMin() {
		return this.min;
	}

	@Override
	protected double getMax() {
		return 100_000_000;
	}

	@Override
	protected long getTimeMin() {
		return 1800;
	}

	@Override
	protected long getTimeMax() {
		return 3600 * 24;
	}

	@Override
	protected void update() {
		this.getDisplay().setSlot(38, this.moneyIcon());
		this.getDisplay().setSlot(40, this.timeIcon());
	}

	private void addLore(Pokemon pokemon, ItemStack icon, List<String> template, Player player, Map<String, Object> variables) {
		for (EnumHidableDetail detail : EnumHidableDetail.values()) {
			if (detail.getCondition().test(pokemon)) {
				template.addAll(ReforgedBridge.getInstance().getMsgConfig().get(detail.getField()));
			}
		}

		List<Text> translated = template.stream().map(str -> TextParsingUtils.fetchAndParseMsg(player, str, null, variables)).collect(Collectors.toList());
		icon.offer(Keys.ITEM_LORE, translated);
	}

	private double calcMin(Pokemon pokemon) {
		double price = ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_BASE);
		boolean isLegend = EnumSpecies.legendaries.contains(pokemon.getSpecies().name());
		if (isLegend && pokemon.isShiny()) {
			price += ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_LEGEND) + ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_SHINY);
		} else if (isLegend) {
			price += ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_LEGEND);
		} else if (pokemon.isShiny()) {
			price += ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_SHINY);
		}

		for (int iv : pokemon.getStats().ivs.getArray()) {
			if (iv >= ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_IVS_MINVAL)) {
				price += ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_IVS_PRICE);
			}
		}

		if (pokemon.getAbilitySlot() == 2) {
			price += ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_HA);
		}

		return price;
	}
}
