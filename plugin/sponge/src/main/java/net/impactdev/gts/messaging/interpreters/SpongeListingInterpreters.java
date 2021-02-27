package net.impactdev.gts.messaging.interpreters;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.listings.ClaimMessageImpl;
import net.impactdev.gts.common.messaging.messages.listings.PublishListingMessageImpl;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.services.text.MessageService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

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
        plugin.getMessagingService().registerDecoder(
                PublishListingMessageImpl.TYPE, PublishListingMessageImpl::decode
        );
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
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
                            Listing working = listing.get();
                            for(Player player : Sponge.getServer().getOnlinePlayers()) {
                                if(!player.getUniqueId().equals(message.getActor())) {
                                    if(working instanceof Auction) {
                                        player.sendMessages(service.parse(
                                                Utilities.readMessageConfigOption(MsgConfigKeys.ADD_BROADCAST_AUCTION),
                                                Lists.newArrayList(() -> working, working::getLister)
                                        ));
                                    } else {
                                        player.sendMessages(service.parse(
                                                Utilities.readMessageConfigOption(MsgConfigKeys.ADD_BROADCAST_BIN),
                                                Lists.newArrayList(() -> working, working::getLister)
                                        ));
                                    }
                                }
                            }
                        }
                    });
                }
        );
    }

}
