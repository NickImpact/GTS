package net.impactdev.gts.messaging.interpreters;

import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionBidMessage;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionCancelMessage;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionClaimMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;

public class SpongeAuctionInterpreters implements Interpreter {

    @Override
    public void register(GTSPlugin plugin) {
        this.getDecoders(plugin);
        this.getInterpreters(plugin);
    }

    @Override
    public void getDecoders(GTSPlugin plugin) {
        plugin.getMessagingService().registerDecoder(
                AuctionBidMessage.Request.TYPE, AuctionBidMessage.Request::decode
        );
        plugin.getMessagingService().registerDecoder(
                AuctionBidMessage.Response.TYPE, AuctionBidMessage.Response::decode
        );
        plugin.getMessagingService().registerDecoder(
                AuctionCancelMessage.Request.TYPE, AuctionCancelMessage.Request::decode
        );
        plugin.getMessagingService().registerDecoder(
                AuctionCancelMessage.Response.TYPE, AuctionCancelMessage.Response::decode
        );
        plugin.getMessagingService().registerDecoder(
                AuctionClaimMessage.ClaimRequest.TYPE, AuctionClaimMessage.ClaimRequest::decode
        );
        plugin.getMessagingService().registerDecoder(
                AuctionClaimMessage.ClaimResponse.TYPE, AuctionClaimMessage.ClaimResponse::decode
        );
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        final IncomingMessageConsumer consumer = plugin.getMessagingService().getMessenger().getMessageConsumer();

        consumer.registerInternalConsumer(
                AuctionBidMessage.Request.class, request -> {
                    request.respond()
                            .thenAccept(response -> plugin.getMessagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        consumer.registerInternalConsumer(
                AuctionBidMessage.Response.class, response -> {
                    consumer.processRequest(response.getRequestID(), response);
                }
        );

        consumer.registerInternalConsumer(
                AuctionClaimMessage.ClaimRequest.class, request -> {
                    request.respond()
                            .thenAccept(response -> plugin.getMessagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        consumer.registerInternalConsumer(
                AuctionClaimMessage.ClaimResponse.class, response -> {
                    consumer.processRequest(response.getRequestID(), response);
                }
        );

        consumer.registerInternalConsumer(
                AuctionCancelMessage.Request.class, request -> {
                    request.respond()
                            .thenAccept(response -> plugin.getMessagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        consumer.registerInternalConsumer(
                AuctionCancelMessage.Response.class, response -> {
                    consumer.processRequest(response.getRequestID(), response);
                }
        );
    }

}
