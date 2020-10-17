package net.impactdev.gts.api.messaging.message.type.listings;

import net.impactdev.gts.api.messaging.message.OutgoingMessage;
import net.impactdev.gts.api.messaging.message.type.MessageType;

import java.util.Optional;
import java.util.UUID;

/**
 * These messages belong to a listing with a singular and finalized selling price. In other
 * words, unlike auctions, players will not be at war with each other for a set of time in
 * order to win the listing. Rather, a player can outright purchase the item immediately.
 */
public interface BuyItNowMessage extends OutgoingMessage {

	/**
	 * Specifies the ID of the listing this message is based on. It'll be used for reference
	 * by any messages that may require it.
	 *
	 * @return The ID of the quick purchase listing.
	 */
	UUID getListingID();

	/**
	 * Represents the ID of the user who is making the purchase. This is to allow for a quick response
	 * rather than need to require a mapping of actor to sent message IDs.
	 *
	 * @return The ID of the user who made the purchase
	 */
	UUID getActor();

	/**
	 * This message type indicates that a new quick purchase listing has been published.
	 *
	 * We cannot expect the message to be populated with physical decoded data given that this
	 * message can be received on a proxy, where extensions should not be installed at all. Along
	 * with that detail, the proxy will never even attempt to deserialize the raw data of a listing.
	 * Therefore, this message is simply populated with the message that indicates that the listing
	 * is actually being published globally, or within the scope of the configuration.
	 */
	interface Publish extends BuyItNowMessage {}

	/**
	 * These messages indicate that a purchase attempt has been made on a listing. Being as these messages
	 * can possibly be concurrent, these messages make use of the request and response structure.
	 *
	 * By supplying the actor's UUID, we can make the request and response rather quick for the user making
	 * the request, assuming the storage provider holds up.
	 */
	interface Purchase extends BuyItNowMessage {

		/**
		 * This message indicates a request being made to purchase an item off the GTS. This request will then
		 * be processed by the active GTS Service manager in action, as a means to ensure that the purchase
		 * is indeed valid.
		 *
		 * A valid request will then be further processed, apply the necessary actions to the storage manager,
		 * and then further respond to the request with a new {@link Purchase.Response Response} message.
		 */
		interface Request extends Purchase, MessageType.Request<Response> {}

		/**
		 * This message indicates the response to a {@link Purchase.Request Request} made prior to this
		 * message. In general, this message simply contains the UUID of the listing being purchased,
		 * as well as the success status of the request.
		 */
		interface Response extends Purchase, MessageType.Response {}

	}

	/**
	 * This messages indicate the attempt to remove a quick purchase listing from the GTS market entirely.
	 * A listing may or may not be already purchased or removed prior to this request to remove it, and as such
	 * this message also builds a response message in relation to a request made indicating success.
	 */
	interface Remove extends BuyItNowMessage {

		/**
		 * Specifies the UUID of the user this listing should be returned to. This field is only checked
		 * if the value of {@link #shouldReturnListing()} is true.
		 *
		 * If this value is true, it is expected that this value will be populated. Otherwise, an exception
		 * will likely be thrown in response.
		 *
		 * Given this can be administratively received/taken, the ID of the user can also be the administrator
		 * executing a removal rather than the actual seller of the listing.
		 *
		 * @return The ID of the user optionally wrapped based on request parameters.
		 */
		Optional<UUID> getRecipient();

		/**
		 * Indicates whether or not this listing should have its item returned to the user who published it.
		 * This is to ensure that items being removed for a specific purpose (server rule violations and etc.)
		 * are not returned to the user and rather just deleted from the system.
		 *
		 * @return True if the listing should be returned to a user, false otherwise.
		 */
		boolean shouldReturnListing();

		/**
		 * This message indicates a user's request to remove a quick purchase listing from the GTS. While this
		 * message contains no extended data, it will at least generate a response stating the outcome of the
		 * removal attempt.
		 */
		interface Request extends Remove, MessageType.Request<Response> {}

		/**
		 * This message indicates a response to a user's request to remove a quick purchase listing from
		 * the GTS. Unlike the request, this response contains an extra field stating if the request was
		 * successfully completed. A response will be marked a failure only if the listing in question is
		 * no longer in the storage provider, which likely means the listing was removed from a purchase
		 * or another request prior to the request this response is now responding to.
		 */
		interface Response extends Remove, MessageType.Response {}

	}

}
