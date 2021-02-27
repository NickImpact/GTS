package net.impactdev.gts.messaging.interpreters;

import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.admin.ForceDeleteMessageImpl;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.services.text.MessageService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

import java.time.LocalDateTime;

public class SpongeAdminInterpreters implements Interpreter {

    @Override
    public void register(GTSPlugin plugin) {
        this.getDecoders(plugin);
        this.getInterpreters(plugin);
    }

    @Override
    public void getDecoders(GTSPlugin plugin) {
        plugin.getMessagingService().registerDecoder(
                ForceDeleteMessageImpl.ForceDeleteRequest.TYPE, ForceDeleteMessageImpl.ForceDeleteRequest::decode
        );
        plugin.getMessagingService().registerDecoder(
                ForceDeleteMessageImpl.ForceDeleteResponse.TYPE, ForceDeleteMessageImpl.ForceDeleteResponse::decode
        );
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        final IncomingMessageConsumer consumer = plugin.getMessagingService().getMessenger().getMessageConsumer();

        consumer.registerInternalConsumer(
                ForceDeleteMessageImpl.ForceDeleteRequest.class, request -> {
                    request.respond().thenAccept(response -> GTSPlugin.getInstance().getMessagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        consumer.registerInternalConsumer(
                ForceDeleteMessageImpl.ForceDeleteResponse.class, response -> {
                    consumer.processRequest(response.getRequestID(), response);

                    if(response.wasSuccessful()) {
                        Listing listing = response.getDeletedListing().orElse(null);
                        Sponge.getServer().getPlayer(listing.getLister())
                                .ifPresent(source -> {
                                    if(response.shouldGive()) {
                                        if(listing.getEntry().give(source.getUniqueId())) {
                                            source.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_USER_RETURN)));
                                        } else {
                                            if(listing instanceof BuyItNow) {
                                                Listing stash = BuyItNow.builder()
                                                        .id(listing.getID())
                                                        .lister(listing.getLister())
                                                        .entry(listing.getEntry())
                                                        .price(((BuyItNow) listing).getPrice())
                                                        .expiration(LocalDateTime.now())
                                                        .build();
                                                GTSPlugin.getInstance().getStorage().publishListing(stash);
                                            } else {
                                                Auction auction = (Auction) listing;
                                                Listing stash = Auction.builder()
                                                        .id(listing.getID())
                                                        .lister(listing.getLister())
                                                        .entry(listing.getEntry())
                                                        .expiration(LocalDateTime.now())
                                                        .bids(auction.getBids())
                                                        .increment(auction.getIncrement())
                                                        .start(auction.getStartingPrice())
                                                        .current(auction.getCurrentPrice())
                                                        .published(auction.getPublishTime())
                                                        .build();
                                                GTSPlugin.getInstance().getStorage().publishListing(stash);
                                            }
                                            source.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_USER_RETURN_STASH)));
                                        }
                                    } else {
                                        source.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_USER)));
                                    }
                                });
                    }
                }
        );
    }

}
