package me.nickimpact.gts.api.listings.entries;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.gui.v2.Displayable;
import com.nickimpact.impactor.gui.v2.Icon;
import com.nickimpact.impactor.gui.v2.Layout;
import com.nickimpact.impactor.gui.v2.UI;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.time.Time;
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

		this.increase = Icon.from(
				ItemStack.builder()
						.itemType(ItemTypes.DYE)
						.add(Keys.DYE_COLOR, DyeColors.LIME)
						.add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Increase Amount Requested"))
						.add(Keys.ITEM_LORE, Lists.newArrayList(
								Text.of(TextColors.GRAY, "Left Click: ", TextColors.AQUA, "+", service.getDefaultCurrency().format(new BigDecimal(this.getLeftClickBaseAmount()))),
								Text.of(TextColors.GRAY, "Right Click: ", TextColors.AQUA, "+", service.getDefaultCurrency().format(new BigDecimal(this.getRightClickBaseAmount()))),
								Text.of(TextColors.GRAY, "Shift + Left Click: ", TextColors.AQUA, "+", service.getDefaultCurrency().format(new BigDecimal(this.getLeftClickShiftAmount()))),
								Text.of(TextColors.GRAY, "Shift + Right Click: ", TextColors.AQUA, "+", service.getDefaultCurrency().format(new BigDecimal(this.getRightClickShiftAmount())))
						))
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
						.add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Decrease Amount Requested"))
						.add(Keys.ITEM_LORE, Lists.newArrayList(
								Text.of(TextColors.GRAY, "Left Click: ", TextColors.AQUA, "-", service.getDefaultCurrency().format(new BigDecimal(this.getLeftClickBaseAmount()))),
								Text.of(TextColors.GRAY, "Right Click: ", TextColors.AQUA, "-", service.getDefaultCurrency().format(new BigDecimal(this.getRightClickBaseAmount()))),
								Text.of(TextColors.GRAY, "Shift + Left Click: ", TextColors.AQUA, "-", service.getDefaultCurrency().format(new BigDecimal(this.getLeftClickShiftAmount()))),
								Text.of(TextColors.GRAY, "Shift + Right Click: ", TextColors.AQUA, "-", service.getDefaultCurrency().format(new BigDecimal(this.getRightClickShiftAmount())))
						))
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
						.add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Increase Time"))
						.add(Keys.ITEM_LORE, Lists.newArrayList(
								Text.of(TextColors.GRAY, "Left Click: ", TextColors.AQUA, "+1 minute"),
								Text.of(TextColors.GRAY, "Right Click: ", TextColors.AQUA, "+10 minutes"),
								Text.of(TextColors.GRAY, "Shift + Left Click: ", TextColors.AQUA, "+1 hour"),
								Text.of(TextColors.GRAY, "Shift + Right Click: ", TextColors.AQUA, "+10 hours")
						))
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
						.add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Decrease Time"))
						.add(Keys.ITEM_LORE, Lists.newArrayList(
								Text.of(TextColors.GRAY, "Left Click: ", TextColors.AQUA, "-1 minute"),
								Text.of(TextColors.GRAY, "Right Click: ", TextColors.AQUA, "-10 minutes"),
								Text.of(TextColors.GRAY, "Shift + Left Click: ", TextColors.AQUA, "-1 hour"),
								Text.of(TextColors.GRAY, "Shift + Right Click: ", TextColors.AQUA, "-10 hours")
						))
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
		return Icon.from(ItemStack.builder()
				.itemType(ItemTypes.GOLD_INGOT)
				.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Listing Price"))
				.add(Keys.ITEM_LORE, Lists.newArrayList(
						Text.of(TextColors.GRAY, "Target Price: ", TextColors.GREEN, GTS.getInstance().getEconomy().getDefaultCurrency().format(this.amount)),
						Text.EMPTY,
						Text.of(TextColors.GRAY, "Min Price: ", TextColors.GREEN, GTS.getInstance().getEconomy().getDefaultCurrency().format(new BigDecimal(this.getMin()))),
						Text.of(TextColors.GRAY, "Max Price: ", TextColors.GREEN, GTS.getInstance().getEconomy().getDefaultCurrency().format(new BigDecimal(this.getMax()))))
				)
				.build());
	}

	protected Icon timeIcon() {
		return Icon.from(ItemStack.builder()
				.itemType(ItemTypes.CLOCK)
				.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Listing Time"))
				.add(Keys.ITEM_LORE, Lists.newArrayList(
						Text.of(TextColors.GRAY, "Target Time: ", TextColors.GREEN, new Time(this.time).toString()),
						Text.EMPTY,
						Text.of(TextColors.GRAY, "Min Time: ", TextColors.GREEN, new Time(this.getTimeMin()).toString()),
						Text.of(TextColors.GRAY, "Max Time: ", TextColors.GREEN, new Time(this.getTimeMax()).toString()))
				)
				.build());
	}

	public abstract double getLeftClickBaseAmount();
	public abstract double getRightClickBaseAmount();
	public abstract double getLeftClickShiftAmount();
	public abstract double getRightClickShiftAmount();
}
