package me.nickimpact.gts.sponge;

import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.prices.Price;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;

public abstract class SpongeEntry<T, D> extends Entry<T, D, Player, User, ItemStack> {

	public SpongeEntry(T element) {
		super(element);
	}
}
