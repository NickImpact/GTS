package net.impactdev.gts.api.messaging.message.type.admin;

import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.messaging.message.OutgoingMessage;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.type.MessageType;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.utilities.Builder;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents the message that'll be sent when an administrator decides to force delete a listing off the
 * GTS market. Realistically, returning the data to a user is outside the scope of the message, and should
 * be handled by the requester.
 */
public interface ForceDeleteMessage extends OutgoingMessage {

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
     * States if this request should return the data contained in the listing if it is deleted.
     *
     * @return True if the entry should be returned to the user on deletion
     */
    boolean shouldGive();

    interface Request extends ForceDeleteMessage, MessageType.Request<Response> {}

    interface Response extends ForceDeleteMessage, MessageType.Response {

        /**
         * Represents the UUID of the user who had their listing deleted. Since the listing will have been
         * deleted before the response is made available to the requesting server, we will need to supply
         * information back to the server about the listing.
         *
         * Note that if the request was marked unsuccessful, this value will not be populated.
         *
         * @return The ID of the user who made the listing
         */
        Optional<Listing> getDeletedListing();

        static ResponseBuilder builder() {
            return Impactor.getInstance().getRegistry().createBuilder(ResponseBuilder.class);
        }

        interface ResponseBuilder extends Builder<ForceDeleteMessage.Response, ResponseBuilder> {

            ResponseBuilder request(UUID request);

            ResponseBuilder listing(UUID listing);

            ResponseBuilder actor(UUID actor);

            ResponseBuilder data(Listing data);

            ResponseBuilder give(boolean give);

            ResponseBuilder successful(boolean successful);

            ResponseBuilder error(ErrorCode error);

        }

    }

}
