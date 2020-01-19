package me.nickimpact.gts.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.gui.InventoryDimensions;
import com.nickimpact.impactor.spigot.ui.SpigotIcon;
import com.nickimpact.impactor.spigot.ui.SpigotLayout;
import com.nickimpact.impactor.spigot.ui.SpigotPage;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.ListingManager;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.searching.Searcher;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.spigot.SpigotListing;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SpigotMainUI {

	private static Map<UUID, Instant> delays = Maps.newHashMap();

	private SpigotPage<SpigotListing> page;
	private Player viewer;

	private Collection<Predicate<Listing>> searchConditions = Lists.newArrayList();

	private Class<? extends Entry> classSelection;

	private boolean justPlayer;

	private static final SpigotIcon GRAY_BORDER;
	private static List<EntryClassification> classifications = GTS.getInstance().getAPIService().getEntryRegistry().getClassifications();
	private int index = 0;

	private int runner;

	/** These settings are for search specific settings */
	private Searcher searcher;
	private String input;

	static {
		GRAY_BORDER = new SpigotIcon(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)15));
		ItemMeta meta = GRAY_BORDER.getDisplay().getItemMeta();
		meta.setDisplayName(" ");
		GRAY_BORDER.getDisplay().setItemMeta(meta);
	}

	public SpigotMainUI(Player viewer, @Nullable Searcher searcher, @Nullable String input) {
		this.viewer = viewer;
		this.searcher = searcher;
		this.input = input;

		this.searchConditions.add(listing -> !listing.hasExpired());
		this.searchConditions.add(listing -> {
			if(this.classSelection == null) return true;
			return listing.getEntry().getClass().isAssignableFrom(classSelection);
		});

		this.page = SpigotPage.builder()
				.viewer(viewer)
				.view(this.design())
				.title(GTS.getInstance().getTokenService().process(MsgConfigKeys.UI_TITLES_MAIN, viewer, null, null))
				.contentZone(new InventoryDimensions(9, 4))
				.previousPage(Material.matchMaterial("pixelmon_trade_holder_left"), 48)
				.nextPage(Material.matchMaterial("pixelmon_trade_holder_right"), 50)
				.build();
		this.page.applier(listing -> {
			SpigotIcon icon = new SpigotIcon(listing.getDisplay(this.viewer));
			icon.addListener(clickable -> {
				UUID uuid = listing.getUuid();
				if(GTS.getInstance().getAPIService().getListingManager().getListingByID(uuid).isPresent()) {
					this.page.close();
					new SpigotConfirmUI(this.viewer, listing, this.searcher, this.input).open();
				}
			});

			return icon;
		});
		this.page.define(this.getListings());
		this.page.getView().attachCloseListener(e -> {
			Bukkit.getScheduler().cancelTask(this.runner);
		});
	}

	public void open() {
		this.page.open();
		this.runner = Bukkit.getScheduler().runTaskTimer(GTS.getInstance(), this::apply, 0, 20).getTaskId();
	}

	private SpigotLayout design() {
		SpigotLayout.SpigotLayoutBuilder builder = SpigotLayout.builder().dimension(9, 6);
		builder.row(SpigotIcon.BORDER, 4);
		builder.slots(SpigotIcon.BORDER, 47, 51);
		builder.slots(GRAY_BORDER, 46, 52);

		ItemStack refresher = new ItemStack(Material.WATCH);
		ItemMeta rMeta = refresher.getItemMeta();
		rMeta.setDisplayName(GTS.getInstance().getTokenService().process(MsgConfigKeys.UI_ITEMS_REFRESH_TITLE, viewer, null, null));
		refresher.setItemMeta(rMeta);
		SpigotIcon rIcon = new SpigotIcon(refresher);
		rIcon.addListener(clickable -> {
			this.apply();
		});

		builder.slot(rIcon, 49);

		ItemStack pListings = new ItemStack(Material.WRITTEN_BOOK);
		ItemMeta pMeta = pListings.getItemMeta();
		pMeta.setDisplayName(GTS.getInstance().getTokenService().process(MsgConfigKeys.UI_ITEMS_PLAYER_LISTINGS_TITLE, viewer, null, null));
		pMeta.setLore(Lists.newArrayList(GTS.getInstance().getTokenService().process(MsgConfigKeys.UI_ITEMS_PLAYER_LISTINGS_LORE_DISABLED, viewer, null, null)));
		pMeta.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES);
		pMeta.hasItemFlag(ItemFlag.HIDE_ENCHANTS);
		pListings.setItemMeta(pMeta);
		SpigotIcon pIcon = new SpigotIcon(pListings);
		pIcon.addListener(clickable -> {
			this.justPlayer = !this.justPlayer;
			pMeta.setLore(Lists.newArrayList(GTS.getInstance().getTokenService().process(this.justPlayer ? MsgConfigKeys.UI_ITEMS_PLAYER_LISTINGS_LORE_ENABLED : MsgConfigKeys.UI_ITEMS_PLAYER_LISTINGS_LORE_DISABLED, viewer, null, null)));
			pListings.setItemMeta(pMeta);
			this.page.getView().setSlot(45, pIcon);
			this.apply();
		});
		builder.slot(pIcon, 45);

		if(classifications.size() != 0) {
			EntryClassification first = classifications.get(0);
			builder.slot(this.classificationToIcon(first), 53);
		} else {
			ItemStack empty = new ItemStack(Material.BARRIER);
			ItemMeta eMeta = empty.getItemMeta();
			eMeta.setDisplayName(GTS.getInstance().getTokenService().process(MsgConfigKeys.UI_MAIN_NO_ENTRIES_AVAILABLE, viewer, null, null));
			empty.setItemMeta(eMeta);
			builder.slot(new SpigotIcon(empty), 53);
		}

		return builder.build();
	}

	private void apply() {
		this.page.define(this.getListings());
	}

	private List<SpigotListing> getListings() {
		List<SpigotListing> listings = Lists.newArrayList();
		ListingManager<SpigotListing> manager = GTS.getInstance().getAPIService().getListingManager();
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

	private SpigotIcon classificationToIcon(EntryClassification classification) {
		ItemStack rep = new ItemStack(Material.matchMaterial(classification.getItemRep()));
		ItemMeta repMeta = rep.getItemMeta();

		Map<String, Function<CommandSender, Optional<String>>> tokens = Maps.newHashMap();
		tokens.put("gts_classifier", src -> Optional.of(classification.getPrimaryIdentifier()));
		repMeta.setDisplayName(GTS.getInstance().getTokenService().process(MsgConfigKeys.FILTER_TITLE, viewer, tokens, null));

		List<String> lore = Lists.newArrayList(GTS.getInstance().getTokenService().process(classification.getClassification().equals(this.classSelection) ? MsgConfigKeys.FILTER_STATUS_ENABLED : MsgConfigKeys.FILTER_STATUS_DISABLED, this.viewer, null, null));
		lore.addAll(GTS.getInstance().getTokenService().process(MsgConfigKeys.FILTER_NOTES, this.viewer, null, null));
		repMeta.setLore(lore);
		rep.setItemMeta(repMeta);

		SpigotIcon icon = new SpigotIcon(rep);
		icon.addListener(clickable -> {
			if(clickable.getEvent().isRightClick()) {
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

				List<String> l = repMeta.getLore();
				if(classification.getClassification().equals(this.classSelection)) {
					this.classSelection = null;
					lore.set(0, GTS.getInstance().getTokenService().process(MsgConfigKeys.FILTER_STATUS_DISABLED, viewer, null, null));
					repMeta.setLore(l);
					rep.setItemMeta(repMeta);
				} else {
					this.classSelection = classification.getClassification();
					lore.set(0, GTS.getInstance().getTokenService().process(MsgConfigKeys.FILTER_STATUS_ENABLED, viewer, null, null));
					repMeta.setLore(l);
					rep.setItemMeta(repMeta);
				}

				this.page.getView().setSlot(53, icon);
				this.apply();
				delays.put(clickable.getPlayer().getUniqueId(), Instant.now().plusSeconds(3));
			}
		});

		return icon;
	}
}
