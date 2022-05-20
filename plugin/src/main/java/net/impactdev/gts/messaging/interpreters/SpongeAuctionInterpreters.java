package net.impactdev.gts.messaging.interpreters;

import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.communication.IncomingMessageConsumer;
import net.impactdev.gts.api.player.PlayerSettingsManager;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionBidMessage;
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionCancelMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.utilities.Time;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.EconomyService;

import java.math.BigDecimal;
import java.util.UUID;

public class SpongeAuctionInterpreters implements Interpreter {

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
                AuctionBidMessage.Response.TYPE, AuctionBidMessage.Response::decode
        );
        plugin.messagingService().registerDecoder(
                AuctionCancelMessage.Request.TYPE, AuctionCancelMessage.Request::decode
        );
        plugin.messagingService().registerDecoder(
                AuctionCancelMessage.Response.TYPE, AuctionCancelMessage.Response::decode
        );
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
        final IncomingMessageConsumer consumer = plugin.messagingService().getMessenger().getMessageConsumer();

        consumer.registerInternalConsumer(
                AuctionBidMessage.Request.class, request -> {
                    request.respond()
                            .thenAccept(response -> plugin.messagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        consumer.registerInternalConsumer(
                AuctionBidMessage.Response.class, response -> {
                    consumer.processRequest(response.getRequestID(), response);
                    if(response.wasSuccessful()) {
                        GTSPlugin.instance().storage().getListing(response.getAuctionID()).thenAccept(listing -> {
                            PlayerSettingsManager manager = GTSService.getInstance().getPlayerSettingsManager();
                            listing.map(l -> l.as(Auction.class)).ifPresent(info -> {
                                info.getUniqueBiddersWithHighestBids().forEach((user, bid) -> {
                                    if(!response.getActor().equals(user)) {
                                        Sponge.server().player(user).ifPresent(player -> {
                                            manager.retrieve(user).thenAccept(settings -> {
                                                if (settings.getOutbidListenState()) {
                                                    double difference = response.getAmountBid() - bid.getAmount();
                                                    ConfigKey<String> key = response.wasSniped() ? MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_OUTBIDSNIPED : MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_OUTBID;

                                                    PlaceholderSources sources = PlaceholderSources.builder()
                                                            .append(Auction.class, () -> info)
                                                            .append(UUID.class, response::getActor)
                                                            .append(Double.class, () -> difference)
                                                            .append(Time.class, () -> GTSPlugin.instance().configuration().main().get(ConfigKeys.AUCTIONS_SET_TIME))
                                                            .build();

                                                    player.sendMessage(service.parse(Utilities.readMessageConfigOption(key), sources));
                                                }
                                            });
                                        });
                                    }
                                });

                                Sponge.server().player(info.getLister()).ifPresent(player -> {
                                    manager.retrieve(info.getLister()).thenAccept(settings -> {
                                        if(settings.getBidListenState()) {
                                            ConfigKey<String> key = response.wasSniped() ? MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_NEWBIDSNIPED : MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_NEWBID;

                                            PlaceholderSources sources = PlaceholderSources.builder()
                                                    .append(Auction.class, () -> info)
                                                    .append(UUID.class, response::getActor)
                                                    .append(Auction.BidContext.class, () -> new Auction.BidContext(response.getActor(), new Auction.Bid(response.getAmountBid())))
                                                    .append(Time.class, () -> GTSPlugin.instance().configuration().main().get(ConfigKeys.AUCTIONS_SET_TIME))
                                                    .build();

                                            player.sendMessage(service.parse(Utilities.readMessageConfigOption(key), sources));

                                        }
                                    });
                                });
                            });
                        });
                    }
                }
        );

        consumer.registerInternalConsumer(
                AuctionCancelMessage.Request.class, request -> {
                    request.respond()
                            .thenAccept(response -> plugin.messagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        consumer.registerInternalConsumer(
                AuctionCancelMessage.Response.class, response -> {
                    consumer.processRequest(response.getRequestID(), response);

                    if(response.wasSuccessful()) {
                        PlaceholderSources sources = PlaceholderSources.builder()
                                .append(Auction.class, response::getData)
                                .build();

                        EconomyService economy = Sponge.server().serviceProvider().economyService().orElseThrow(IllegalStateException::new);

                        for(UUID bidder : response.getBidders()) {
                            Sponge.server().player(bidder).ifPresent(player -> {
                                player.sendMessage(service.parse(
                                        Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_CANCELLED),
                                        sources
                                ));

                                economy.findOrCreateAccount(bidder).ifPresent(account -> {
                                    account.deposit(
                                            economy.defaultCurrency(),
                                            BigDecimal.valueOf(response.getData().getCurrentBid(bidder).map(Auction.Bid::getAmount).orElseThrow(() -> new IllegalStateException("Unable to find highest bid for player marked as a bidder")))
                                    );
                                });
                            });
                        }
                    }
                }
        );
    }

}
