package me.nickimpact.gts.listings;

import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.spigot.SpigotEntry;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SpigotItemEntry extends SpigotEntry<String, ItemStack> {

	public SpigotItemEntry(String element) {
		super(element);
	}

	@Override
	public ItemStack getEntry() {
		return null;
	}


	@Override
	public String getSpecsTemplate() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public List<String> getDetails() {
		return null;
	}

	@Override
	public ItemStack baseItemStack(Player player, Listing listing) {
		return null;
	}

	@Override
	public ItemStack confirmItemStack(Player player, Listing listing) {
		return null;
	}

	@Override
	public boolean supportsOffline() {
		return false;
	}

	@Override
	public boolean giveEntry(OfflinePlayer user) {
		return false;
	}

	@Override
	public boolean doTakeAway(Player player) {

		return false;
	}
}
