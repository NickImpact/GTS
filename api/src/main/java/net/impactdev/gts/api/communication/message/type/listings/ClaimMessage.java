package net.impactdev.gts.api.communication.message.type.listings;

import net.impactdev.gts.api.communication.message.OutgoingMessage;
import net.impactdev.gts.api.communication.message.type.MessageType;
import net.impactdev.gts.api.util.TriState;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents an attempt by a user to claim an item of theirs granted by GTS.
 */
public interface ClaimMessage extends OutgoingMessage {

    /**
     * Represents the ID of the auction being acted on. This ID is the primary key to locating an auction,
     * and as such, should be unique to one specific auction.
     *
     * @return The ID of the auction
     */
    @NonNull UUID getListingID();

    /**
     * Represents the UUID of a player or another source that is applying the action to this auction.
     *
     * @return The UUID of the source applying the action
     */
    @NonNull UUID getActor();

    /**
     * Represents the ID of the user that will be receiving this listing. This is primarily for
     * admin usage. Otherwise, a normal claim operation will find this value always empty.
     *
     * @return The UUID of the intended receiver, if one is set
     */
    Optional<UUID> getReceiver();

    /**
     * Specifies whether or not this claim request was based on an auction.
     *
     * @return True if the claim request was for an auction, false for BIN
     */
    boolean isAuction();

    /**
     * This represents the actual request made to the messaging service, in which we expect a response indicating
     * whether or not the action can go through.
     *
     * If our claim is based around an auction, this request should return an {@link ClaimMessage.Response.AuctionResponse}
     * that includes the extended information.
     */
    interface Request extends ClaimMessage, MessageType.Request<Response> {}

    /**
     * This represents the response to a request. This will be used to determine whether the claim request proceeded
     * successfully, or received some kind of error stopping the request.
     */
    interface Response extends ClaimMessage, MessageType.Response {

        /**
         * A child of a response, specific to an attempt to claim from an auction. This carries
         * more information in regards to the auction such that we can ensure successful
         * retrieval of goods per each user.
         */
        interface AuctionResponse extends ClaimMessage.Response {

            /**
             * Indicates that the lister of the auction has claimed their portion of the auction.
             *
             * @return True if the lister has claimed part of the auction, false otherwise
             */
            boolean hasListerClaimed();

            /**
             * Indicates that the winner of the auction has claimed their portion of the auction.
             *
             * @return True if the winner has claimed part of the auction, false otherwise
             */
            boolean hasWinnerClaimed();

            /**
             * Specifies whether or not a particular user has claimed their bid they placed on an auction
             * that they did not win.
             *
             * @param uuid The ID of the user
             * @return True if the user has claimed, false if not, undefined if they are not available
             * within the contextual query
             */
            TriState hasOtherBidderClaimed(UUID uuid);

            /**
             * Specifies the list of users who have lost this auction, and have already claimed their money
             * back from their loss.
             *
             * @return The list of users who lost this auction and claimed their money back
             */
            List<UUID> getAllOtherClaimers();

        }

    }

}
