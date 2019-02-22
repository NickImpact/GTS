package me.nickimpact.gts.ui;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.gui.v2.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.configuration.MsgConfigKeys;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.internal.TextParsingUtils;
import me.nickimpact.gts.utils.ItemUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class MainUI implements Observer {

	private static Map<UUID, Instant> delays = Maps.newHashMap();

	/** The player viewing the UI */
	private Player player;

	/** The current viewing index of listings */
	private QueriedPage<Listing> page;

	/** The condition to search by for the listings */
	private Collection<Predicate<Listing>> searchConditions = Lists.newArrayList();

	private List<Class<? extends Entry>> classSelections = Lists.newArrayList();

	/** Whether or not we should show the player's listings or not */
	private boolean justPlayer = false;

	private static final Icon BORDER = Icon.from(ItemStack.builder().from(Icon.BORDER.getDisplay()).add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Click to refresh UI")).build());

	/** These two fields represent the current set of categories we can use to filter our listings */
	private List<Icon> categories = Lists.newArrayList();
	private int index = 0;

	public MainUI(Player player) {
		this(player, Collections.emptyList());
	}

	/**
	 *
	 *
	 * @param player The player to open the UI for
	 * @param conditions The search condition to apply to the listings available
	 */
	public MainUI(Player player, Collection<Predicate<Listing>> conditions) {
		this.player = player;
		this.searchConditions.addAll(conditions);
		this.searchConditions.add(listing -> !listing.hasExpired());
		this.searchConditions.add(listing -> {
			if(this.classSelections.isEmpty()) return true;

			for (Class<? extends Entry> entry : classSelections) {
				if (listing.getEntry().getClass().isAssignableFrom(entry)) {
					return true;
				}
			}
			return false;
		});

		this.page = QueriedPage.builder()
				.title(Text.of(TextColors.RED, "GTS ", TextColors.GRAY, "\u00BB ", TextColors.DARK_AQUA, "Listings"))
				.view(this.design())
				.viewer(player)
				.previousPage(Sponge.getRegistry().getType(ItemType.class, "pixelmon:trade_holder_left").orElse(ItemTypes.BARRIER), 52)
				.nextPage(Sponge.getRegistry().getType(ItemType.class, "pixelmon:trade_holder_right").orElse(ItemTypes.BARRIER), 53)
				.contentZone(InventoryDimension.of(7, 3))
				.offsets(1)
				.build(GTS.getInstance());
		this.page = this.page.applier(listing -> {
			Icon icon = Icon.from(listing.getDisplay(player, false));
			icon.addListener(clickable -> {
				UUID uuid = listing.getUuid();
				if(GTS.getInstance().getListingsCache().stream().anyMatch(listing1 -> listing1.getUuid() == uuid)) {
					Sponge.getScheduler().createTaskBuilder()
							.execute(() -> new ConfirmUI(this.player, listing, searchConditions).open(player))
							.delayTicks(1)
							.submit(GTS.getInstance());
				}
			});

			return icon;
		});
		this.page.define(this.getListings());
		GTS.getInstance().getUpdater().addObserver(this);
		this.page.getView().setCloseAction((event, pl) -> GTS.getInstance().getUpdater().deleteObserver(this));
	}

	public void open() {
		this.page.open();
	}

	private Layout design() {
		Layout.Builder lb = Layout.builder().dimension(9, 6);
		lb.row(BORDER, 0).row(BORDER, 4);
		lb.column(BORDER, 0).column(BORDER, 8);
		lb.slots(BORDER, 46, 51);

		Text pLTitle = TextParsingUtils.fetchAndParseMsg(this.player, MsgConfigKeys.UI_ITEMS_PLAYER_LISTINGS_TITLE, null, null);
		List<Text> pLLore = Lists.newArrayList(Text.of(TextColors.GRAY, "Status: ", this.justPlayer ? Text.of(TextColors.GREEN, "Enabled") : Text.of(TextColors.RED, "Disabled")));
		ImmutableList<Text> additional = ImmutableList.copyOf(TextParsingUtils.fetchAndParseMsgs(this.player, MsgConfigKeys.UI_ITEMS_PLAYER_LISTINGS_LORE, null, null));
		pLLore.addAll(additional);
		ItemStack pListings = ItemStack.builder().itemType(ItemTypes.WRITTEN_BOOK).add(Keys.DISPLAY_NAME, pLTitle).add(Keys.ITEM_LORE, pLLore).build();
		Icon pl = new Icon(pListings);
		pl.addListener(clickable -> {
			this.justPlayer = !this.justPlayer;
			List<Text> lore = Lists.newArrayList(Text.of(TextColors.GRAY, "Status: ", this.justPlayer ? Text.of(TextColors.GREEN, "Enabled") : Text.of(TextColors.RED, "Disabled")));
			lore.addAll(additional);
			pl.getDisplay().offer(Keys.ITEM_LORE, lore);
			this.page.getView().setSlot(45, pl);
			this.page.getView().clear(
					10, 11, 12, 13, 14, 15, 16,
					19, 20, 21, 22, 23, 24, 25,
					28, 29, 30, 31, 32, 33, 34
			);
			this.apply();
		});
		lb.slot(pl, 45);

		// Setup Entry Selector
		// This needs to be scrollable within the layout, such that only two appear at once.
		GTS.getInstance().getService().getEntryRegistry().getClassifications().forEach(classification -> {
			String identifier = classification.getIdentifer();
			ItemStack rep = ItemStack.builder().itemType(Sponge.getRegistry().getType(ItemType.class, classification.getItemRep()).orElse(ItemTypes.BARRIER)).build();
			rep.offer(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Show only ", identifier, "?"));
			rep.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "Status: ", TextColors.RED, "Disabled")));

			Icon icon = Icon.from(rep);
			icon.addListener(clickable -> {
				if(delays.containsKey(clickable.getPlayer().getUniqueId())) {
					if(!Instant.now().isAfter(delays.get(clickable.getPlayer().getUniqueId()))) {
						return;
					}
				}
				if(this.hasEntryType(classification.getClassification())) {
					this.classSelections.remove(classification.getClassification());
					rep.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "Status: ", TextColors.RED, "Disabled")));
				} else {
					this.classSelections.add(classification.getClassification());
					rep.offer(Keys.ITEM_LORE, Lists.newArrayList(Text.of(TextColors.GRAY, "Status: ", TextColors.GREEN, "Enabled")));
				}
				this.updateOptions();
				this.apply();
				delays.put(clickable.getPlayer().getUniqueId(), Instant.now().plusSeconds(3));
			});

			this.categories.add(icon);
		});

		for(int i = 0; i < this.categories.size() && i < 2; i++) {
			lb.slot(this.categories.get(i), 48 + i);
		}

		return lb.build();
	}

	private boolean hasEntryType(Class<? extends Entry> entry) {
		return this.classSelections.contains(entry);
	}

	private void updateOptions() {
		this.page.getView().setSlot(48, this.categories.get(index));
		this.page.getView().setSlot(49, this.categories.get(index + 1));
	}

	private void apply() {
		this.page.define(this.getListings());
	}

	private List<Listing> getListings() {
		List<Listing> listings;
		if(justPlayer) {
			listings = GTS.getInstance().getListingsCache().stream().filter(listing -> listing.getOwnerUUID().equals(this.player.getUniqueId())).collect(Collectors.toList());
		} else {
			if (!this.searchConditions.isEmpty()) {
				listings = GTS.getInstance().getListingsCache().stream().filter(listing -> {
					boolean passed = false;
					for(Predicate<Listing> predicate : this.searchConditions) {
						passed = predicate.test(listing);
					}

					return passed;
				}).collect(Collectors.toList());
			} else {
				listings = GTS.getInstance().getListingsCache();
			}
		}

		listings = listings.stream().filter(listing -> !listing.hasExpired()).collect(Collectors.toList());
		return listings;
	}

	@Override
	public void update(Observable o, Object arg) {
		this.apply();
	}
}
