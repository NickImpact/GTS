package net.impactdev.gts.bungee.messaging.interpreters;

import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionBidMessage;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionCancelMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;

public class BungeeAuctionInterpreter implements Interpreter {

    @Override
    public void register(GTSPlugin plugin) {
        this.getDecoders(plugin);
        this.getInterpreters(plugin);
    }

    @Override
    public void getDecoders(GTSPlugin plugin) {
        plugin.messagingService().registerDecoder(
                AuctionBidMessage.Request.TYPE, AuctionBidMessage.Request::decode
        );
        plugin.messagingService().registerDecoder(
                AuctionCancelMessage.Request.TYPE, AuctionCancelMessage.Request::decode
        );
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        IncomingMessageConsumer consumer = plugin.messagingService().getMessenger().getMessageConsumer();

        consumer.registerInternalConsumer(
                AuctionBidMessage.Request.class, request -> {
                    request.respond()
                            .thenAccept(response -> plugin.messagingService().getMessenger().sendOutgoingMessage(response));
                }
        );

        consumer.registerInternalConsumer(
                AuctionCancelMessage.Request.class, request -> {
                    request.respond()
                            .thenAccept(response -> plugin.messagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
    }

}
