package me.nickimpact.gts.pixelmon.ui;

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
import me.nickimpact.gts.configuration.MsgConfigKeys;
import me.nickimpact.gts.entries.prices.MoneyPrice;
import me.nickimpact.gts.internal.TextParsingUtils;
import me.nickimpact.gts.pixelmon.config.PokemonConfigKeys;
import me.nickimpact.gts.pixelmon.entries.EnumHidableDetail;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.data.key.Keys;
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
				.build(GTS.getInstance());
		return display.define(this.forgeLayout(player));
	}

	@Override
	protected Layout forgeLayout(Player player) {
		Layout.Builder lb = Layout.builder();
		lb.row(Icon.BORDER, 0).row(Icon.BORDER, 2);
		lb.slots(Icon.BORDER, 0, 9, 18, 16, 34, 43, 52);

		lb.slot(this.increase, 30);
		lb.slot(this.money, 39);
		lb.slot(this.decrease, 48);

		List<Icon> party = Lists.newArrayList();
		PlayerStorage storage = PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP) player).orElse(null);
		if (storage != null) {
			for (NBTTagCompound nbt : storage.partyPokemon) {
				if(nbt == null) continue;
				EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) player.getWorld());
				Map<String, Object> variables = Maps.newHashMap();
				variables.put("pokemon", pokemon);

				ItemStack display = PixelmonIcons.pokemonDisplay(pokemon, pokemon.getForm());
				display.offer(Keys.DISPLAY_NAME, TextParsingUtils.fetchAndParseMsg(player, MsgConfigKeys.POKEMON_ENTRY_BASE_TITLE, null, variables));

				List<String> template = Lists.newArrayList();
				template.addAll(GTS.getInstance().getMsgConfig().get(PokemonConfigKeys.POKEMON_SELLER_PREVIEW));
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

		return lb.build();
	}

	@Override
	protected double getMin() {
		return this.min;
	}

	@Override
	protected double getMax() {
		return 0;
	}

	@Override
	protected void update() {
		this.getDisplay().setSlot(39, this.moneyIcon());
	}

	private void addLore(EntityPixelmon pokemon, ItemStack icon, List<String> template, Player player, Map<String, Object> variables) {
		for (EnumHidableDetail detail : EnumHidableDetail.values()) {
			if (detail.getCondition().test(pokemon)) {
				template.addAll(GTS.getInstance().getMsgConfig().get(detail.getField()));
			}
		}

		List<Text> translated = template.stream().map(str -> TextParsingUtils.fetchAndParseMsg(player, str, null, variables)).collect(Collectors.toList());
		icon.offer(Keys.ITEM_LORE, translated);
	}

	private double calcMin(EntityPixelmon pokemon) {
		double price = 0;
		boolean isLegend = EnumPokemon.legendaries.contains(pokemon.getName());
		if (isLegend && pokemon.getIsShiny()) {
			price += GTS.getInstance().getConfig().get(ConfigKeys.MIN_PRICING_POKEMON_LEGEND) + GTS.getInstance().getConfig().get(ConfigKeys.MIN_PRICING_POKEMON_SHINY);
		} else if (isLegend) {
			price += GTS.getInstance().getConfig().get(ConfigKeys.MIN_PRICING_POKEMON_LEGEND);
		} else if (pokemon.getIsShiny()) {
			price += GTS.getInstance().getConfig().get(ConfigKeys.MIN_PRICING_POKEMON_SHINY);
		}

		for (int iv : pokemon.stats.ivs.getArray()) {
			if (iv >= GTS.getInstance().getConfig().get(ConfigKeys.MIN_PRICING_POKEMON_IVS_MINVAL)) {
				price += GTS.getInstance().getConfig().get(ConfigKeys.MIN_PRICING_POKEMON_IVS_PRICE);
			}
		}

		if (pokemon.getAbilitySlot() == 2) {
			price += GTS.getInstance().getConfig().get(ConfigKeys.MIN_PRICING_POKEMON_HA);
		}

		return price;
	}
}
