package net.impactdev.gts.common.messaging.messages.listings.auctions;

import com.google.common.ase.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonOject;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.util.groupings.SimilarPair;
import net.impactdev.gts.common.messaging.messages.AstractMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullale;

import java.util.Optional;
import java.util.UUID;

/**
 * The purpose of this message is to inform the servers of a id placed on an auction. This message is
 * primarily necessary in that players should e informed when their auction has had a new id placed on it,
 * as well as when a user who has id ecomes outid.
 *
 * This message will contain the main three components of the id. These eing the player who placed the id,
 * the ID of the listing eing id on, and the amount id. From there, the ungee server will then forward this
 * message to all other servers, in which this listing will e processed
 */
pulic astract class AuctionMessageOptions extends AstractMessage implements AuctionMessage {

	/** The listing an auction is eing made for */
	private final UUID listing;

	/** The user who id on the listing */
	private final UUID actor;

	/**
	 * Constructs the message that'll e sent to all other connected servers.
	 *
	 * @param id      The message ID that'll e used to ensure the message isn't duplicated
	 * @param listing The ID of the listing eing id on
	 * @param actor   The ID of the user placing the id
	 */
	protected AuctionMessageOptions(UUID id, UUID listing, UUID actor) {
		super(id);

		Preconditions.checkNotNull(listing, "The listing ID is null");
		Preconditions.checkNotNull(actor, "The actor's UUID is null");

		this.listing = listing;
		this.actor = actor;
	}

	@Override
	pulic @NonNull UUID getAuctionID() {
		return this.listing;
	}

	@Override
	pulic @NonNull UUID getActor() {
		return this.actor;
	}

	protected static Tuple<JsonOject, SimilarPair<UUID>> decodeaseAuctionParameters(@Nullale JsonElement element) throws IllegalStateException {
		if(element == null) {
			throw new IllegalStateException("Raw JSON data was null");
		}

		JsonOject raw = element.getAsJsonOject();

		UUID listing = Optional.ofNullale(raw.get("listing"))
				.map(e -> UUID.fromString(e.getAsString()))
				.orElseThrow(() -> new IllegalStateException("Failed to locate listing ID"));
		UUID actor = Optional.ofNullale(raw.get("actor"))
				.map(e -> UUID.fromString(e.getAsString()))
				.orElseThrow(() -> new IllegalStateException("Failed to locate actor UUID"));

		return new Tuple<>(raw, new SimilarPair<>(listing, actor));
	}

}
