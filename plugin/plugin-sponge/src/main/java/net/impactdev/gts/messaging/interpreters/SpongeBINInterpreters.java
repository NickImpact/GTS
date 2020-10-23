package net.impactdev.gts.messaging.interpreters;

import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage;
import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.purchase.BINPurchaseRequestMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.purchase.BINPurchaseResponseMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal.BuyItNowRemoveRequestMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal.BuyItNowRemoveResponseMessage;
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
                BuyItNowRemoveRequestMessage.TYPE, BuyItNowRemoveRequestMessage::decode
        );
        plugin.getMessagingService().registerDecoder(
                BuyItNowRemoveResponseMessage.TYPE, BuyItNowRemoveResponseMessage::decode
        );
        plugin.getMessagingService().registerDecoder(
                BINPurchaseRequestMessage.TYPE, BINPurchaseRequestMessage::decode
        );
        plugin.getMessagingService().registerDecoder(
                BINPurchaseResponseMessage.TYPE, BINPurchaseResponseMessage::decode
        );
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        final IncomingMessageConsumer consumer = plugin.getMessagingService().getMessenger().getMessageConsumer();

        consumer.registerInternalConsumer(
                BuyItNowRemoveRequestMessage.class, request -> {
                    GTSPlugin.getInstance().getStorage()
                            .processListingRemoveRequest(request)
                            .thenAccept(response -> plugin.getMessagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        consumer.registerInternalConsumer(
                BuyItNowRemoveResponseMessage.class, response -> {
                    consumer.processRequest(response.getRequestID(), response);
                }
        );

        // Purchase Requests/Responses
        consumer.registerInternalConsumer(
                BINPurchaseRequestMessage.class, request -> {
                    GTSPlugin.getInstance().getStorage()
                            .processPurchase(request)
                            .thenAccept(response -> plugin.getMessagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        consumer.registerInternalConsumer(
                BINPurchaseResponseMessage.class, response -> {
                    consumer.processRequest(response.getRequestID(), response);
                }
        );
    }

}
