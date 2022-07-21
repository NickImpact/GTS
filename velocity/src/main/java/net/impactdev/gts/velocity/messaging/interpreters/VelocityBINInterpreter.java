package net.impactdev.gts.velocity.messaging.interpreters;

import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.purchase.BINPurchaseMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal.BINRemoveMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;

public class VelocityBINInterpreter implements Interpreter {

    @Override
    public void register(GTSPlugin plugin) {
        this.getDecoders(plugin);
        this.getInterpreters(plugin);
    }

    @Override
    public void getDecoders(GTSPlugin plugin) {
        plugin.messagingService().registerDecoder(
                BINRemoveMessage.Request.TYPE, BINRemoveMessage.Request::decode
        );
        plugin.messagingService().registerDecoder(
                BINPurchaseMessage.Request.TYPE, BINPurchaseMessage.Request::decode
        );
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        plugin.messagingService().getMessenger().getMessageConsumer().registerInternalConsumer(
                BINRemoveMessage.Request.class, request -> {
                    GTSPlugin.instance().storage()
                            .processListingRemoveRequest(request)
                            .thenAccept(response -> plugin.messagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        plugin.messagingService().getMessenger().getMessageConsumer().registerInternalConsumer(
                BINPurchaseMessage.Request.class, request -> {
                    GTSPlugin.instance().storage()
                            .processPurchase(request)
                            .thenAccept(response -> plugin.messagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
    }
}