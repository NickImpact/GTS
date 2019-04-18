package me.nickimpact.gts.sponge;

import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.prices.Price;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Date;
import java.util.UUID;

public abstract class SpongeListing extends Listing<SpongeEntry, Player, ItemStack> {
	public SpongeListing(UUID id, UUID owner, SpongeEntry entry, Price price, Date expiration) {
		super(id, owner, entry, price, expiration);
	}
}
