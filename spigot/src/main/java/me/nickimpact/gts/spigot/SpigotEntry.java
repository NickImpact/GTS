package me.nickimpact.gts.spigot;

import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.prices.Price;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class SpigotEntry<T, D> extends Entry<T, D, Player, OfflinePlayer, ItemStack> {
	public SpigotEntry(T element) {
		super(element);
	}
}
