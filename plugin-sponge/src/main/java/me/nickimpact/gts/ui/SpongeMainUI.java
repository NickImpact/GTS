package me.nickimpact.gts.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongePage;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.listings.manager.ListingManager;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.searching.Searcher;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.sponge.listings.SpongeListing;
import me.nickimpact.gts.common.utils.SpongeItemTypeUtil;
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
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SpongeMainUI {

	private static Map<UUID, Instant> delays = Maps.newHashMap();

	private SpongePage<SpongeListing> page;
	private Player viewer;

	private Collection<Predicate<Listing>> searchConditions = Lists.newArrayList();

	private Class<? extends Entry> classSelection;

	private boolean justPlayer;

	private static final SpongeIcon GRAY_BORDER;
	private static List<EntryClassification> classifications = GTS.getInstance().getAPIService().getEntryRegistry().getClassifications();
	private int index = 0;

	private Task runner;

	/** These settings are for search specific settings */
	private Searcher searcher;
	private String input;

	static {
		GRAY_BORDER = new SpongeIcon(ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).add(Keys.DISPLAY_NAME, Text.EMPTY).add(Keys.DYE_COLOR, DyeColors.GRAY).build());
	}

	public SpongeMainUI(Player viewer, @Nullable Searcher searcher, @Nullable String input) {
		this.viewer = viewer;
		this.searcher = searcher;
		this.input = input;

		this.searchConditions.add(listing -> !listing.hasExpired());
		this.searchConditions.add(listing -> {
			if(this.classSelection == null) return true;
			return listing.getEntry().getClass().isAssignableFrom(classSelection);
		});

		this.page = SpongePage.builder()
				.viewer(viewer)
				.view(this.design())
				.title(GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, GTS.getInstance().getMsgConfig(), MsgConfigKeys.UI_TITLES_MAIN, null, null))
				.contentZone(InventoryDimension.of(9, 4))
				.previousPage(
						Sponge.getPluginManager().isLoaded("pixelmon") ?
								SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:trade_holder_left") :
								ItemTypes.ARROW,
						48
				)
				.nextPage(
						Sponge.getPluginManager().isLoaded("pixelmon") ?
								SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:trade_holder_right") :
								ItemTypes.SPECTRAL_ARROW,
						50
				)
				.build();
		this.page.applier(listing -> {
			SpongeIcon icon = new SpongeIcon(listing.getDisplay(this.viewer));
			icon.addListener(clickable -> {
				UUID uuid = listing.getUuid();
				if(GTS.getInstance().getAPIService().getListingManager().getListingByID(uuid).isPresent()) {
					this.page.close();
					new SpongeConfirmUI(this.viewer, listing, searcher, input).open();
				}
			});

			return icon;
		});
		this.page.define(this.getListings());
		this.page.getView().attachCloseListener(e -> {
			//this.runner.cancel();
		});
	}

	public void open() {
		this.page.open();
		//this.runner = Sponge.getScheduler().createTaskBuilder().execute(this::apply).interval(1, TimeUnit.SECONDS).submit(GTS.getInstance());
	}

	private SpongeLayout design() {
		SpongeLayout.SpongeLayoutBuilder builder = SpongeLayout.builder().dimension(9, 6);
		builder.row(SpongeIcon.BORDER, 4);
		builder.slots(SpongeIcon.BORDER, 47, 51);
		builder.slots(GRAY_BORDER, 46, 52);

		ItemStack refresher = ItemStack.builder().itemType(ItemTypes.CLOCK).add(Keys.DISPLAY_NAME, GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, GTS.getInstance().getMsgConfig(), MsgConfigKeys.UI_ITEMS_REFRESH_TITLE, null, null)).build();
		SpongeIcon rIcon = new SpongeIcon(refresher);
		rIcon.addListener(clickable -> {
			this.apply();
		});

		builder.slot(rIcon, 49);

		ItemStack pListings = ItemStack.builder()
				.itemType(ItemTypes.WRITTEN_BOOK)
				.add(Keys.DISPLAY_NAME, GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, GTS.getInstance().getMsgConfig(), MsgConfigKeys.UI_ITEMS_PLAYER_LISTINGS_TITLE, null, null))
				.add(Keys.ITEM_LORE, Lists.newArrayList(
						GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, GTS.getInstance().getMsgConfig(), MsgConfigKeys.UI_ITEMS_PLAYER_LISTINGS_LORE_DISABLED, null, null))
				)
				.add(Keys.HIDE_ATTRIBUTES, true)
				.add(Keys.HIDE_ENCHANTMENTS, true)
				.build();
		SpongeIcon pIcon = new SpongeIcon(pListings);
		pIcon.addListener(clickable -> {
			this.justPlayer = !this.justPlayer;
			List<Text> lore = pListings.get(Keys.ITEM_LORE).get();
			lore.set(0, GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, GTS.getInstance().getMsgConfig(), this.justPlayer ? MsgConfigKeys.UI_ITEMS_PLAYER_LISTINGS_LORE_ENABLED : MsgConfigKeys.UI_ITEMS_PLAYER_LISTINGS_LORE_DISABLED, null, null));
			pListings.offer(Keys.ITEM_LORE, lore);
			this.page.getView().setSlot(45, pIcon);
			this.apply();
		});
		builder.slot(pIcon, 45);

		if(classifications.size() != 0) {
			EntryClassification first = classifications.get(0);
			builder.slot(this.classificationToIcon(first), 53);
		} else {
			ItemStack empty = ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, GTS.getInstance().getMsgConfig(), MsgConfigKeys.UI_MAIN_NO_ENTRIES_AVAILABLE, null, null)).build();
			builder.slot(new SpongeIcon(empty), 53);
		}

		return builder.build();
	}

	private void apply() {
		this.page.define(this.getListings());
	}

	private List<SpongeListing> getListings() {
		List<SpongeListing> listings = Lists.newArrayList();
		ListingManager<SpongeListing> manager = GTS.getInstance().getAPIService().getListingManager();
		if(justPlayer) {
			listings = manager.getListings().stream()
					.filter(listing -> listing.getOwnerUUID().equals(this.viewer.getUniqueId()))
					.filter(listing -> {
						if(searcher == null) {
							return true;
						}

						return searcher.parse(listing, this.input);
					}).collect(Collectors.toList());
		} else {
			if(!this.searchConditions.isEmpty()) {
				listings = manager.getListings().stream().filter(listing -> {
					boolean passed = true;
					for(Predicate<Listing> predicate : this.searchConditions) {
						passed = passed && predicate.test(listing);
					}

					return passed;
				}).filter(listing -> {
					if(searcher == null) {
						return true;
					}

					return searcher.parse(listing, this.input);
				}).collect(Collectors.toList());
			}
		}

		return listings;
	}

	private SpongeIcon classificationToIcon(EntryClassification classification) {
		String identifier = classification.getPrimaryIdentifier();
		ItemStack rep = ItemStack.builder().itemType(Sponge.getRegistry().getType(ItemType.class, classification.getItemRep()).orElse(ItemTypes.BARRIER)).build();

		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("gts_entry_classification", src -> Optional.of(Text.of(identifier)));
		rep.offer(Keys.DISPLAY_NAME, GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.FILTER_TITLE, tokens, null));

		List<Text> lore = Lists.newArrayList(GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, classification.getClassification().equals(this.classSelection) ? MsgConfigKeys.FILTER_STATUS_ENABLED : MsgConfigKeys.FILTER_STATUS_DISABLED, null, null));
		lore.addAll(GTS.getInstance().getTextParsingUtils().fetchAndParseMsgs(this.viewer, MsgConfigKeys.FILTER_NOTES, null, null));
		rep.offer(Keys.ITEM_LORE, lore);

		SpongeIcon icon = new SpongeIcon(rep);
		icon.addListener(clickable -> {
			if(clickable.getEvent() instanceof ClickInventoryEvent.Secondary) {
				if(index + 1 >= classifications.size()) {
					index = -1;
				}

				this.page.getView().setSlot(53, this.classificationToIcon(classifications.get(++index)));
			} else {
				if (delays.containsKey(clickable.getPlayer().getUniqueId())) {
					if (!Instant.now().isAfter(delays.get(clickable.getPlayer().getUniqueId()))) {
						return;
					}
				}

				List<Text> l = rep.get(Keys.ITEM_LORE).get();
				if(classification.getClassification().equals(this.classSelection)) {
					this.classSelection = null;
					l.set(0, GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.FILTER_STATUS_DISABLED, null, null));
				} else {
					this.classSelection = classification.getClassification();
					l.set(0, GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.FILTER_STATUS_ENABLED, null, null));
				}
				rep.offer(Keys.ITEM_LORE, l);

				this.page.getView().setSlot(53, icon);
				this.apply();
				delays.put(clickable.getPlayer().getUniqueId(), Instant.now().plusSeconds(3));
			}
		});

		return icon;
	}
}
