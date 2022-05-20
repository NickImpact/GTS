package net.impactdev.gts.messaging.interpreters;

import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.communication.IncomingMessageConsumer;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.listings.ClaimMessageImpl;
import net.impactdev.gts.common.messaging.messages.listings.PublishListingMessageImpl;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.services.text.MessageService;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.UUID;

public class SpongeListingInterpreters implements Interpreter {

    @Override
    public void register(GTSPlugin plugin) {
        this.getDecoders(plugin);
        this.getInterpreters(plugin);
    }

    @Override
    public void getDecoders(GTSPlugin plugin) {
        plugin.messagingService().registerDecoder(
                ClaimMessageImpl.ClaimRequestImpl.TYPE, ClaimMessageImpl.ClaimRequestImpl::decode
        );
        plugin.messagingService().registerDecoder(
                ClaimMessageImpl.ClaimResponseImpl.TYPE, ClaimMessageImpl.ClaimResponseImpl::decode
        );
        plugin.messagingService().registerDecoder(
                PublishListingMessageImpl.TYPE, PublishListingMessageImpl::decode
        );
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
        IncomingMessageConsumer consumer = plugin.messagingService().getMessenger().getMessageConsumer();

        consumer.registerInternalConsumer(
                ClaimMessageImpl.ClaimRequestImpl.class, request -> {
                    request.respond().thenAccept(response -> plugin.messagingService().getMessenger().sendOutgoingMessage(response));
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
                    if(!GTSPlugin.instance().configuration().main().get(ConfigKeys.USE_MULTI_SERVER)) {
                        return;
                    }

                    GTSPlugin.instance().storage().getListing(message.getListingID()).thenAccept(listing -> {
                        if(listing.isPresent()) {
                            Listing working = listing.get();
                            for(ServerPlayer player : Sponge.server().onlinePlayers()) {
                                if(!player.uniqueId().equals(message.getActor())) {
                                    PlaceholderSources sources = PlaceholderSources.builder()
                                            .append(Listing.class, () -> working)
                                            .append(UUID.class, working::getLister)
                                            .build();

                                    if(working instanceof Auction) {
                                        service.parse(
                                                Utilities.readMessageConfigOption(MsgConfigKeys.ADD_BROADCAST_AUCTION),
                                                sources
                                        ).forEach(player::sendMessage);
                                    } else {
                                        service.parse(
                                                Utilities.readMessageConfigOption(MsgConfigKeys.ADD_BROADCAST_BIN),
                                                sources
                                        ).forEach(player::sendMessage);
                                    }
                                }
                            }
                        }
                    });
                }
        );
    }

}
