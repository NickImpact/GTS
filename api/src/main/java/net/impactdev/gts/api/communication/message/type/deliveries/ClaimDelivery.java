package net.impactdev.gts.api.communication.message.type.deliveries;

import net.impactdev.gts.api.communication.message.OutgoingMessage;
import net.impactdev.gts.api.communication.message.type.MessageType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public interface ClaimDelivery extends OutgoingMessage {

    @NonNull UUID getDeliveryID();

    @NonNull UUID getActor();

    interface Request extends ClaimDelivery, MessageType.Request<Response> {}

    interface Response extends ClaimDelivery, MessageType.Response {}

}
