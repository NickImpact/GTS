package net.impactdev.gts.api.messaging.message.type.listings;

import net.impactdev.gts.api.messaging.message.OutgoingMessage;

import java.util.UUID;

/**
 * This message indicates that a listing has recently been published, and we should attempt
 * to notify all servers of the listing.
 */
public interface PublishListingMessage extends OutgoingMessage {

    UUID getListingID();

    UUID getActor();

    boolean isAuction();

}
