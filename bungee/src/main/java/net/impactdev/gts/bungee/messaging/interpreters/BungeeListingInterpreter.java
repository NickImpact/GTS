package net.impactdev.gts.bungee.messaging.interpreters;

import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage;
import net.impactdev.gts.api.messaging.message.type.listings.PublishListingMessage;
import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.listings.ClaimMessageImpl;
import net.impactdev.gts.common.messaging.messages.listings.PublishListingMessageImpl;
import net.impactdev.gts.common.plugin.GTSPlugin;

public class BungeeListingInterpreter implements Interpreter {

    @Override
    public void register(GTSPlugin plugin) {
        this.getDecoders(plugin);
        this.getInterpreters(plugin);
    }

    @Override
    public void getDecoders(GTSPlugin plugin) {
        plugin.messagingService().registerDecoder(
                PublishListingMessageImpl.TYPE, PublishListingMessageImpl::decode
        );
        plugin.messagingService().registerDecoder(
                ClaimMessageImpl.ClaimRequestImpl.TYPE, ClaimMessageImpl.ClaimRequestImpl::decode
        );
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        IncomingMessageConsumer consumer = plugin.messagingService().getMessenger().getMessageConsumer();

        consumer.registerInternalConsumer(
                PublishListingMessageImpl.class, message -> plugin.messagingService().getMessenger().sendOutgoingMessage(message)
        );
        consumer.registerInternalConsumer(
                ClaimMessageImpl.ClaimRequestImpl.class, request -> {
                    request.respond()
                            .thenAccept(response -> plugin.messagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
    }
}
