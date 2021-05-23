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
        plugin.getMessagingService().registerDecoder(
                BINRemoveMessage.Request.TYPE, BINRemoveMessage.Request::decode
        );
        plugin.getMessagingService().registerDecoder(
                BINPurchaseMessage.Request.TYPE, BINPurchaseMessage.Request::decode
        );
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        plugin.getMessagingService().getMessenger().getMessageConsumer().registerInternalConsumer(
                BINRemoveMessage.Request.class, request -> {
                    GTSPlugin.getInstance().getStorage()
                            .processListingRemoveRequest(request)
                            .thenAccept(response -> plugin.getMessagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        plugin.getMessagingService().getMessenger().getMessageConsumer().registerInternalConsumer(
                BINPurchaseMessage.Request.class, request -> {
                    GTSPlugin.getInstance().getStorage()
                            .processPurchase(request)
                            .thenAccept(response -> plugin.getMessagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
    }
}