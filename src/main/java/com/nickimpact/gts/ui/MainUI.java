package com.nickimpact.gts.ui;

import com.google.common.collect.Lists;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.api.gui.InventoryBase;
import com.nickimpact.gts.api.gui.InventoryIcon;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.ui.shared.SharedItems;
import com.nickimpact.gts.utils.ItemUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class MainUI extends InventoryBase implements Observer {

	/** The current viewing index of listings */
	private int page;

	/** The condition to search by for the listings */
	private Predicate<Listing> searchCondition;

	/**
	 * Constructs an Inventory to which listings from the GTS are displayed.
	 *
	 * @param player The player to open the UI for
	 * @param page The page index to show for the listings
	 */
	public MainUI(Player player, int page) {
		this(player, page, null);
	}

	/**
	 * Constructs an Inventory UI to which listings from the GTS are displayed. Unlike the other constructor,
	 * this format is able to specify a search condition, to which the listings displayed must match said
	 * condition.
	 *
	 * @param player The player to open the UI for
	 * @param page The page index to show for the listings
	 * @param predicate The search condition to apply to the listings available
	 */
	public MainUI(Player player, int page, @Nullable Predicate<Listing> predicate) {
		super(player, 6, Text.of(TextColors.RED, "GTS ", TextColors.GRAY, "\u00BB ", TextColors.DARK_AQUA, "Listings"));

		this.page = page;
		this.searchCondition = predicate;

		this.drawInventory();
		GTS.getInstance().getUpdater().addObserver(this);
	}

	private void drawInventory() {
		this.drawDesign();

		List<Listing> listings;
		if(this.searchCondition != null)
			listings = GTS.getInstance().getListingsCache().stream().filter(this.searchCondition).collect(Collectors.toList());
		else
			listings = GTS.getInstance().getListingsCache();
		this.drawListings(listings);
	}

	/**
	 * Attempts to draw a set of listings based on the current page index. The valid range that these
	 * listings can be drawn is within the first four rows of a 6 row inventory, and the first 6 columns.
	 *
	 * @param listings The set of listings to draw, based on the page index
	 */
	private void drawListings(List<Listing> listings) {
		int x;
		int y = 0;

		GTS.getInstance().getConsole().ifPresent(console -> console.sendMessage(Text.of(GTSInfo.DEBUG_PREFIX, "Size: " + listings.size())));

		int index = 24 * (page - 1);

		for(; y < 4; y++) {
			for(x = 0; x < 6; x++, index++) {
				if(index >= listings.size())
					break;
				final int pos = index;
				InventoryIcon icon = new InventoryIcon(x + (9 * y), listings.get(pos).getDisplay(this.player, false));
				icon.addListener(ClickInventoryEvent.class, e -> {
					Listing l = listings.get(pos);
					int id = l.getID();

					if(GTS.getInstance().getListingsCache().stream().anyMatch(listing -> listing.getID() == id)) {
						Sponge.getScheduler().createTaskBuilder()
								.execute(() -> {
									this.player.closeInventory();
									GTS.getInstance().getUpdater().deleteObserver(this);

									this.player.openInventory(new ConfirmUI(this.player, l, searchCondition).getInventory());
								})
								.delayTicks(1)
								.submit(GTS.getInstance());
					} else {
						// Send error message about the lot already being purchased, then update the inventory
					}
				});

				this.addIcon(icon);
			}
		}
	}

	/**
	 * Draws the physical border of the main menu.
	 */
	private void drawDesign() {
		// Draw the entire 5th row full of border icons
		for(int x = 0, y = 4; x < 9; x++) {
			this.addIcon(SharedItems.forgeBorderIcon(x + 9 * y, DyeColors.BLACK));
		}

		// Draw the rectangle around the page options (exclude bottom row)
		for(int x = 6; x < 9; x++) {
			this.addIcon(SharedItems.forgeBorderIcon(x, DyeColors.BLACK));
		}

		for(int y = 1; y < 4; y++) {
			for(int x = 6; x < 9; x += 2) {
				this.addIcon(SharedItems.forgeBorderIcon(x + 9 * y, DyeColors.BLACK));
			}
		}

		ItemStack skull = ItemUtils.createSkull(player.getUniqueId(), Text.of(TextColors.YELLOW, "Player Info"), Lists.newArrayList());
		this.addIcon(new InventoryIcon(49, skull));
	}

	private void drawSidePanel() {

	}

	private void drawActionPanel() {

	}

	@Override
	public void update(Observable o, Object arg) {
		List<Listing> listings;
		if(this.searchCondition != null)
			listings = GTS.getInstance().getListingsCache().stream().filter(this.searchCondition).collect(Collectors.toList());
		else
			listings = GTS.getInstance().getListingsCache();
		this.drawListings(listings);
	}
}
