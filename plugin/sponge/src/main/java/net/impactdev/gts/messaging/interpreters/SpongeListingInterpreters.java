package net.impactdev.gts.messaging.interpreters;

import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.listings.ClaimMessageImpl;
import net.impactdev.gts.common.messaging.messages.listings.PublishListingMessageImpl;
import net.impactdev.gts.common.plugin.GTSPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

public class SpongeListingInterpreters implements Interpreter {

    @Override
    public void register(GTSPlugin plugin) {
        this.getDecoders(plugin);
        this.getInterpreters(plugin);
    }

    @Override
    public void getDecoders(GTSPlugin plugin) {
        plugin.getMessagingService().registerDecoder(
                ClaimMessageImpl.ClaimRequestImpl.TYPE, ClaimMessageImpl.ClaimRequestImpl::decode
        );
        plugin.getMessagingService().registerDecoder(
                ClaimMessageImpl.ClaimResponseImpl.TYPE, ClaimMessageImpl.ClaimResponseImpl::decode
        );
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        IncomingMessageConsumer consumer = plugin.getMessagingService().getMessenger().getMessageConsumer();

        consumer.registerInternalConsumer(
                ClaimMessageImpl.ClaimRequestImpl.class, request -> {
                    request.respond().thenAccept(response -> plugin.getMessagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        consumer.registerInternalConsumer(
                ClaimMessageImpl.ClaimResponseImpl.class, response -> {
                    consumer.processRequest(response.getRequestID(), response);
                }
        );
        consumer.registerInternalConsumer(
                ClaimMessageImpl.ClaimResponseImpl.AuctionClaimResponseImpl.class, response -> {
                    consumer.processRequest(response.getRequestID(), response);
                }
        );

        consumer.registerInternalConsumer(
                PublishListingMessageImpl.class, message -> {
                    GTSPlugin.getInstance().getStorage().getListing(message.getListingID()).thenAccept(listing -> {
                        if(listing.isPresent()) {
                            for(Player player : Sponge.getServer().getOnlinePlayers()) {
                                if(!player.getUniqueId().equals(message.getActor())) {

                                }
                            }
                        }
                    });
                }
        );
    }

}
