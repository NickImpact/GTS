package me.nickimpact.gts.generations.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.gui.v2.Icon;
import com.nickimpact.impactor.gui.v2.Layout;
import com.nickimpact.impactor.gui.v2.UI;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.configuration.ConfigKeys;
import me.nickimpact.gts.entries.prices.MoneyPrice;
import me.nickimpact.gts.generations.GenerationsBridge;
import me.nickimpact.gts.generations.config.PokemonConfigKeys;
import me.nickimpact.gts.generations.config.PokemonMsgConfigKeys;
import me.nickimpact.gts.generations.entries.EnumHidableDetail;
import me.nickimpact.gts.generations.entries.PokemonEntry;
import me.nickimpact.gts.internal.TextParsingUtils;
import me.nickimpact.gts.ui.SellUI;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
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

	private EntityPixelmon selection;
	private PlayerStorage storage;
	private double min;

	public PixelmonUI() {}

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
				.build(GenerationsBridge.getInstance());
		return display.define(this.forgeLayout(player));
	}

	@Override
	protected Layout forgeLayout(Player player) {
		Layout.Builder lb = Layout.builder();
		lb.row(Icon.BORDER, 0).row(Icon.BORDER, 2);
		lb.column(Icon.BORDER, 0).slots(Icon.BORDER, 16, 34, 43, 52);

		lb.slot(this.increase, 29);
		lb.slot(this.money, 38);
		lb.slot(this.decrease, 47);

		lb.slot(this.timeInc, 31);
		lb.slot(this.timeIcon, 40);
		lb.slot(this.timeDec, 49);

		List<Icon> party = Lists.newArrayList();
		this.storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID(player.getUniqueId()).orElseThrow(() -> new RuntimeException("Unable to fetch player data for " + player.getName()));
		for(NBTTagCompound nbt : storage.partyPokemon) {
			if(nbt == null) continue;

			EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
			Map<String, Object> variables = Maps.newHashMap();
			variables.put("pokemon", pokemon);

			ItemStack display = PixelmonIcons.pokemonDisplay(pokemon, pokemon.getForm());
			display.offer(Keys.DISPLAY_NAME, TextParsingUtils.fetchAndParseMsg(player, PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_TITLE, null, variables));

			List<String> template = Lists.newArrayList();
			template.addAll(GenerationsBridge.getInstance().getMsgConfig().get(PokemonMsgConfigKeys.POKEMON_SELLER_PREVIEW));
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

		for(int i = 10; i < 16 && i - 10 < party.size(); i++) {
			lb.slot(party.get(i - 10), i);
		}

		Icon confirm = Icon.from(Icon.CONFIRM.getDisplay());
		confirm.addListener(clickable -> {
			if(this.selection != null) {
				this.display.close(clickable.getPlayer());
				if(this.storage.countTeam() == 1) {
					clickable.getPlayer().sendMessage(TextParsingUtils.fetchAndParseMsg(clickable.getPlayer(), PokemonMsgConfigKeys.POKEMON_LAST_MEMBER, null, null));
					return;
				}

				Listing listing = Listing.builder()
						.entry(new PokemonEntry(this.selection, new MoneyPrice(this.amount)))
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

	private void addLore(EntityPixelmon pokemon, ItemStack icon, List<String> template, Player player, Map<String, Object> variables) {
		for (EnumHidableDetail detail : EnumHidableDetail.values()) {
			if (detail.getCondition().test(pokemon)) {
				template.addAll(GenerationsBridge.getInstance().getMsgConfig().get(detail.getField()));
			}
		}

		List<Text> translated = template.stream().map(str -> TextParsingUtils.fetchAndParseMsg(player, str, null, variables)).collect(Collectors.toList());
		icon.offer(Keys.ITEM_LORE, translated);
	}

	@Override
	protected double getMin() {
		return GTS.getInstance().getConfig().get(ConfigKeys.MIN_PRICING_ENABLED) ? this.min : 1;
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

	@Override
	public double getLeftClickBaseAmount() {
		return GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.PRICING_LEFTCLICK_BASE);
	}

	@Override
	public double getRightClickBaseAmount() {
		return GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.PRICING_RIGHTCLICK_BASE);
	}

	@Override
	public double getLeftClickShiftAmount() {
		return GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.PRICING_LEFTCLICK_SHIFT);
	}

	@Override
	public double getRightClickShiftAmount() {
		return GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.PRICING_RIGHTCLICK_SHIFT);
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
