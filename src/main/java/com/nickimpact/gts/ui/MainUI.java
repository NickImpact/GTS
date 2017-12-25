package com.nickimpact.gts.ui;

import com.google.common.collect.Lists;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.api.configuration.MsgConfigKeys;
import com.nickimpact.gts.api.gui.InventoryBase;
import com.nickimpact.gts.api.gui.Icon;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.ui.shared.SharedItems;
import com.nickimpact.gts.utils.ItemUtils;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;
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
		Sponge.getScheduler().createTaskBuilder().execute(() -> {
			this.drawListings(getListings());
			this.listingUpdate();
		}).interval(1, TimeUnit.SECONDS).name("Main-" + player.getName()).submit(GTS.getInstance());
	}

	private List<Listing> getListings() {
		List<Listing> listings;
		if(this.searchCondition != null)
			listings = GTS.getInstance().getListingsCache().stream().filter(this.searchCondition).collect(Collectors.toList());
		else
			listings = GTS.getInstance().getListingsCache();

		listings = listings.stream().filter(listing -> !listing.checkHasExpired()).collect(Collectors.toList());

		return listings;
	}

	private void drawInventory() {
		this.drawDesign();
		this.drawSidePanel();
		this.drawActionPanel();
		this.drawListings(getListings());
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

		GTS.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.DEBUG_PREFIX, "Drawing listings")));

		int index = 24 * (page - 1);

		for(; y < 4; y++) {
			for(x = 0; x < 6; x++, index++) {
				if(index >= listings.size())
					break;
				final int pos = index;
				Icon icon = new Icon(x + (9 * y), listings.get(pos).getDisplay(this.player, false));
				icon.addListener(clickable -> {
					Listing l = listings.get(pos);
					int id = l.getID();

					if(GTS.getInstance().getListingsCache().stream().anyMatch(listing -> listing.getID() == id)) {
						Sponge.getScheduler().createTaskBuilder()
								.execute(() -> {
									clickable.getPlayer().closeInventory();
									GTS.getInstance().getUpdater().deleteObserver(this);

									clickable.getPlayer().openInventory(new ConfirmUI(this.player, l, searchCondition).getInventory());
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
	}

	private void drawSidePanel() {
		// Page up = 16
		// Page down = 25
		// Refresh = 34

		try {
			Icon up = new Icon(
					16,
					ItemStack.builder()
							.itemType(ItemTypes.TIPPED_ARROW)
							.add(Keys.POTION_EFFECTS, Lists.newArrayList(
									PotionEffect.builder()
											.amplifier(0)
											.duration(100)
											.potionType(PotionEffectTypes.JUMP_BOOST)
											.build()
							))
							.add(Keys.DISPLAY_NAME, GTS.getInstance().getTextParsingUtils().parse(
									GTS.getInstance().getMsgConfig().get(MsgConfigKeys.UI_ITEMS_NEXT_PAGE),
									player,
									null,
									null
							))
							.add(Keys.HIDE_ATTRIBUTES, true)
							.add(Keys.HIDE_MISCELLANEOUS, true)
							.build()
			);
			up.addListener(clickable -> {

			});
			Icon down = new Icon(
					25,
					ItemStack.builder()
							.itemType(ItemTypes.TIPPED_ARROW)
							.add(Keys.POTION_EFFECTS, Lists.newArrayList(
									PotionEffect.builder()
											.amplifier(0)
											.duration(100)
											.potionType(PotionEffectTypes.INSTANT_HEALTH)
											.build()
							))
							.add(Keys.DISPLAY_NAME, GTS.getInstance().getTextParsingUtils().parse(
									GTS.getInstance().getMsgConfig().get(MsgConfigKeys.UI_ITEMS_LAST_PAGE),
									player,
									null,
									null
							))
							.add(Keys.HIDE_ATTRIBUTES, true)
							.add(Keys.HIDE_MISCELLANEOUS, true)
							.build()
			);
			Icon refresh = new Icon(
					34,
					ItemStack.builder()
							.itemType(ItemTypes.TOTEM_OF_UNDYING)
							.add(Keys.DISPLAY_NAME, GTS.getInstance().getTextParsingUtils().parse(
									GTS.getInstance().getMsgConfig().get(MsgConfigKeys.UI_ITEMS_REFRESH),
									player,
									null,
									null
							))
							.add(Keys.HIDE_ATTRIBUTES, true)
							.add(Keys.HIDE_MISCELLANEOUS, true)
							.build()
			);
			refresh.addListener(clickable -> {
				this.drawListings(this.getListings());
				this.listingUpdate();
			});
			this.addIcon(up);
			this.addIcon(down);
			this.addIcon(refresh);
		} catch (NucleusException e) {
			e.printStackTrace();
		}
	}

	private void drawActionPanel() {
		try {
			ItemStack sort = ItemStack.builder()
					.itemType(ItemTypes.FILLED_MAP)
					.add(Keys.DISPLAY_NAME, GTS.getInstance().getTextParsingUtils().parse(
							GTS.getInstance().getMsgConfig().get(MsgConfigKeys.UI_ITEMS_SORT_TITLE),
							player,
							null,
							null
					))
					.build();
			Icon icon = new Icon(47, sort);
			icon.addListener(clickable -> {
				GTS.getInstance().getConsole().ifPresent(console -> {
					console.sendMessages(
							Text.of(GTSInfo.DEBUG_PREFIX, "Click Action Test"),
							Text.of(GTSInfo.DEBUG_PREFIX, "Player: " + clickable.getPlayer().getName()),
							Text.of(GTSInfo.DEBUG_PREFIX, "Event: " + clickable.getEvent().getClass())
					);
				});
			});
			this.addIcon(icon);
		} catch (NucleusException e) {
			e.printStackTrace();
		}

		ItemStack skull = ItemUtils.createSkull(player.getUniqueId(), Text.of(TextColors.YELLOW, "Player Info"), Lists.newArrayList());
		this.addIcon(new Icon(49, skull));

		ItemStack possession = ItemStack.builder().itemType(ItemTypes.WRITTEN_BOOK).add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Your Listings")).build();
		this.addIcon(new Icon(51, possession));
	}

	@Override
	public void update(Observable o, Object arg) {
		List<Listing> listings;
		if(this.searchCondition != null)
			listings = GTS.getInstance().getListingsCache().stream().filter(this.searchCondition).collect(Collectors.toList());
		else
			listings = GTS.getInstance().getListingsCache();
		this.drawListings(listings);
		this.listingUpdate();
	}

	private void listingUpdate() {
		this.updateContents(
				0, 1, 2, 3, 4, 5,
				9, 10, 11, 12, 13, 14,
				18, 19, 20, 21, 22, 23,
				27, 28, 29, 30, 31, 32
		);
	}
}
