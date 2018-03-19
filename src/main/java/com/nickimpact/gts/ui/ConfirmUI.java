package com.nickimpact.gts.ui;

import com.google.common.collect.Maps;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.api.gui.Icon;
import com.nickimpact.gts.api.gui.InventoryBase;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.configuration.MsgConfigKeys;
import com.nickimpact.gts.ui.shared.SharedItems;
import com.nickimpact.gts.utils.ListingUtils;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class ConfirmUI extends InventoryBase {

	/** The listing that the player might wish to purchase */
	private Listing target;

	/** The lingering search condition that can be restored if the user cancels purchase */
	private Predicate<Listing> search;

	public ConfirmUI(Player player, Listing listing, @Nullable Predicate<Listing> search) {
		super(player, 3, Text.of(TextColors.RED, "GTS ", TextColors.GRAY, "\u00BB ", TextColors.DARK_AQUA, "Confirmation"));
		this.target = listing;
		this.search = search;

		this.drawDesign();

		Sponge.getScheduler().createTaskBuilder()
				.execute(this::drawTarget)
				.interval(1, TimeUnit.SECONDS)
				.name("Confirm-" + player.getName())
				.submit(GTS.getInstance());
	}

	@Override
	protected void processClose(InteractInventoryEvent.Close event) {
		Sponge.getScheduler().getTasksByName("Confirm-" + player.getName()).forEach(Task::cancel);
	}

	private void drawDesign() {
		// Draw the typical Nick border
		// Place listing on the left
		// Draw confirm, deny, and more info icons appropriately
		this.drawBorder(3, DyeColors.BLACK);
		this.addIcon(new Icon(10, this.target.getDisplay(player, false)));
		this.addIcon(SharedItems.forgeBorderIcon(11, DyeColors.GRAY));
		this.addIcon(new Icon(14, this.target.getDisplay(player, true)));
		this.drawConfirmIcon();
		this.drawDenyIcon();
	}

	private void drawTarget() {
		this.addIcon(new Icon(10, this.target.getDisplay(player, false)));
		this.updateContents(10);
	}

	private void drawConfirmIcon() {
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("dummy", target.getEntry().getElement());
		variables.put("dummy2", target);
		variables.put("dummy3", target.getEntry());

		if(this.target.getOwnerUUID().equals(this.player.getUniqueId())) {
			Icon icon = new Icon(
					12,
					ItemStack.builder()
							.itemType(ItemTypes.ANVIL)
							.add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Remove from GTS"))
							.build()
			);
			icon.addListener(clickable -> {
				if(!GTS.getInstance().getListingsCache().contains(this.target)) {
					clickable.getPlayer().sendMessages(
							Text.of(GTSInfo.ERROR, "Unfortunately, your listing has already been claimed...")
					);
					return;
				}
				this.target.getEntry().giveEntry(clickable.getPlayer());
				ListingUtils.deleteEntry(this.target);
				try {
					clickable.getPlayer().sendMessages(
							GTS.getInstance().getTextParsingUtils().parse(
									GTS.getInstance().getMsgConfig().get(MsgConfigKeys.REMOVAL_CHOICE),
									player,
									null,
									variables
							)
					);
				} catch (NucleusException e) {
					e.printStackTrace();
				}
				clickable.getPlayer().closeInventory();
			});
			this.addIcon(icon);
		} else {
			Icon icon = SharedItems.confirmIcon(12);
			icon.addListener(clickable -> {
				if (this.target.getAucData() != null) {
					ListingUtils.bid(clickable.getPlayer(), this.target);
				} else {
					ListingUtils.purchase(clickable.getPlayer(), this.target);
				}

				clickable.getPlayer().closeInventory();
			});
			this.addIcon(icon);
		}
	}

	private void drawDenyIcon() {
		Icon icon = SharedItems.denyIcon(16);
		icon.addListener(clickable -> {
			clickable.getPlayer().closeInventory();
			clickable.getPlayer().openInventory(new MainUI(clickable.getPlayer(), 1, search).getInventory());
		});
		this.addIcon(icon);
	}
}
