package net.impactdev.gts.api.messaging.message.type.stash;

import net.impactdev.gts.api.messaging.message.OutgoingMessage;
import net.impactdev.gts.api.messaging.message.type.MessageType;
import net.impactdev.gts.api.stashes.Stash;

/**
 * Represents a message relating to the act of working on a player's stash. The {@link Stash Stash}
 * is a item preserver for users who have outgoing listings or auctions and have received an item they may not yet be able
 * to retrieve due to some condition not being met and the like. This message will typically only be requested once
 * in response to a player logging in. From there, the stash will be cached on the server. Any updates made to that stash
 * will feature a secondary message indicating the claim results.
 */
public interface StashMessage extends OutgoingMessage {

	interface Request extends StashMessage, MessageType.Request<Response> {}

	interface Response extends StashMessage, MessageType.Response {}

	interface Claim extends StashMessage {}

}
