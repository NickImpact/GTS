package net.impactdev.gts.ui.submenu;

import com.google.common.collect.Lists;

import io.leangen.geantyref.TypeToken;
import net.impactdev.gts.api.listings.entries.EntryManager;
import net.impactdev.gts.SpongeGTSPlugin;
import net.impactdev.gts.api.ui.GTSMenu;
import net.impactdev.gts.sponge.utils.items.ProvidedIcons;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.api.searching.Searcher;
import net.impactdev.gts.ui.admin.editor.SpongeListingEditorMenu;
import net.impactdev.gts.ui.submenu.browser.SpongeSelectedListingMenu;
import net.impactdev.gts.sponge.utils.SpongeMenuOpener;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.scheduler.SchedulerTask;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.ui.containers.icons.DisplayProvider;
import net.impactdev.impactor.api.ui.containers.icons.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.Layout;
import net.impactdev.impactor.api.ui.containers.pagination.sectioned.SectionedPagination;
import net.impactdev.impactor.api.ui.containers.pagination.sectioned.sections.Section;
import net.impactdev.impactor.api.ui.containers.pagination.updaters.PageUpdater;
import net.impactdev.impactor.api.ui.containers.pagination.updaters.PageUpdaterType;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.config.wrappers.SortConfigurationOptions;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.lists.CircularLinkedList;
import net.impactdev.gts.common.config.wrappers.TitleLorePair;
import net.impactdev.gts.sponge.listings.SpongeListing;
import net.impactdev.gts.ui.SpongeMainMenu;
import net.impactdev.gts.sponge.utils.Utilities;

import net.impactdev.impactor.api.utilities.ComponentManipulator;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.util.TriState;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.menu.ClickType;
import org.spongepowered.api.item.inventory.menu.ClickTypes;
import org.spongepowered.api.scheduler.Task;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SpongeListingMenu implements GTSMenu, SpongeMenuOpener {

	private static final QuickPurchaseOnly QUICK_PURCHASE_ONLY = new QuickPurchaseOnly();
	private static final AuctionsOnly AUCTIONS_ONLY = new AuctionsOnly();

	private static final MessageService PARSER = Utilities.PARSER;

	private final SectionedPagination pagination;
	private SchedulerTask runner;

	private @Nullable Searching searchQuery;

	/** True = Auction, False = Quick Purchase, Not Set = All */
	private TriState mode = TriState.NOT_SET;
	private CircularLinkedList<Sorter> sorter;

	/** If the menu was opened in editor mode */
	private final boolean editor;

	@SafeVarargs
	public SpongeListingMenu(ServerPlayer viewer, boolean editor, Predicate<Listing>... conditions) {
		Predicate<Listing> filter = listing -> true;
		for(Predicate<Listing> condition : conditions) {
			filter = filter.and(condition);
		}

		this.sorter = Sorter.QUICK_PURCHASE_ONLY.copy();
		this.pagination = SectionedPagination.builder()
				.provider(Key.key("gts", "listings"))
				.viewer(PlatformPlayer.from(viewer))
				.title(PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_MENU_LISTINGS_TITLE)))
				.readonly(true)
				.layout(this.design(viewer))
				.section()
				.asynchronous(Listing.class)
				.accumulator(this.fetchAndTranslate(viewer))
				.filter(filter)
				.dimensions(7, 4)
				.offset(2, 0)
				.updater(PageUpdater.builder()
						.slot(47)
						.type(PageUpdaterType.PREVIOUS)
						.provider(target -> ItemStack.builder()
								.itemType(ItemTypes.SPECTRAL_ARROW)
								.add(Keys.CUSTOM_NAME, MiniMessage.miniMessage().deserialize("<gradient:red:gold>Previous Page (" + target + ")</gradient>"))
								.build()
						)
						.build()
				)
				.updater(PageUpdater.builder()
						.slot(48)
						.type(PageUpdaterType.NEXT)
						.provider(target -> ItemStack.builder()
								.itemType(ItemTypes.SPECTRAL_ARROW)
								.add(Keys.CUSTOM_NAME, MiniMessage.miniMessage().deserialize("<gradient:green:blue>Next Page (" + target + ")</gradient>"))
								.build()
						)
						.build()
				)
				.style(TriState.TRUE)
				.waiting(this.loading())
				.timeout(5, TimeUnit.SECONDS, this.timeout())
				.complete()
				.section()
				.synchronous(new TypeToken<EntryManager<?, ?>>() {})
				.contents(Lists.newArrayList()) // TODO - Entry Type Filtering
				.dimensions(1, 3)
				.offset(0, 1)
				.style(TriState.FALSE)
				.updater(PageUpdater.builder()
						.slot(0)
						.type(PageUpdaterType.PREVIOUS)
						.provider(target -> ItemStack.builder()
								.itemType(ItemTypes.SPECTRAL_ARROW)
								.add(Keys.CUSTOM_NAME, MiniMessage.miniMessage().deserialize("<gradient:red:gold>Previous Page (" + target + ")</gradient>"))
								.build()
						)
						.build()
				)
				.updater(PageUpdater.builder()
						.slot(36)
						.type(PageUpdaterType.NEXT)
						.provider(target -> ItemStack.builder()
								.itemType(ItemTypes.SPECTRAL_ARROW)
								.add(Keys.CUSTOM_NAME, MiniMessage.miniMessage().deserialize("<gradient:green:blue>Next Page (" + target + ")</gradient>"))
								.build()
						)
						.build()
				)
				.complete()
				.onClose(context -> {
					this.runner.cancel();
					return true;
				})
				.build();

		this.editor = editor;
	}

	public void open() {
		this.runner = Sponge.server().scheduler().submit(this.schedule())::cancel;
		this.open(this.pagination::open);
	}

	protected Layout design(ServerPlayer viewer) {
		Layout.LayoutBuilder builder = Layout.builder();
		builder.row(ProvidedIcons.BORDER, 5)
				.column(ProvidedIcons.BORDER, 2)
				.slots(ProvidedIcons.BORDER, 0, 36, 49);

		this.createBottomPanel(viewer, builder);
		return builder.build();
	}

	private Icon<ItemStack> loading() {
		return Icon.builder(ItemStack.class)
				.display(new DisplayProvider.Constant<>(ItemStack.builder()
					.itemType(ItemTypes.YELLOW_STAINED_GLASS_PANE)
					.add(Keys.CUSTOM_NAME, ComponentManipulator.noItalics(PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_MENU_LISTINGS_SPECIAL_LOADING))))
					.build()
				))
				.build();
	}

	private Icon<ItemStack> timeout() {
		TitleLorePair pair = Utilities.readMessageConfigOption(MsgConfigKeys.UI_MENU_LISTINGS_SPECIAL_TIMED_OUT);
		return Icon.builder(ItemStack.class)
				.display(new DisplayProvider.Constant<>(ItemStack.builder()
						.itemType(ItemTypes.RED_STAINED_GLASS_PANE)
						.add(Keys.CUSTOM_NAME, ComponentManipulator.noItalics(PARSER.parse(pair.getTitle())))
						.add(Keys.LORE,
								PARSER.parse(pair.getLore()).stream()
										.map(ComponentManipulator::noItalics)
										.collect(Collectors.toList())
						)
						.build()
				))
				.build();
	}

	private CompletableFuture<List<Icon.Binding<?, Listing>>> fetchAndTranslate(ServerPlayer viewer) {
		CompletableFuture<List<Listing>> future = Impactor.getInstance().getRegistry()
				.get(ListingManager.class)
				.fetchListings();

		return future.thenApply(list -> list.stream()
					.filter(listing -> {
						boolean expired = listing.hasExpired();
						if(!expired) {
							if (listing instanceof BuyItNow) {
								return !((BuyItNow) listing).isPurchased();
							}
						}

						return !expired;
					})
					.sorted(this.sorter.getCurrent().get().comparator)
					.collect(Collectors.toList())
			)
			.thenApply(list -> list.stream().map(listing -> this.translate(listing, viewer)).collect(Collectors.toList()));
	}

	private Icon.Binding<ItemStack, Listing> translate(Listing listing, ServerPlayer viewer) {
		return Icon.builder(ItemStack.class)
				.display(() -> {
					final Config lang = GTSPlugin.instance().configuration().language();
					final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
					SpongeListing sponge = (SpongeListing) listing;

					Display<ItemStack> display = sponge.getEntry().getDisplay(viewer.uniqueId());
					ItemStack item = ItemStack.builder().from(display.get()).build();

					Optional<List<Component>> lore = item.get(Keys.LORE);
					lore.ifPresent(texts -> texts.addAll(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_LISTING_DETAIL_SEPARATOR))));
					Supplier<List<Component>> append = () -> {
						List<Component> result = Lists.newArrayList();
						if(listing instanceof Auction) {
							Auction auction = (Auction) listing;
							List<String> input;
							if(auction.getBids().size() > 1) {
								input = lang.get(MsgConfigKeys.UI_AUCTION_DETAILS_WITH_BIDS);
							} else if(auction.getBids().size() == 1) {
								input = lang.get(MsgConfigKeys.UI_AUCTION_DETAILS_WITH_SINGLE_BID);
							} else {
								input = lang.get(MsgConfigKeys.UI_AUCTION_DETAILS_NO_BIDS);
							}

							PlaceholderSources sources = PlaceholderSources.builder()
									.append(Auction.class, () -> auction)
									.build();
							result.addAll(service.parse(input, sources));
						} else if(listing instanceof BuyItNow) {
							BuyItNow bin = (BuyItNow) listing;
							List<String> input = lang.get(MsgConfigKeys.UI_BIN_DETAILS);

							PlaceholderSources sources = PlaceholderSources.builder()
									.append(BuyItNow.class, () -> bin)
									.build();
							result.addAll(service.parse(input, sources));
						}
						return result;
					};
					List<Component> result = lore.orElse(Lists.newArrayList());
					result.addAll(append.get());
					item.offer(Keys.LORE, result);

					return item;
				})
				.listener(context -> {
					this.pagination.close();
					if(!this.editor) {
						new SpongeSelectedListingMenu(viewer, listing, () -> new SpongeListingMenu(viewer, false), false, true).open();
					} else {
						new SpongeListingEditorMenu(viewer, listing, () -> new SpongeListingMenu(viewer, false)).open();
					}

					return false;
				})
				.build(() -> listing);
	}

	private void createBottomPanel(ServerPlayer viewer, Layout.LayoutBuilder layout) {
		Config lang = GTSPlugin.instance().configuration().language();
		Style style = Style.style().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).build();

		ItemStack quick = ItemStack.builder()
				.itemType(ItemTypes.EMERALD)
				.add(Keys.CUSTOM_NAME, Component.text("Quick Purchase").style(style).color(NamedTextColor.GREEN))
				.add(Keys.LORE, Lists.newArrayList(
						Component.text("You are currently viewing").style(style).color(NamedTextColor.GRAY),
						Component.text("Quick Purchase").style(style).color(NamedTextColor.AQUA)
										.append(Component.text(" listings only").color(NamedTextColor.GRAY)),
						Component.empty(),
						Component.text("Click to adjust your filter settings:").color(NamedTextColor.YELLOW),
						Component.text("Left Click: All").color(NamedTextColor.YELLOW),
						Component.text("Right Click: Auctions").color(NamedTextColor.YELLOW)
				))
				.build();
		Icon<ItemStack> bin = Icon.builder(ItemStack.class)
				.display(new DisplayProvider.Constant<>(quick))
				.build();

		ItemStack auctions = ItemStack.builder()
				.itemType(ItemTypes.GOLD_INGOT)
				.add(Keys.CUSTOM_NAME, Component.text("Auctions").style(style).color(NamedTextColor.GREEN))
				.add(Keys.LORE, Lists.newArrayList(
						Component.text("You are currently viewing").style(style).color(NamedTextColor.GRAY),
						Component.text("Auction").style(style).color(NamedTextColor.AQUA)
								.append(Component.text(" listings only").color(NamedTextColor.GRAY)),
						Component.empty(),
						Component.text("Click to adjust your filter settings:").color(NamedTextColor.YELLOW),
						Component.text("Left Click: BIN").color(NamedTextColor.YELLOW),
						Component.text("Right Click: All").color(NamedTextColor.YELLOW)
				))
				.build();

		Icon<ItemStack> auction = Icon.builder(ItemStack.class)
				.display(new DisplayProvider.Constant<>(auctions))
				.build();

		ItemStack noFilter = ItemStack.builder()
				.itemType(ItemTypes.ENDER_CHEST)
				.add(Keys.CUSTOM_NAME, PARSER.parse("&aNon-Filtered View"))
				.add(Keys.LORE, PARSER.parse(Lists.newArrayList(
						"&7You are currently viewing",
						"&7all listings. If you wish to",
						"&7filter your view, &eclick &7via",
						"&7the following guide:",
						"",
						"&eLeft Click: BIN",
						"&eRight Click: Auctions"
				)))
				.build();

		Icon<ItemStack> all = Icon.builder(ItemStack.class)
				.display(new DisplayProvider.Constant<>(noFilter))
				.listener(context -> {
					Section.Generic<Listing> section = this.pagination.at(4)
							.map(s -> (Section.Generic<Listing>) s)
							.orElseThrow(IllegalStateException::new);

					ClickType<?> type = context.require(ClickType.class);
					if(type.equals(ClickTypes.CLICK_LEFT.get())) {
						this.mode = TriState.TRUE;
						this.pagination.set(bin, 52);

						section.filter(QUICK_PURCHASE_ONLY);
					} else if(type.equals(ClickTypes.CLICK_RIGHT.get())) {
						this.mode = TriState.FALSE;
						this.pagination.set(auction, 52);
					}

					return false;
				})
				.build();

		layout.slot(all, 52);
		bin.listener(context -> {
			Section.Generic<Listing> section = this.pagination.at(4)
					.map(s -> (Section.Generic<Listing>) s)
					.orElseThrow(IllegalStateException::new);

			ClickType<?> type = context.require(ClickType.class);
			if(type.equals(ClickTypes.CLICK_LEFT.get())) {
				this.mode = TriState.NOT_SET;
				this.pagination.set(all, 52);

				section.filter(null);
			} else if(type.equals(ClickTypes.CLICK_RIGHT.get())) {
				this.mode = TriState.FALSE;
				this.pagination.set(auction, 52);

				section.filter(AUCTIONS_ONLY);
			}

			return false;
		});
		auction.listener(context -> {
			Section.Generic<Listing> section = this.pagination.at(4)
					.map(s -> (Section.Generic<Listing>) s)
					.orElseThrow(IllegalStateException::new);

			ClickType<?> type = context.require(ClickType.class);
			if(type.equals(ClickTypes.CLICK_LEFT.get())) {
				this.mode = TriState.TRUE;
				this.pagination.set(bin, 52);

				section.filter(QUICK_PURCHASE_ONLY);
			} else if(type.equals(ClickTypes.CLICK_RIGHT.get())) {
				this.mode = TriState.NOT_SET;
				this.pagination.set(all, 52);

				section.filter(null);
			}

			return false;
		});

//		ItemStack searcher = ItemStack.builder()
//				.itemType(ItemTypes.SIGN)
//				.add(Keys.DISPLAY_NAME, PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_MENU_SEARCH_TITLE), Lists.newArrayList(this::getViewer)))
//				.add(Keys.ITEM_LORE, PARSER.parse(
//						this.searchQuery != null ? Utilities.readMessageConfigOption(MsgConfigKeys.UI_MENU_SEARCH_LORE_QUERIED) :
//								Utilities.readMessageConfigOption(MsgConfigKeys.UI_MENU_SEARCH_LORE_NO_QUERY),
//						Lists.newArrayList(this::getViewer, () -> this.searchQuery)
//				))
//				.build();
//		SpongeIcon sIcon = new SpongeIcon(searcher);
//		sIcon.addListener(clickable -> {
//			SignQuery<Text, Player> query = SignQuery.<Text, Player>builder()
//					.position(new Vector3d(0, 1, 0))
//					.text(Lists.newArrayList(
//							Text.EMPTY,
//							Text.of("----------------"),
//							Text.of("Enter your search"),
//							Text.of("query above")
//					))
//					.response(submission -> {
//						final String asking = submission.get(0);
//						Impactor.getInstance().getScheduler().executeSync(() -> {
//							if (!asking.isEmpty()) {
//								this.conditions.removeIf(p -> p instanceof Searching);
//								this.conditions.add(new Searching(asking));
//							} else {
//								this.conditions.removeIf(p -> p instanceof Searching);
//							}
//							new SpongeListingMenu(this.getViewer(), this.editor, this.conditions.toArray(new Predicate[]{})).open();
//						});
//
//						return true;
//					})
//					.build();
//
//			this.getView().close(this.getViewer());
//			query.sendTo(this.getViewer());
//		});
//		layout.slot(sIcon, 49);

		Icon<ItemStack> sorter = this.drawSorter();
		layout.slot(sorter, 51);

		ItemStack b = ItemStack.builder()
				.itemType(ItemTypes.BARRIER)
				.add(Keys.CUSTOM_NAME, PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK)))
				.build();

		Icon<ItemStack> back = Icon.builder(ItemStack.class)
				.display(new DisplayProvider.Constant<>(b))
				.listener(context -> {
					this.runner.cancel();
					// We need to cancel a running query for listings if it's still running

					SpongeMainMenu menu = new SpongeMainMenu(viewer);
					menu.open();
					return false;
				})
				.build();
		layout.slot(back, 45);
	}

	private Icon<ItemStack> drawSorter() {
		SortConfigurationOptions options = GTSPlugin.instance().configuration().language().get(MsgConfigKeys.UI_MENU_LISTINGS_SORT);

		this.sorter.next();
		Icon<ItemStack> icon = Icon.builder(ItemStack.class)
				.display(() -> ItemStack.builder()
						.itemType(ItemTypes.HOPPER)
						.add(Keys.DISPLAY_NAME, PARSER.parse(options.getTitle()))
						.add(Keys.LORE, PARSER.parse(this.craftSorterLore(options)))
						.build())
				.build();
		icon.listener(context -> {
			this.sorter.next();
			this.pagination.set(icon, 51);

			return false;
		});

		return icon;
	}

	private List<String> craftSorterLore(SortConfigurationOptions options) {
		List<String> lore = Lists.newArrayList();
		lore.add("");
		for(Sorter sorter : Sorter.sorters(this.mode).getFramesNonCircular()) {
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

	private static class JustPlayer implements Predicate<SpongeListing> {

		private final UUID viewer;

		public JustPlayer(UUID viewer) {
			this.viewer = viewer;
		}

		@Override
		public boolean test(SpongeListing listing) {
			return listing.getLister().equals(this.viewer);
		}

	}

	private static class QuickPurchaseOnly implements Predicate<Listing> {

		@Override
		public boolean test(Listing listing) {
			return listing instanceof BuyItNow;
		}

	}

	private static class AuctionsOnly implements Predicate<Listing> {

		@Override
		public boolean test(Listing listing) {
			return listing instanceof Auction;
		}

	}

	public static class Searching implements Predicate<Listing> {

		private final String query;

		public Searching(String query) {
			this.query = query;
		}

		@Override
		public boolean test(Listing listing) {
			for(Searcher s : GTSService.getInstance().getSearchers()) {
				if(s.parse(listing, this.query)) {
					return true;
				}
			}

			return false;
		}

		public String getQuery() {
			return this.query;
		}
	}

	private enum Sorter {
		QP_MOST_RECENT(SortConfigurationOptions::getQpMostRecent, Comparator.comparing(Listing::getPublishTime).reversed()),
		QP_ENDING_SOON(SortConfigurationOptions::getQpEndingSoon, Comparator.comparing(Listing::getExpiration)),
		A_HIGHEST_BID(SortConfigurationOptions::getAHighest, Comparator.<Listing, Double>comparing(listing -> ((Auction) listing).getCurrentPrice()).reversed()),
		A_LOWEST_BID(SortConfigurationOptions::getALowest, Comparator.comparing(listing -> ((Auction) listing).getCurrentPrice())),
		A_ENDING_SOON(SortConfigurationOptions::getAEndingSoon, Comparator.comparing(Listing::getExpiration)),
		A_MOST_BIDS(SortConfigurationOptions::getAMostBids, Comparator.<Listing, Integer>comparing(listing -> ((Auction) listing).getBids().size()).reversed())
		;

		private final Function<SortConfigurationOptions, String> key;
		private final Comparator<Listing> comparator;

		Sorter(Function<SortConfigurationOptions, String> key, Comparator<Listing> comparator) {
			this.key = key;
			this.comparator = comparator;
		}

		public Function<SortConfigurationOptions, String> key() {
			return this.key;
		}

		public Comparator<Listing> comparator() {
			return this.comparator;
		}

		private static final CircularLinkedList<Sorter> QUICK_PURCHASE_ONLY = CircularLinkedList.of(QP_ENDING_SOON, QP_MOST_RECENT);
		private static final CircularLinkedList<Sorter> AUCTION_ONLY = CircularLinkedList.of(A_HIGHEST_BID, A_LOWEST_BID, A_ENDING_SOON, A_MOST_BIDS);

		public static CircularLinkedList<Sorter> sorters(TriState mode) {
			return mode.toBooleanOrElse(false) ? AUCTION_ONLY : QUICK_PURCHASE_ONLY;
		}
	}

	private Task schedule() {
		return Task.builder()
				.execute(() -> {
					if (Sponge.server().ticksPerSecond() >= 18) {
						this.pagination.at(3).ifPresent(section -> section.pages().nextOrThrow().refresh());
					}
				})
				.interval(1, TimeUnit.SECONDS)
				.plugin(GTSPlugin.instance().as(SpongeGTSPlugin.class).container())
				.build();
	}
}
