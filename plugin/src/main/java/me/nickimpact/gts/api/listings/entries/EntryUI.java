package me.nickimpact.gts.api.listings.entries;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.gui.v2.Displayable;
import com.nickimpact.impactor.gui.v2.Icon;
import com.nickimpact.impactor.gui.v2.Layout;
import com.nickimpact.impactor.gui.v2.UI;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.time.Time;
import me.nickimpact.gts.configuration.MsgConfigKeys;
import me.nickimpact.gts.internal.TextParsingUtils;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class EntryUI implements Displayable {

	private Player player;

	/** Represents the amount of money being used to put the listing up for */
	protected BigDecimal amount = new BigDecimal(this.getMin());
	protected long time = this.getTimeMin();

	/** Icons which handle the price increase setup */
	protected Icon increase;
	protected Icon money;
	protected Icon decrease;

	protected Icon timeInc;
	protected Icon timeIcon;
	protected Icon timeDec;

	public EntryUI() {}

	protected EntryUI(Player player) {
		this.player = player;

		EconomyService service = GTS.getInstance().getEconomy();

		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("gts_button_currency_left_click", src -> Optional.of(service.getDefaultCurrency().format(new BigDecimal(this.getLeftClickBaseAmount()))));
		tokens.put("gts_button_currency_right_click", src -> Optional.of(service.getDefaultCurrency().format(new BigDecimal(this.getRightClickBaseAmount()))));
		tokens.put("gts_button_currency_shift_left_click", src -> Optional.of(service.getDefaultCurrency().format(new BigDecimal(this.getLeftClickShiftAmount()))));
		tokens.put("gts_button_currency_shift_right_click", src -> Optional.of(service.getDefaultCurrency().format(new BigDecimal(this.getRightClickShiftAmount()))));
		tokens.put("gts_button_time_left_click", src -> Optional.of(Text.of("00:01:00")));
		tokens.put("gts_button_time_right_click", src -> Optional.of(Text.of("00:10:00")));
		tokens.put("gts_button_time_shift_left_click", src -> Optional.of(Text.of("01:00:00")));
		tokens.put("gts_button_time_shift_right_click", src -> Optional.of(Text.of("10:00:00")));

		this.increase = Icon.from(
				ItemStack.builder()
						.itemType(ItemTypes.DYE)
						.add(Keys.DYE_COLOR, DyeColors.LIME)
						.add(Keys.DISPLAY_NAME, TextParsingUtils.fetchAndParseMsg(player, MsgConfigKeys.BUTTONS_INCREASE_CURRENCY_TITLE, null, null))
						.add(Keys.ITEM_LORE, TextParsingUtils.fetchAndParseMsgs(player, MsgConfigKeys.BUTTONS_INCREASE_CURRENCY_LORE, tokens, null))
						.build()
		);
		this.increase.addListener(clickable -> {
			double current = this.amount.doubleValue();

			if(clickable.getEvent() instanceof ClickInventoryEvent.Shift) {
				if(clickable.getEvent() instanceof ClickInventoryEvent.Shift.Primary) {
					this.amount = new BigDecimal(Math.min(this.getMax(), current + this.getLeftClickShiftAmount()));
				} else {
					this.amount = new BigDecimal(Math.min(this.getMax(), current + this.getRightClickShiftAmount()));
				}
			} else if (clickable.getEvent() instanceof ClickInventoryEvent.Primary) {
				this.amount = new BigDecimal(Math.min(this.getMax(), current + this.getLeftClickBaseAmount()));
			} else if(clickable.getEvent() instanceof ClickInventoryEvent.Secondary) {
				this.amount = new BigDecimal(Math.min(this.getMax(), current + this.getRightClickBaseAmount()));
			}
			this.update();
		});

		this.money = this.moneyIcon();

		this.decrease = Icon.from(
				ItemStack.builder()
						.itemType(ItemTypes.DYE)
						.add(Keys.DYE_COLOR, DyeColors.RED)
						.add(Keys.DISPLAY_NAME, TextParsingUtils.fetchAndParseMsg(player, MsgConfigKeys.BUTTONS_DECREASE_CURRENCY_TITLE, null, null))
						.add(Keys.ITEM_LORE, TextParsingUtils.fetchAndParseMsgs(player, MsgConfigKeys.BUTTONS_DECREASE_CURRENCY_LORE, tokens, null))
						.build()
		);
		this.decrease.addListener(clickable -> {
			double current = this.amount.doubleValue();
			if(clickable.getEvent() instanceof ClickInventoryEvent.Shift) {
				if(clickable.getEvent() instanceof ClickInventoryEvent.Shift.Primary) {
					this.amount = new BigDecimal(Math.max(this.getMin(), current - this.getLeftClickShiftAmount()));
				} else {
					this.amount = new BigDecimal(Math.max(this.getMin(), current - this.getRightClickShiftAmount()));
				}
			} else if (clickable.getEvent() instanceof ClickInventoryEvent.Primary) {
				this.amount = new BigDecimal(Math.max(this.getMin(), current - this.getLeftClickBaseAmount()));
			} else if(clickable.getEvent() instanceof ClickInventoryEvent.Secondary) {
				this.amount = new BigDecimal(Math.max(this.getMin(), current - this.getRightClickBaseAmount()));
			}
			this.update();
		});

		this.timeInc = Icon.from(
				ItemStack.builder()
						.itemType(ItemTypes.DYE)
						.add(Keys.DYE_COLOR, DyeColors.LIME)
						.add(Keys.DISPLAY_NAME, TextParsingUtils.fetchAndParseMsg(player, MsgConfigKeys.BUTTONS_INCREASE_TIME_TITLE, null, null))
						.add(Keys.ITEM_LORE, TextParsingUtils.fetchAndParseMsgs(player, MsgConfigKeys.BUTTONS_INCREASE_TIME_LORE, tokens, null))
						.build()
		);
		this.timeInc.addListener(clickable -> {
			if(clickable.getEvent() instanceof ClickInventoryEvent.Shift) {
				if(clickable.getEvent() instanceof ClickInventoryEvent.Shift.Primary) {
					this.time = Math.min(this.getTimeMax(), time + 3600);
				} else {
					this.time = Math.min(this.getTimeMax(), time + (3600 * 10));
				}
			} else if (clickable.getEvent() instanceof ClickInventoryEvent.Primary) {
				this.time = Math.min(this.getTimeMax(), time + 60);
			} else if(clickable.getEvent() instanceof ClickInventoryEvent.Secondary) {
				this.time = Math.min(this.getTimeMax(), time + 600);
			}
			this.update();
		});

		this.timeIcon = this.timeIcon();

		this.timeDec = Icon.from(
				ItemStack.builder()
						.itemType(ItemTypes.DYE)
						.add(Keys.DYE_COLOR, DyeColors.RED)
						.add(Keys.DISPLAY_NAME, TextParsingUtils.fetchAndParseMsg(player, MsgConfigKeys.BUTTONS_DECREASE_TIME_TITLE, null, null))
						.add(Keys.ITEM_LORE, TextParsingUtils.fetchAndParseMsgs(player, MsgConfigKeys.BUTTONS_DECREASE_TIME_LORE, tokens, null))
						.build()
		);
		this.timeDec.addListener(clickable -> {
			if(clickable.getEvent() instanceof ClickInventoryEvent.Shift) {
				if(clickable.getEvent() instanceof ClickInventoryEvent.Shift.Primary) {
					this.time = Math.max(this.getTimeMin(), time - 3600);
				} else {
					this.time = Math.max(this.getTimeMin(), time - (3600 * 10));
				}
			} else if (clickable.getEvent() instanceof ClickInventoryEvent.Primary) {
				this.time = Math.max(this.getTimeMin(), time - 60);
			} else if(clickable.getEvent() instanceof ClickInventoryEvent.Secondary) {
				this.time = Math.max(this.getTimeMin(), time - 600);
			}
			this.update();
		});
	}

	public abstract EntryUI createFor(Player player);

	public abstract UI getDisplay();

	protected abstract UI createUI(Player player);

	protected abstract Layout forgeLayout(Player player);

	protected abstract double getMin();

	protected abstract double getMax();

	protected abstract long getTimeMin();

	protected abstract long getTimeMax();

	protected abstract void update();

	protected Icon moneyIcon() {
		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("gts_price", src -> Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().format(this.amount)));
		tokens.put("gts_min_price", src -> Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().format(new BigDecimal(this.getMin()))));
		tokens.put("gts_max_price", src -> Optional.of(GTS.getInstance().getEconomy().getDefaultCurrency().format(new BigDecimal(this.getMax()))));

		return Icon.from(ItemStack.builder()
				.itemType(ItemTypes.GOLD_INGOT)
				.add(Keys.DISPLAY_NAME, TextParsingUtils.fetchAndParseMsg(null, MsgConfigKeys.PRICE_DISPLAY_TITLE, null, null))
				.add(Keys.ITEM_LORE, TextParsingUtils.fetchAndParseMsgs(null, MsgConfigKeys.PRICE_DISPLAY_LORE, tokens, null))
				.build());
	}

	protected Icon timeIcon() {
		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("gts_time", src -> Optional.of(Text.of(new Time(this.time).toString())));
		tokens.put("gts_min_time", src -> Optional.of(Text.of(new Time(this.getTimeMin()).toString())));
		tokens.put("gts_max_time", src -> Optional.of(Text.of(new Time(this.getTimeMax()).toString())));

		return Icon.from(ItemStack.builder()
				.itemType(ItemTypes.CLOCK)
				.add(Keys.DISPLAY_NAME, TextParsingUtils.fetchAndParseMsg(null, MsgConfigKeys.TIME_DISPLAY_TITLE, null, null))
				.add(Keys.ITEM_LORE, TextParsingUtils.fetchAndParseMsgs(null, MsgConfigKeys.TIME_DISPLAY_LORE, tokens, null))
				.build());
	}

	public abstract double getLeftClickBaseAmount();
	public abstract double getRightClickBaseAmount();
	public abstract double getLeftClickShiftAmount();
	public abstract double getRightClickShiftAmount();
}
