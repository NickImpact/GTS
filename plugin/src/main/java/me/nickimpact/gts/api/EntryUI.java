package me.nickimpact.gts.api;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.gui.v2.Icon;
import com.nickimpact.impactor.gui.v2.Layout;
import com.nickimpact.impactor.gui.v2.UI;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;

public abstract class EntryUI {

	private Player player;

	/** Represents the amount of money being used to put the listing up for */
	private BigDecimal amount;

	/** Icons which handle the price increase setup */
	private Icon increase;
	private Icon money;
	private Icon decrease;

	public EntryUI(Player player) {
		this.player = player;

		this.increase = Icon.from(
				ItemStack.builder()
						.add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Increase Amount Requested"))
						.add(Keys.ITEM_LORE, Lists.newArrayList(
								Text.of(TextColors.AQUA, "Left Click: +1"),
								Text.of(TextColors.AQUA, "Right Click: +10"),
								Text.of(TextColors.AQUA, "Shift + Left Click: +100"),
								Text.of(TextColors.AQUA, "Shift + Right Click: +1000")
						))
						.build()
		);
		this.increase.addListener(clickable -> {
			if(clickable.getEvent() instanceof ClickInventoryEvent.Shift) {
				if(clickable.getEvent() instanceof ClickInventoryEvent.Shift.Primary) {
					this.amount = this.amount.add(new BigDecimal(100));
				} else {
					this.amount = this.amount.add(new BigDecimal(1000));
				}
			} else if (clickable.getEvent() instanceof ClickInventoryEvent.Primary) {
				this.amount = this.amount.add(new BigDecimal(1));
			} else if(clickable.getEvent() instanceof ClickInventoryEvent.Secondary) {
				this.amount = this.amount.add(new BigDecimal(10));
			}
		});

		this.money = Icon.from(ItemStack.builder()
				.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Listing Price"))
				.add(Keys.ITEM_LORE, Lists.newArrayList(

				))
				.build());
	}

	public abstract UI getDisplay();

	protected abstract UI createUI(Player player);

	protected abstract Layout forgeLayout(Player player);
}
