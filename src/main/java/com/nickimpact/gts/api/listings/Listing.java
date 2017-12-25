package com.nickimpact.gts.api.listings;

import com.nickimpact.gts.api.listings.data.AuctionData;
import com.nickimpact.gts.api.listings.entries.Entry;
import lombok.Getter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 *
 *
 * @author NickImpact
 */
public class Listing {

    /** The ID of the entry */
    @Getter private int ID;

    /** The owner of the entry */
    @Getter private String ownerName;

    /** The uuid of the entry owner */
    @Getter private UUID ownerUUID;

    @Getter private Entry entry;

    /** Whether or not the entry will expire */
    private boolean expires;

    /** When the lot will expire, if the above is true */
    @Getter private Date expiration;

    /** Represents the data for an auction, if the listing is in fact one */
    @Getter private AuctionData aucData;

	/**
	 * Constructs the content of a listing that will be available on the GTS market
	 *
	 * @param id The listing ID
	 * @param player The player depositing the listing
	 * @param entry The element represented by the listing
	 * @param expires Whether or not the listing will expire
	 * @param seconds The duration of the listing's lifetime
	 */
    public Listing(int id, Player player, Entry entry, boolean expires, long seconds){
        this(id, player, entry, expires, seconds, null);
    }

	/**
	 * Constructs the content of a listing that will be available on the GTS market
	 *
	 * @param id The listing ID
	 * @param player The player depositing the listing
	 * @param entry The element represented by the listing
	 * @param expires Whether or not the listing will expire
	 * @param seconds The duration of the listing's lifetime
	 * @param ad Data representing an auction (can be null to state it is not an auction)
	 */
    public Listing(int id, Player player, Entry entry, boolean expires, long seconds, @Nullable AuctionData ad) {
	    this.ID = id;
	    this.ownerName = player.getName();
	    this.ownerUUID = player.getUniqueId();
	    this.entry = entry;
	    this.expires = expires;
	    this.expiration = Date.from(Instant.now().plusSeconds(seconds));
	    this.aucData = ad;
    }

	/**
	 * States whether or not a lot entry can expire
	 *
	 * @return True if it can expire, false otherwise
	 */
	public boolean canExpire() {
        return expires;
    }

	/**
	 * Checks whether or not a listing has exceeded its allocated listing time.
	 *
	 * @return Whether or not a listing has expired
	 */
	public boolean checkHasExpired() {
        return getExpiration().before(Date.from(Instant.now()));
    }

	/**
	 * Returns the name of a lot entry. Common uses of this method might be its use in messages displayed
	 * to users.
	 *
	 * @return The name of a lot entry.
	 */
	public String getName() {
		return entry.getName();
	}

	public ItemStack getDisplay(Player player, boolean confirm) {
		if(confirm) {
			return this.entry.getConfirmDisplay(player, this);
		} else {
			return this.entry.getBaseDisplay(player, this);
		}
	}
}
