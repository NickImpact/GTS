package net.impactdev.gts.messaging.interpreters;

import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.deliveries.ClaimDeliveryImpl;
import net.impactdev.gts.common.plugin.GTSPlugin;

public class SpongeDeliveryInterpreters implements Interpreter {

    @Override
    public void register(GTSPlugin plugin) {
        this.getDecoders(plugin);
        this.getInterpreters(plugin);
    }

    @Override
    public void getDecoders(GTSPlugin plugin) {
        plugin.messagingService().registerDecoder(
                ClaimDeliveryImpl.ClaimDeliveryRequestImpl.TYPE, ClaimDeliveryImpl.ClaimDeliveryRequestImpl::decode
        );
        plugin.messagingService().registerDecoder(
                ClaimDeliveryImpl.ClaimDeliveryResponseImpl.TYPE, ClaimDeliveryImpl.ClaimDeliveryResponseImpl::decode
        );
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        final IncomingMessageConsumer consumer = plugin.messagingService().getMessenger().getMessageConsumer();

        consumer.registerInternalConsumer(
                ClaimDeliveryImpl.ClaimDeliveryRequestImpl.class, request -> {
                    GTSPlugin.instance().storage()
                            .claimDelivery(request)
                            .thenAccept(response -> plugin.messagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        consumer.registerInternalConsumer(
                ClaimDeliveryImpl.ClaimDeliveryResponseImpl.class, response -> {
                    consumer.processRequest(response.getRequestID(), response);
                }
        );
    }

}
