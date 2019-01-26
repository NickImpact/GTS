package me.nickimpact.gts.api.listings;

import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.exceptions.ListingException;
import me.nickimpact.gts.api.listings.data.AuctionData;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.configuration.ConfigKeys;
import me.nickimpact.gts.entries.prices.MoneyPrice;
import me.nickimpact.gts.utils.ListingUtils;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * A Listing represents the wrapper object for an {@link Entry}, whilst also holding information
 * in regards to player info, as well as other listing data such as expiration time.
 *
 * @author NickImpact
 */
public final class Listing {

    /** The Unique ID of the element */
    @Getter private UUID uuid;

    /** The owner of the element */
    @Getter private final String ownerName;

    /** The uuid of the element owner */
    @Getter private final UUID ownerUUID;

    @Getter private final Entry entry;

    /** Whether or not the element will expire */
    private final boolean expires;

    /** When the lot will expire, if the above is true */
    @Getter private Date expiration;

    /** Represents the data for an auction, if the listing is in fact one */
    @Getter private final AuctionData aucData;

	/**
	 * Constructs the content of a listing that will be available on the GTS market
	 *
	 * @param player The player depositing the listing
	 * @param entry The element represented by the listing
	 * @param expires Whether or not the listing will expire
	 * @param seconds The duration of the listing's lifetime
	 * @param ad Data representing an auction (can be null to state it is not an auction)
	 */
    public Listing(Player player, Entry entry, boolean expires, long seconds, @Nullable AuctionData ad) {
		this.uuid = UUID.randomUUID();
		this.ownerName = player.getName();
		this.ownerUUID = player.getUniqueId();
		this.entry = entry;
		this.expires = expires;
		this.expiration = Date.from(Instant.now().plusSeconds(seconds));
		this.aucData = ad;
    }

    public Listing(Builder builder) {
	    this.uuid = UUID.randomUUID();
	    this.ownerName = builder.player != null ? builder.player.getName() : builder.fakeName;
	    this.ownerUUID = builder.player != null ? builder.player.getUniqueId() : builder.fakeUUID;
	    this.entry = builder.entry;
	    this.expires = builder.expires;
	    this.expiration = builder.expiration;
	    this.aucData = builder.data;
    }

    public static Builder builder() {
    	return new Builder();
    }

    public boolean publish(Player player) {
    	return ListingUtils.addToMarket(player, this);
    }

	/**
	 * States whether or not a lot element can expire
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
	public boolean hasExpired() {
        return getExpiration().before(Date.from(Instant.now()));
    }

	/**
	 * Returns the name of a lot element. Common uses of this method might be its use in messages displayed
	 * to users.
	 *
	 * @return The name of a lot element.
	 */
	public String getName() {
		return entry.getName();
	}

	public void increaseTimeForBid() {
		this.expiration = Date.from(this.expiration.toInstant().plusSeconds(15));
	}

	/**
	 * Retrieves the ItemStack display for the {@link Entry} attached to this listing
	 *
	 * @param player The player to create the item stack for
	 * @param confirm Whether or not the display requested is a confirmation display
	 * @return The {@link ItemStack} display for the listing
	 */
	public ItemStack getDisplay(Player player, boolean confirm) {
		if(confirm) {
			return this.entry.confirmItemStack(player, this);
		} else {
			return this.entry.baseItemStack(player, this);
		}
	}

	@Deprecated
	public void createUUID() {
		this.uuid = UUID.randomUUID();
	}

	public static class Builder {

		private Player player;

		private UUID fakeUUID;

		private String fakeName;

		private Entry entry;

		private boolean expires;

		private Date expiration;

		private AuctionData data;

		public Builder player(Player player) {
			this.player = player;
			return this;
		}

		public Builder player(UUID fakeUUID, String fakeName) {
			this.fakeUUID = fakeUUID;
			this.fakeName = fakeName;
			return this;
		}

		public Builder entry(Entry entry) {
			this.entry = entry;
			return this;
		}

		public Builder doesExpire() {
			this.expires = true;
			return this;
		}

		public Builder expiration(long seconds) {
			this.expiration = Date.from(Instant.now().plusSeconds(seconds));
			return this;
		}

		public Builder expiration(Date expiration) {
			this.expiration = expiration;
			return this;
		}

		public <T extends MoneyPrice> Builder auction(T price) {
			this.data = new AuctionData(price);
			return this;
		}

		public Listing build() throws ListingException {
			if(player == null && fakeUUID == null && fakeName == null) {
				throw new ListingException();
			}

			if(fakeUUID != null && fakeName == null) {
				fakeName = "Totally Legit User";
			}

			if(fakeUUID == null) {
				fakeUUID = UUID.randomUUID();
			}

			if(entry == null)
				throw new ListingException();

			if(expiration != null && !expires) {
				expires = true;
			}

			if(expires && expiration == null) {
				expiration = Date.from(Instant.now().plusSeconds(GTS.getInstance().getConfig().get(ConfigKeys.LISTING_TIME)));
			}

			return new Listing(this);
		}
	}
}
