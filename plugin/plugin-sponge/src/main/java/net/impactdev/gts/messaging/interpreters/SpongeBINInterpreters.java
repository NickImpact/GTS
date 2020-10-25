package net.impactdev.gts.messaging.interpreters;

import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.purchase.BINPurchaseMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal.BINRemoveMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;

public class SpongeBINInterpreters implements Interpreter {

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
                BINRemoveMessage.Response.TYPE, BINRemoveMessage.Response::decode
        );
        plugin.getMessagingService().registerDecoder(
                BINPurchaseMessage.Request.TYPE, BINPurchaseMessage.Request::decode
        );
        plugin.getMessagingService().registerDecoder(
                BINPurchaseMessage.Response.TYPE, BINPurchaseMessage.Response::decode
        );
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        final IncomingMessageConsumer consumer = plugin.getMessagingService().getMessenger().getMessageConsumer();

        consumer.registerInternalConsumer(
                BINRemoveMessage.Request.class, request -> {
                    GTSPlugin.getInstance().getStorage()
                            .processListingRemoveRequest(request)
                            .thenAccept(response -> plugin.getMessagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        consumer.registerInternalConsumer(
                BINRemoveMessage.Response.class, response -> {
                    consumer.processRequest(response.getRequestID(), response);
                }
        );

        // Purchase Requests/Responses
        consumer.registerInternalConsumer(
                BINPurchaseMessage.Request.class, request -> {
                    GTSPlugin.getInstance().getStorage()
                            .processPurchase(request)
                            .thenAccept(response -> plugin.getMessagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        consumer.registerInternalConsumer(
                BINPurchaseMessage.Response.class, response -> {
                    consumer.processRequest(response.getRequestID(), response);
                }
        );
    }

}
