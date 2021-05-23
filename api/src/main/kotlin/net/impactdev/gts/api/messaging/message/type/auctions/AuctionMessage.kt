package net.impactdev.gts.api.messaging.message.type.auctions

import com.google.common.collect.TreeMultimap
import net.impactdev.gts.api.listings.auctions.Auction
import net.impactdev.gts.api.messaging.message.OutgoingMessage
import net.impactdev.gts.api.messaging.message.type.MessageType
import org.checkerframework.checker.index.qual.Positive
import java.util.*

/**
 * Represents a message that belongs to an auction. These messages are meant to inform a user
 * about an action that has just occurred with an auction.
 */
interface AuctionMessage : OutgoingMessage {
    /**
     * Represents the ID of the auction being acted on. This ID is the primary key to locating an auction,
     * and as such, should be unique to one specific auction.
     *
     * @return The ID of the auction
     */
    val auctionID: UUID

    /**
     * Represents the UUID of a player or another source that is applying the action to this auction.
     *
     * @return The UUID of the source applying the action
     */
    val actor: UUID

    /**
     * Represents a bid action. This message is created whenever a player decides to bid on an auction.
     * The responsibility of this message is to inform the users who have all made bids, as well
     * as the seller, that a new bid has been placed, either outbidding someone else, or simply
     * just informing the seller.
     *
     *
     * To aid in avoiding redundant querying, this message will be processed on the proxy as it comes
     * in. On the proxy, the listing will be queried for through the appropriate storage manager, applying
     * that data to this message as to populate it with that information. This information includes the
     * seller and the mapping of bids.
     *
     *
     * It is expected on the proxy to have the seller and bid list be possibly null. In other words,
     * this is really only meant to suggest that the proxy should be the one processing the listing, and then
     * reporting all other relevant information.
     */
    interface Bid : AuctionMessage {
        val amountBid: @Positive Double

        /**
         * Represents the message that'll be sent to the proxy servers. While it contains no further data, this
         * message is built to simply be an identifier for the proxy.
         */
        interface Request : Bid, MessageType.Request<Response?>

        /**
         * Represents the response that'll be sent out to all servers. Essentially, the bid message will be initially
         * processed by the Request, and evaluated for its information. After the proxy has determined the results of the
         * bid, it'll send this response message, which specifies whether or not the bid was successful, who the seller
         * was, and a mapping of all bids placed by other players.
         */
        interface Response : Bid, MessageType.Response {
            /**
             * Specifies the seller of the listing. The seller is the individual who should receive notice of their
             * auction being bid on, when a bid is successful.
             *
             * @return The UUID of the user who created the auction
             */
            val seller: UUID

            /**
             * Represents a filtered map of users to their highest bids. While a user may have multiple bids on a listing,
             * this data will only be populated by their highest bids. This is meant to be a quick and easy way to inform
             * a user of just how much money they are outbid by.
             *
             * @return A mapping of users to their highest bids on the auction currently
             */
            val allOtherBids: TreeMultimap<UUID?, Auction.Bid?>
        }
    }

    /**
     * Represents the act of the auction creator cancelling their auction. This message is entirely dependent on
     * two main aspects of plugin configuration: The allowance of a user being able to cancel an auction, and the
     * amount of time left on the auction per some ratio that'll actually allow a user to cancel their auction.
     *
     *
     * If an auction is cancelled, this message will thereby be sent as a means to inform all current
     * bidders that the auction has been cancelled.
     */
    interface Cancel : AuctionMessage {
        interface Request : Cancel, MessageType.Request<Response?>

        /**
         * After processing, this is the message that the proxy will attempt to send back to the servers.
         */
        interface Response : Cancel, MessageType.Response {
            /**
             * Contextual information regarding this listing will be deleted by the time a server processing
             * the response will have begun. As such, we will pass a copy of the data alongside the response
             * so listeners can interact with the information.
             *
             * @return The data of the auction that was just cancelled
             */
            val data: Auction?

            /**
             * Specifies the list of users who have bid on this auction at least once. They should
             * be informed of the auction being cancelled.
             *
             * @return The list of all bidders
             */
            val bidders: List<UUID?>?
        }
    }
}