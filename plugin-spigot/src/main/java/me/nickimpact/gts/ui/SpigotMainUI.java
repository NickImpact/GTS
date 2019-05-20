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
import me.nickimpact.gts.spigot.SpigotListing;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

	static {
		GRAY_BORDER = new SpigotIcon(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)15));
		ItemMeta meta = GRAY_BORDER.getDisplay().getItemMeta();
		meta.setDisplayName(" ");
		GRAY_BORDER.getDisplay().setItemMeta(meta);
	}

	public SpigotMainUI(Player viewer) {
		this.viewer = viewer;
		this.searchConditions.add(listing -> !listing.hasExpired());
		this.searchConditions.add(listing -> {
			if(this.classSelection == null) return true;
			return listing.getEntry().getClass().isAssignableFrom(classSelection);
		});

		this.page = SpigotPage.builder()
				.viewer(viewer)
				.view(this.design())
				.title(ChatColor.RED + "GTS " + ChatColor.GRAY + "(" + ChatColor.DARK_AQUA + "Listings" + ChatColor.GRAY + ")")
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
					new SpigotConfirmUI(this.viewer, listing).open();
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
		rMeta.setDisplayName(ChatColor.YELLOW + "Refresh Listings");
		refresher.setItemMeta(rMeta);
		SpigotIcon rIcon = new SpigotIcon(refresher);
		rIcon.addListener(clickable -> {
			this.apply();
		});

		builder.slot(rIcon, 49);

		ItemStack pListings = new ItemStack(Material.WRITTEN_BOOK);
		ItemMeta pMeta = pListings.getItemMeta();
		pMeta.setDisplayName(ChatColor.YELLOW + "Your Listings");
		pMeta.setLore(Lists.newArrayList(ChatColor.GRAY + "Status: " + ChatColor.RED + "Disabled"));
		pMeta.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES);
		pMeta.hasItemFlag(ItemFlag.HIDE_ENCHANTS);
		pListings.setItemMeta(pMeta);
		SpigotIcon pIcon = new SpigotIcon(pListings);
		pIcon.addListener(clickable -> {
			this.justPlayer = !this.justPlayer;
			pMeta.setLore(Lists.newArrayList(ChatColor.GRAY + "Status: " + (this.justPlayer ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")));
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
			eMeta.setDisplayName(ChatColor.RED + "No Entry Types Available");
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
			listings = manager.getListings().stream().filter(listing -> listing.getOwnerUUID().equals(this.viewer.getUniqueId())).collect(Collectors.toList());
		} else {
			if(!this.searchConditions.isEmpty()) {
				listings = manager.getListings().stream().filter(listing -> {
					boolean passed = true;
					for(Predicate<Listing> predicate : this.searchConditions) {
						passed = passed && predicate.test(listing);
					}

					return passed;
				}).collect(Collectors.toList());
			}
		}

		return listings;
	}

	private SpigotIcon classificationToIcon(EntryClassification classification) {
		ItemStack rep = new ItemStack(Material.matchMaterial(classification.getItemRep()));
		ItemMeta repMeta = rep.getItemMeta();
		repMeta.setDisplayName(ChatColor.YELLOW + "Show only " + classification.getPrimaryIdentifier() + "?");
		repMeta.setLore(Lists.newArrayList(
				ChatColor.GRAY + "Status: " + (classification.getClassification().equals(this.classSelection) ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"),
				"",
				ChatColor.AQUA + "Controls:",
				ChatColor.GRAY + "Left Click: " + ChatColor.GREEN + "Apply filter",
				ChatColor.GRAY + "Right Click: " + ChatColor.GREEN + "Switch filter",
				"",
				ChatColor.AQUA + "NOTE:",
				ChatColor.GRAY + "This option will be overridden by",
				ChatColor.GRAY + "the " + ChatColor.YELLOW + "Your Listings " + ChatColor.GRAY + "option",
				ChatColor.GRAY + "if it is enabled."
		));
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

				List<String> lore = repMeta.getLore();
				if(classification.getClassification().equals(this.classSelection)) {
					this.classSelection = null;
					lore.set(0, ChatColor.GRAY + "Status: " + ChatColor.RED + "Disabled");
					repMeta.setLore(lore);
					rep.setItemMeta(repMeta);
				} else {
					this.classSelection = classification.getClassification();
					lore.set(0, ChatColor.GRAY + "Status: " + ChatColor.GREEN + "Enabled");
					repMeta.setLore(lore);
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
