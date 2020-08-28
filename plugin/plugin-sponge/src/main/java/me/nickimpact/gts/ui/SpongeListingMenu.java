package me.nickimpact.gts.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.Impactor;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.gui.InventoryDimensions;
import com.nickimpact.impactor.api.services.text.MessageService;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.auctions.Auction;
import me.nickimpact.gts.api.listings.buyitnow.BuyItNow;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.util.groupings.Tuple;
import me.nickimpact.gts.common.config.MsgConfigKeys;
import me.nickimpact.gts.common.config.wrappers.SortConfigurationOptions;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.common.utils.CircularLinkedList;
import me.nickimpact.gts.common.config.wrappers.TitleLorePair;
import me.nickimpact.gts.manager.SpongeListingManager;
import me.nickimpact.gts.sponge.listings.SpongeListing;
import me.nickimpact.gts.sponge.ui.SpongeAsyncPage;
import me.nickimpact.gts.util.Utilities;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import static me.nickimpact.gts.util.Utilities.readMessageConfigOption;

public class SpongeListingMenu extends SpongeAsyncPage<SpongeListing> {

	private static final QuickPurchaseOnly QUICK_PURCHASE_ONLY = new QuickPurchaseOnly();
	private static final AuctionsOnly AUCTIONS_ONLY = new AuctionsOnly();

	private static final MessageService<Text> PARSER = Utilities.PARSER;

	private Class<? extends Entry> filter;

	private Task runner;

	private String searchQuery = "Testing";

	/** True = Quick Purchase, false = Auction */
	private boolean mode = false;
	private CircularLinkedList<Sorter> sorter = Sorter.QUICK_PURCHASE_ONLY.copy();

	public SpongeListingMenu(Player viewer) {
		super(GTSPlugin.getInstance(), viewer, Impactor.getInstance().getRegistry().get(SpongeListingManager.class).fetchListings());
	}

	@Override
	protected Text getTitle() {
		return PARSER.parse(GTSPlugin.getInstance().getMsgConfig().get(MsgConfigKeys.UI_MENU_LISTINGS_TITLE), Lists.newArrayList(this::getViewer));
	}

	@Override
	protected Map<PageIconType, PageIcon<ItemType>> getPageIcons() {
		Map<PageIconType, PageIcon<ItemType>> options = Maps.newHashMap();
		options.put(PageIconType.PREV, new PageIcon<>(ItemTypes.ARROW, 47));
		options.put(PageIconType.NEXT, new PageIcon<>(ItemTypes.ARROW, 53));

		return options;
	}

	@Override
	protected InventoryDimensions getContentZone() {
		return new InventoryDimensions(7, 4);
	}

	@Override
	protected Tuple<Integer, Integer> getOffsets() {
		return new Tuple<>(0, 2);
	}

	@Override
	protected Tuple<Long, TimeUnit> getTimeout() {
		return new Tuple<>((long) 5, TimeUnit.SECONDS);
	}

	@Override
	protected SpongeLayout design() {
		SpongeLayout.SpongeLayoutBuilder slb = SpongeLayout.builder();
		slb.slots(SpongeIcon.BORDER, 0, 1, 10, 19, 28, 37, 38, 39, 40, 41, 42, 42, 43, 44, 36, 46, 48, 52);
		this.createBottomPanel(slb);
		this.createFilterOptions(slb);

		return slb.build();
	}

	@Override
	protected SpongeUI build(SpongeLayout layout) {
		return SpongeUI.builder()
				.title(this.title)
				.dimension(InventoryDimension.of(9, 6))
				.build()
				.define(this.layout);
	}

	@Override
	protected SpongeIcon getLoadingIcon() {
		return new SpongeIcon(ItemStack.builder()
				.itemType(ItemTypes.STAINED_GLASS_PANE)
				.add(Keys.DISPLAY_NAME, PARSER.parse(
						Utilities.readMessageConfigOption(MsgConfigKeys.UI_MENU_LISTINGS_SPECIAL_LOADING),
						Lists.newArrayList(this::getViewer)
				))
				.add(Keys.DYE_COLOR, DyeColors.YELLOW)
				.build()
		);
	}

	@Override
	protected SpongeIcon getTimeoutIcon() {
		TitleLorePair pair = Utilities.readMessageConfigOption(MsgConfigKeys.UI_MENU_LISTINGS_SPECIAL_TIMED_OUT);
		return new SpongeIcon(ItemStack.builder()
				.itemType(ItemTypes.STAINED_GLASS_PANE)
				.add(Keys.DISPLAY_NAME, PARSER.parse(pair.getTitle(), Lists.newArrayList(this::getViewer)))
				.add(Keys.DYE_COLOR, DyeColors.RED)
				.add(Keys.ITEM_LORE, PARSER.parse(pair.getLore(), Lists.newArrayList(this::getViewer)))
				.build()
		);
	}

	private void createFilterOptions(SpongeLayout.SpongeLayoutBuilder layout) {
//		int size = GTSService.getInstance().getEntryRegistry().getClassifications().size();
//		if(size > 6) {
//
//		} else {
//
//		}
	}

	private void createBottomPanel(SpongeLayout.SpongeLayoutBuilder layout) {
		ItemStack quick = ItemStack.builder()
				.itemType(ItemTypes.EMERALD)
				.add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Quick Purchase"))
				.add(Keys.ITEM_LORE, Lists.newArrayList(
						Text.of(TextColors.GRAY, "You are currently viewing"),
						Text.of(TextColors.AQUA, "Quick Purchase", TextColors.GRAY, " listings only!"),
						Text.EMPTY,
						Text.of(TextColors.YELLOW, "Click this to change your view"),
						Text.of(TextColors.YELLOW, "of listings to Auctions!")
				))
				.build();
		SpongeIcon qIcon = new SpongeIcon(quick);

		ItemStack auctions = ItemStack.builder()
				.itemType(ItemTypes.GOLD_INGOT)
				.add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Auctions"))
				.add(Keys.ITEM_LORE, Lists.newArrayList(
						Text.of(TextColors.GRAY, "You are currently viewing"),
						Text.of(TextColors.AQUA, "Auction", TextColors.GRAY, " listings only!"),
						Text.EMPTY,
						Text.of(TextColors.YELLOW, "Click this to change your view"),
						Text.of(TextColors.YELLOW, "to Quick Purchase Listings!")
				))
				.build();
		SpongeIcon aIcon = new SpongeIcon(auctions);

		TitleLorePair pair = GTSPlugin.getInstance().getMsgConfig().get(MsgConfigKeys.UI_MENU_LISTINGS_SEARCH);
		ItemStack searcher = ItemStack.builder()
				.itemType(ItemTypes.SIGN)
				.add(Keys.DISPLAY_NAME, PARSER.parse(pair.getTitle(), Lists.newArrayList(this::getViewer)))
				.add(Keys.ITEM_LORE, PARSER.parse(pair.getLore(), Lists.newArrayList(this::getViewer)))
				.build();
		SpongeIcon sIcon = new SpongeIcon(searcher);
		sIcon.addListener(clickable -> {

		});
		layout.slot(sIcon, 49);

		SpongeIcon sorter = this.drawSorter();
		layout.slot(sorter, 51);

		qIcon.addListener(clickable -> {
			this.conditions.remove(QUICK_PURCHASE_ONLY);
			this.conditions.add(AUCTIONS_ONLY);

			this.mode = true;
			this.sorter = Sorter.AUCTION_ONLY.copy();
			this.sorter.reset();
			sorter.getDisplay().offer(Keys.ITEM_LORE, PARSER.parse(this.craftSorterLore(GTSPlugin.getInstance().getMsgConfig().get(MsgConfigKeys.UI_MENU_LISTINGS_SORT)), Lists.newArrayList(this::getViewer)));
			this.getView().setSlot(51, sorter);

			this.getView().setSlot(50, aIcon);
			this.apply();
		});
		aIcon.addListener(clickable -> {
			this.conditions.add(QUICK_PURCHASE_ONLY);
			this.conditions.remove(AUCTIONS_ONLY);

			this.mode = false;
			this.sorter = Sorter.QUICK_PURCHASE_ONLY.copy();
			this.sorter.reset();
			sorter.getDisplay().offer(Keys.ITEM_LORE, PARSER.parse(this.craftSorterLore(GTSPlugin.getInstance().getMsgConfig().get(MsgConfigKeys.UI_MENU_LISTINGS_SORT)), Lists.newArrayList(this::getViewer)));
			this.getView().setSlot(51, sorter);

			this.getView().setSlot(50, qIcon);
			this.apply();
		});

		layout.slot(qIcon, 50);

		SpongeIcon back = new SpongeIcon(ItemStack.builder()
				.itemType(ItemTypes.BARRIER)
				.add(Keys.DISPLAY_NAME, PARSER.parse(readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK), Lists.newArrayList(this::getViewer)))
				.build()
		);
		back.addListener(clickable -> {
			this.cancelIfRunning();
			SpongeMainMenu menu = new SpongeMainMenu(this.getViewer());
			menu.open();
		});
		layout.slot(back, 45);
	}

	private SpongeIcon drawSorter() {
		SortConfigurationOptions options = GTSPlugin.getInstance().getMsgConfig().get(MsgConfigKeys.UI_MENU_LISTINGS_SORT);

		this.sorter.next();
		ItemStack sorter = ItemStack.builder()
				.itemType(ItemTypes.HOPPER)
				.add(Keys.DISPLAY_NAME, PARSER.parse(options.getTitle(), Lists.newArrayList(this::getViewer)))
				.add(Keys.ITEM_LORE, PARSER.parse(this.craftSorterLore(options), Lists.newArrayList(this::getViewer)))
				.build();
		SpongeIcon sortIcon = new SpongeIcon(sorter);
		sortIcon.addListener(clickable -> {
			this.sorter.next();
			sortIcon.getDisplay().offer(Keys.ITEM_LORE, PARSER.parse(this.craftSorterLore(options), Lists.newArrayList(this::getViewer)));

			this.getView().setSlot(51, sortIcon);
		});
		return sortIcon;
	}

	private List<String> craftSorterLore(SortConfigurationOptions options) {
		List<String> lore = Lists.newArrayList();
		lore.add("");
		for(Sorter sorter : Sorter.getSortOptions(this.mode).getFramesNonCircular()) {
			if(this.sorter.getCurrent().get() == sorter) {
				lore.add(options.getSelectedColor() + "\u25b6 " + sorter.key.apply(options));
			} else {
				lore.add(options.getNonSelectedColor() + sorter.key.apply(options));
			}
		}
		lore.add("");
		lore.add("&eClick to switch sort filter");
		return lore;
	}

	@AllArgsConstructor
	private static class JustPlayer implements Predicate<SpongeListing> {

		private UUID viewer;

		@Override
		public boolean test(SpongeListing listing) {
			return listing.getLister().equals(this.viewer);
		}

	}

	private static class QuickPurchaseOnly implements Predicate<SpongeListing> {

		@Override
		public boolean test(SpongeListing listing) {
			return listing instanceof BuyItNow;
		}

	}

	private static class AuctionsOnly implements Predicate<SpongeListing> {

		@Override
		public boolean test(SpongeListing listing) {
			return listing instanceof Auction;
		}

	}

	@Getter
	@AllArgsConstructor
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	private enum Sorter {
		QP_MOST_RECENT(SortConfigurationOptions::getQpMostRecent, new Matcher<>(Comparator.comparing(BuyItNow::getPublishTime).reversed())),
		QP_ENDING_SOON(SortConfigurationOptions::getQpEndingSoon, new Matcher<BuyItNow>(Comparator.comparing(Listing::getExpiration))),
		A_HIGHEST_BID(SortConfigurationOptions::getAHighest, new Matcher<>(Comparator.<Auction, Double>comparing(a -> a.getHighBid().getSecond()).reversed())),
		A_LOWEST_BID(SortConfigurationOptions::getALowest, new Matcher<Auction>(Comparator.comparing(a -> a.getHighBid().getSecond()))),
		A_ENDING_SOON(SortConfigurationOptions::getAEndingSoon, new Matcher<Auction>(Comparator.comparing(Listing::getExpiration))),
		A_MOST_BIDS(SortConfigurationOptions::getAMostBids, new Matcher<>(Comparator.<Auction, Integer>comparing(a -> a.getBids().size()).reversed()))
		;

		private final Function<SortConfigurationOptions, String> key;
		private final Matcher<?> comparator;

		private static final CircularLinkedList<Sorter> QUICK_PURCHASE_ONLY = CircularLinkedList.of(QP_MOST_RECENT, QP_ENDING_SOON);
		private static final CircularLinkedList<Sorter> AUCTION_ONLY = CircularLinkedList.of(A_HIGHEST_BID, A_LOWEST_BID, A_ENDING_SOON, A_MOST_BIDS);

		public static CircularLinkedList<Sorter> getSortOptions(boolean mode) {
			return mode ? AUCTION_ONLY : QUICK_PURCHASE_ONLY;
		}
	}

	@Getter
	@AllArgsConstructor
	private static class Matcher<T> {
		private final Comparator<T> comparator;
	}

}
