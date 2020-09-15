package me.nickimpact.gts.messaging.interpreters;

import me.nickimpact.gts.common.messaging.interpreters.Interpreter;
import me.nickimpact.gts.common.messaging.messages.listings.buyitnow.removal.BuyItNowRemoveResponseMessage;
import me.nickimpact.gts.common.plugin.GTSPlugin;

public class SpongeBINInterpreters implements Interpreter {

    @Override
    public void register(GTSPlugin plugin) {
        this.getDecoders(plugin);
        this.getInterpreters(plugin);
    }

    @Override
    public void getDecoders(GTSPlugin plugin) {
        plugin.getMessagingService().registerDecoder(
                BuyItNowRemoveResponseMessage.TYPE, BuyItNowRemoveResponseMessage::decode
        );
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        plugin.getMessagingService().getMessenger().getMessageConsumer().registerInternalConsumer(
                BuyItNowRemoveResponseMessage.class, response -> {
                    plugin.getMessagingService().getMessenger().getMessageConsumer()
                            .processRequest(response.getRequestID(), response);
                }
        );
    }

}
