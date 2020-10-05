package net.impactdev.gts.common.messaging.messages.listings.auctions;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.util.groupings.SimilarPair;
import net.impactdev.gts.common.messaging.messages.AbstractMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * The purpose of this message is to inform the servers of a bid placed on an auction. This message is
 * primarily necessary in that players should be informed when their auction has had a new bid placed on it,
 * as well as when a user who has bid becomes outbid.
 *
 * This message will contain the main three components of the bid. These being the player who placed the bid,
 * the ID of the listing being bid on, and the amount bid. From there, the bungee server will then forward this
 * message to all other servers, in which this listing will be processed
 */
public abstract class AuctionMessageOptions extends AbstractMessage implements AuctionMessage {

	/** The listing an auction is being made for */
	private final UUID listing;

	/** The user who bid on the listing */
	private final UUID actor;

	/**
	 * Constructs the message that'll be sent to all other connected servers.
	 *
	 * @param id      The message ID that'll be used to ensure the message isn't duplicated
	 * @param listing The ID of the listing being bid on
	 * @param actor   The ID of the user placing the bid
	 */
	protected AuctionMessageOptions(UUID id, UUID listing, UUID actor) {
		super(id);

		Preconditions.checkNotNull(listing, "The listing ID is null");
		Preconditions.checkNotNull(actor, "The actor's UUID is null");

		this.listing = listing;
		this.actor = actor;
	}

	@Override
	public @NonNull UUID getAuctionID() {
		return this.listing;
	}

	@Override
	public @NonNull UUID getActor() {
		return this.actor;
	}

	protected static Tuple<JsonObject, SimilarPair<UUID>> decodeBaseAuctionParameters(@Nullable JsonElement element) throws IllegalStateException {
		if(element == null) {
			throw new IllegalStateException("Raw JSON data was null");
		}

		JsonObject raw = element.getAsJsonObject();

		UUID listing = Optional.ofNullable(raw.get("listing"))
				.map(e -> UUID.fromString(e.getAsString()))
				.orElseThrow(() -> new IllegalStateException("Failed to locate listing ID"));
		UUID actor = Optional.ofNullable(raw.get("actor"))
				.map(e -> UUID.fromString(e.getAsString()))
				.orElseThrow(() -> new IllegalStateException("Failed to locate actor UUID"));

		return new Tuple<>(raw, new SimilarPair<>(listing, actor));
	}
}
