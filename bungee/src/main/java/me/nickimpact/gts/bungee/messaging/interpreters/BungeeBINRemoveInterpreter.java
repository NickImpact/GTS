package me.nickimpact.gts.bungee.messaging.interpreters;

import me.nickimpact.gts.api.messaging.message.type.listings.BuyItNowMessage;
import me.nickimpact.gts.common.messaging.interpreters.Interpreter;
import me.nickimpact.gts.common.messaging.messages.listings.buyitnow.removal.BuyItNowRemoveRequestMessage;
import me.nickimpact.gts.common.plugin.GTSPlugin;

public class BungeeBINRemoveInterpreter implements Interpreter {

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
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        plugin.getMessagingService().getMessenger().getMessageConsumer().registerInternalConsumer(
                BuyItNowRemoveRequestMessage.class, request -> {
                    GTSPlugin.getInstance().getStorage()
                            .processListingRemoveRequest(request)
                            .thenAccept(response -> plugin.getMessagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
    }
}
