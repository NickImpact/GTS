package net.impactdev.gts.api.messaging.message.type.deliveries;

import net.impactdev.gts.api.messaging.message.OutgoingMessage;
import net.impactdev.gts.api.messaging.message.type.MessageType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public interface ClaimDelivery extends OutgoingMessage {

    @NonNull UUID getDeliveryID();

    @NonNull UUID getActor();

    interface Request extends ClaimDelivery, MessageType.Request<Response> {}

    interface Response extends ClaimDelivery, MessageType.Response {}

}
