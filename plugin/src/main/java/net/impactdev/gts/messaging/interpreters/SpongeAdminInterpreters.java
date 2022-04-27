package net.impactdev.gts.messaging.interpreters;

import net.impactdev.gts.api.deliveries.Delivery;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.admin.ForceDeleteMessageImpl;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.pricing.provided.MonetaryEntry;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.services.text.MessageService;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SpongeAdminInterpreters implements Interpreter {

    @Override
    public void register(GTSPlugin plugin) {
        this.getDecoders(plugin);
        this.getInterpreters(plugin);
    }

    @Override
    public void getDecoders(GTSPlugin plugin) {
        plugin.messagingService().registerDecoder(
                ForceDeleteMessageImpl.ForceDeleteRequest.TYPE, ForceDeleteMessageImpl.ForceDeleteRequest::decode
        );
        plugin.messagingService().registerDecoder(
                ForceDeleteMessageImpl.ForceDeleteResponse.TYPE, ForceDeleteMessageImpl.ForceDeleteResponse::decode
        );
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
        final IncomingMessageConsumer consumer = plugin.messagingService().getMessenger().getMessageConsumer();

        consumer.registerInternalConsumer(
                ForceDeleteMessageImpl.ForceDeleteRequest.class, request -> {
                    request.respond().thenAccept(response -> GTSPlugin.instance().messagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        consumer.registerInternalConsumer(
                ForceDeleteMessageImpl.ForceDeleteResponse.class, response -> {
                    consumer.processRequest(response.getRequestID(), response);

                    if(response.wasSuccessful()) {
                        Listing listing = response.getDeletedListing().orElse(null);
                        Optional<ServerPlayer> player = Sponge.server().player(listing.getLister());

                        if(response.shouldGive()) {
                            if(this.attemptReturn(listing)) {
                                player.ifPresent(source -> source.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_USER_RETURN))));
                            } else {
                                if(listing instanceof BuyItNow) {
                                    Listing stash = BuyItNow.builder()
                                            .id(listing.getID())
                                            .lister(listing.getLister())
                                            .entry(listing.getEntry())
                                            .price(((BuyItNow) listing).getPrice())
                                            .expiration(LocalDateTime.now())
                                            .build();
                                    GTSPlugin.instance().storage().publishListing(stash);
                                } else {
                                    Auction auction = (Auction) listing;
                                    Delivery delivery = Delivery.builder()
                                            .source(response.getActor())
                                            .recipient(auction.getLister())
                                            .content(auction.getEntry())
                                            .build();
                                    GTSPlugin.instance().storage().sendDelivery(delivery);

                                    for(Map.Entry<UUID, Auction.Bid> bid : auction.getUniqueBiddersWithHighestBids().entrySet()) {
                                        Delivery bidder = Delivery.builder()
                                                .source(response.getActor())
                                                .recipient(bid.getKey())
                                                .content(new MonetaryEntry(bid.getValue().getAmount()))
                                                .build();
                                        GTSPlugin.instance().storage().sendDelivery(bidder);
                                    }
                                }
                                player.ifPresent(source -> source.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_USER_RETURN_STASH))));
                            }
                        } else {
                            player.ifPresent(source -> source.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_USER))));
                        }
                    }
                }
        );
    }

    private boolean attemptReturn(Listing listing) {
        UUID target = listing.getLister();
        return listing.getEntry().give(target);
    }

}
