package com.nickimpact.gts.api.listings;

import com.nickimpact.gts.api.listings.data.AuctionData;
import com.nickimpact.gts.api.listings.entries.Entry;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

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
    private int id;

    /** The owner of the entry */
    private String oName;

    /** The uuid of the entry owner */
    private UUID oUUID;

    private Entry entry;

    /** Whether or not the entry will expire */
    private boolean expires;

    /** When the lot will expire, if the above is true */
    private Date expiration;

    /** Represents the data for an auction, if the listing is in fact one */
    private AuctionData aucData;

	/**
	 * Constructs the contents of an entry that will partake within the GTS listings
	 *
	 * @param id The entry id
	 * @param player The player who is depositing the entry
	 * @param entry The element held within a listing
	 * @param expires Whether or not the entry will expire
	 * @param seconds How many seconds a lot should exist for
	 */
    public Listing(int id, Player player, Entry entry, boolean expires, long seconds){
        this.id = id;
        this.oName = player.getName();
        this.oUUID = player.getUniqueId();
        this.entry = entry;

        this.expires = expires;
        expiration = Date.from(Instant.now().plusSeconds(seconds));
    }

    public Listing(int id, Entry entry, boolean expires, long seconds) {
    	this(id, entry, expires, seconds, null);

    }

    public Listing(int id, Entry entry, boolean expires, long seconds, AuctionData ad) {
	    this.id = id;
	    this.oName = "Test";
	    this.oUUID = UUID.randomUUID();
	    this.entry = entry;
	    this.expires = expires;
	    this.expiration = Date.from(Instant.now().plusSeconds(seconds));
	    this.aucData = ad;
    }

	/**
	 * Fetches the ID of a lot entry
	 *
	 * @return The ID of a lot entry
	 */
	public int getID() {
        return this.id;
    }

	/**
	 * Fetches the owner, by name, of a lot entry
	 *
	 * @return The owner of a lot entry
	 */
	public String getOwnerName() {
        return this.oName;
    }

	/**
	 * Fetches the owner, by UUID, of a lot entry
	 *
	 * @return The UUID of a lot entry's owner
	 */
	public UUID getOwnerUUID() {
        return this.oUUID;
    }

	/**
	 * States the specified pricing of an entry. The pricing is to be declared by an incoming function.
	 *
	 * @return The mode the entry is assigned to
	 */
	public Entry getEntry() {
        return this.entry;
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
	 * States the Date a lot will expire on
	 *
	 * @return The Data a lot will expire on
	 */
	public Date getExpiration() {
        return expiration;
    }

	/**
	 * Checks whether or not a listing has exceeded its allocated listing time.
	 *
	 * @return Whether or not a listing has expired
	 */
	public boolean checkHasExpired() {
		System.out.println(getExpiration() + " vs " + Date.from(Instant.now()));
		System.out.println(getExpiration().before(Date.from(Instant.now())));
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

	public AuctionData getAucData() {
		return aucData;
	}
}
