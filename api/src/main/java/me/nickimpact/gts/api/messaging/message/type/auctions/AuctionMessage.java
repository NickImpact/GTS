package me.nickimpact.gts.api.messaging.message.type.auctions;

import me.nickimpact.gts.api.messaging.message.OutgoingMessage;
import me.nickimpact.gts.api.messaging.message.type.MessageType;
import net.kyori.text.TextComponent;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a message that belongs to an auction. These messages are meant to inform a user
 * about an action that has just occurred with an auction. These are only necessary when GTS
 * is running on a multi-server platform.
 */
public interface AuctionMessage extends OutgoingMessage {

	/**
	 * Represents the ID of the auction being acted on. This ID is the primary key to locating an auction,
	 * and as such, should be unique to one specific auction.
	 *
	 * @return The ID of the auction
	 */
	@NonNull UUID getAuctionID();

	/**
	 * Represents the UUID of a player or another source that is applying the action to this auction.
	 *
	 * @return The UUID of the source applying the action
	 */
	@NonNull UUID getActor();

	/**
	 * This message type indicates that a new auction listing has been published.
	 *
	 * We cannot expect the message to be populated with physical decoded data given that this
	 * message can be received on a proxy, where extensions should not be installed at all. Along
	 * with that detail, the proxy will never even attempt to deserialize the raw data of a listing.
	 * Therefore, this message is simply populated with the message that indicates that the listing
	 * is actually being published globally, or within the scope of the configuration.
	 */
	interface Publish extends AuctionMessage {}

	/**
	 * Represents a bid action. This message is created whenever a player decides to bid on an auction.
	 * The responsibility of this message is to inform the users who have all made bids, as well
	 * as the seller, that a new bid has been placed, either outbidding someone else, or simply
	 * just informing the seller.
	 *
	 * <p>To aid in avoiding redundant querying, this message will be processed on the proxy as it comes
	 * in. On the proxy, the listing will be queried for through the appropriate storage manager, applying
	 * that data to this message as to populate it with that information. This information includes the
	 * seller and the mapping of bids.</p>
	 *
	 * <p>It is expected on the proxy to have the seller and bid list be possibly null. In other words,
	 * this is really only meant to suggest that the proxy should be the one processing the listing, and then
	 * reporting all other relevant information.</p>
	 */
	interface Bid extends AuctionMessage {

		@Positive double getAmountBid();

		/**
		 * Represents the message that'll be sent to the proxy servers. While it contains no further data, this
		 * message is built to simply be an identifier for the proxy.
		 */
		interface Request extends Bid, MessageType.Request<Response> {}

		/**
		 * Represents the response that'll be sent out to all servers. Essentially, the bid message will be initially
		 * processed by the Request, and evaluated for its information. After the proxy has determined the results of the
		 * bid, it'll send this response message, which specifies whether or not the bid was successful, who the seller
		 * was, and a mapping of all bids placed by other players.
		 */
		interface Response extends Bid, MessageType.Response {

			/**
			 * Specifies the seller of the listing. The seller is the individual who should receive notice of their
			 * auction being bid on, when a bid is successful.
			 *
			 * @return The UUID of the user who created the auction
			 */
			@NonNull UUID getSeller();

			/**
			 * Represents a filtered map of users to their highest bids. While a user may have multiple bids on a listing,
			 * this data will only be populated by their highest bids. This is meant to be a quick and easy way to inform
			 * a user of just how much money they are outbid by.
			 *
			 * @return A mapping of users to their highest bids on the auction currently
			 */
			@NonNull Map<UUID, Double> getAllOtherBids();
		}
	}

	/**
	 * Represents the message indicating a user who is attempting to claim a part of their auction after
	 * it has expired. Since auctions are special in that they have two rewards versus one, we need to
	 * process the claim accordingly as to ensure we don't delete the auction from memory until both claim
	 * requests have been processed.
	 */
	interface Claim extends AuctionMessage {

		interface Request extends Claim, MessageType.Request<Response> {

			boolean isLister();

		}

		interface Response extends Claim, MessageType.Response {}

	}

	/**
	 * Represents the act of the auction creator cancelling their auction. This message is entirely dependent on
	 * two main aspects of plugin configuration: The allowance of a user being able to cancel an auction, and the
	 * amount of time left on the auction per some ratio that'll actually allow a user to cancel their auction.
	 *
	 * <p>If an auction is cancelled, this message will thereby be sent as a means to inform all current
	 * bidders that the auction has been cancelled.</p>
	 */
	interface Cancel extends AuctionMessage {

		interface Request extends Cancel, MessageType.Request<Response> {}

		/**
		 * After processing, this is the message that the proxy will attempt to send back to the servers.
		 */
		interface Response extends Cancel, MessageType.Response {

			/**
			 * Specifies the list of users who have bid on this auction at least once. They should
			 * be informed of the auction being cancelled.
			 *
			 * @return The list of all bidders
			 */
			List<UUID> getBidders();

		}
	}
}
