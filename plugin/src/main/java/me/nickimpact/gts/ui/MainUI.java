package me.nickimpact.gts.ui;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.gui.v2.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.configuration.ConfigKeys;
import me.nickimpact.gts.configuration.MsgConfigKeys;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.internal.TextParsingUtils;
import me.nickimpact.gts.utils.ItemUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
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
import java.util.function.Function;
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

	private Class<? extends Entry> classSelections;

	/** Whether or not we should show the player's listings or not */
	private boolean justPlayer = false;

	private static final Icon BORDER = Icon.from(ItemStack.builder().from(Icon.BORDER.getDisplay()).add(Keys.DISPLAY_NAME, Text.EMPTY).build());

	private static List<EntryClassification> classifications = GTS.getInstance().getService().getEntryRegistry().getClassifications();
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
			if(this.classSelections == null) return true;
			return listing.getEntry().getClass().isAssignableFrom(classSelections);
		});

		this.page = QueriedPage.builder()
				.title(TextParsingUtils.fetchAndParseMsg(this.player, MsgConfigKeys.UI_TITLES_MAIN, null, null))
				.view(this.design())
				.viewer(player)
				.previousPage(Sponge.getRegistry().getType(ItemType.class, "pixelmon:trade_holder_left").orElse(ItemTypes.BARRIER), 48)
				.nextPage(Sponge.getRegistry().getType(ItemType.class, "pixelmon:trade_holder_right").orElse(ItemTypes.BARRIER), 50)
				.contentZone(InventoryDimension.of(9, 4))
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
	}

	public void open() {
		this.page.open();
	}

	private Layout design() {
		Layout.Builder lb = Layout.builder().dimension(9, 6);
//		lb.row(BORDER, 0).row(BORDER, 4);
//		lb.column(BORDER, 0).column(BORDER, 8);
		lb.row(Icon.BORDER, 4);
		lb.slots(BORDER, 47, 51);
		lb.slots(Icon.from(ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).add(Keys.DISPLAY_NAME, Text.EMPTY).add(Keys.DYE_COLOR, DyeColors.GRAY).build()), 46, 52);

		ItemStack refresher = ItemStack.builder()
				.itemType(ItemTypes.CLOCK)
				.add(Keys.DISPLAY_NAME, TextParsingUtils.fetchAndParseMsg(player, MsgConfigKeys.REFRESH_ICON, null, null))
				.build();
		Icon rIcon = Icon.from(refresher);
		rIcon.addListener(clickable -> this.apply());
		lb.slot(rIcon, 49);

		Text pLTitle = TextParsingUtils.fetchAndParseMsg(this.player, MsgConfigKeys.UI_ITEMS_PLAYER_LISTINGS_TITLE, null, null);

		List<Text> pLLore = Lists.newArrayList(TextParsingUtils.fetchAndParseMsg(this.player, this.justPlayer ? MsgConfigKeys.FILTER_STATUS_ENABLED : MsgConfigKeys.FILTER_STATUS_DISABLED, null, null));
		ImmutableList<Text> additional = ImmutableList.copyOf(TextParsingUtils.fetchAndParseMsgs(this.player, MsgConfigKeys.UI_ITEMS_PLAYER_LISTINGS_LORE, null, null));
		pLLore.addAll(additional);
		ItemStack pListings = ItemStack.builder().itemType(ItemTypes.WRITTEN_BOOK).add(Keys.DISPLAY_NAME, pLTitle).add(Keys.ITEM_LORE, pLLore).add(Keys.HIDE_MISCELLANEOUS, true).build();
		Icon pl = new Icon(pListings);
		pl.addListener(clickable -> {
			this.justPlayer = !this.justPlayer;
			List<Text> lore = Lists.newArrayList(TextParsingUtils.fetchAndParseMsg(this.player, this.justPlayer ? MsgConfigKeys.FILTER_STATUS_ENABLED : MsgConfigKeys.FILTER_STATUS_DISABLED, null, null));
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
		if(classifications.size() != 0) {
			EntryClassification first = classifications.get(0);
			lb.slot(this.classificationToIcon(first), 53);
		} else {
			lb.slot(Icon.from(ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, TextParsingUtils.fetchAndParseMsg(this.player, MsgConfigKeys.UI_MAIN_NO_ENTRIES_AVAILABLE, null, null)).build()), 53);
		}

		return lb.build();
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

	private Icon classificationToIcon(EntryClassification classification) {
		String identifier = classification.getPrimaryIdentifier();
		ItemStack rep = ItemStack.builder().itemType(Sponge.getRegistry().getType(ItemType.class, classification.getItemRep()).orElse(ItemTypes.BARRIER)).build();

		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("gts_entry_classification", src -> Optional.of(Text.of(identifier)));
		rep.offer(Keys.DISPLAY_NAME, TextParsingUtils.fetchAndParseMsg(this.player, MsgConfigKeys.FILTER_TITLE, tokens, null));

		List<Text> lore = Lists.newArrayList(TextParsingUtils.fetchAndParseMsg(this.player, classification.getClassification().equals(this.classSelections) ? MsgConfigKeys.FILTER_STATUS_ENABLED : MsgConfigKeys.FILTER_STATUS_DISABLED, null, null));
		lore.addAll(TextParsingUtils.fetchAndParseMsgs(this.player, MsgConfigKeys.FILTER_NOTES, null, null));
		rep.offer(Keys.ITEM_LORE, lore);

		Icon icon = Icon.from(rep);
		icon.addListener(clickable -> {
			if(clickable.getEvent() instanceof ClickInventoryEvent.Secondary) {
				if(index + 1 >= classifications.size()) {
					index = -1;
				}

				this.updateOption(this.classificationToIcon(classifications.get(++index)));
			} else {
				if (delays.containsKey(clickable.getPlayer().getUniqueId())) {
					if (!Instant.now().isAfter(delays.get(clickable.getPlayer().getUniqueId()))) {
						return;
					}
				}

				List<Text> l = rep.get(Keys.ITEM_LORE).get();
				if(classification.getClassification().equals(this.classSelections)) {
					this.classSelections = null;
					l.set(0, TextParsingUtils.fetchAndParseMsg(this.player, MsgConfigKeys.FILTER_STATUS_DISABLED, null, null));
				} else {
					this.classSelections = classification.getClassification();
					l.set(0, TextParsingUtils.fetchAndParseMsg(this.player, MsgConfigKeys.FILTER_STATUS_ENABLED, null, null));
				}
				rep.offer(Keys.ITEM_LORE, l);

				this.updateOption(icon);
				this.apply();
				delays.put(clickable.getPlayer().getUniqueId(), Instant.now().plusSeconds(3));
			}
		});

		return icon;
	}

	private void updateOption(Icon icon) {
		this.page.getView().setSlot(53, icon);
	}
}
