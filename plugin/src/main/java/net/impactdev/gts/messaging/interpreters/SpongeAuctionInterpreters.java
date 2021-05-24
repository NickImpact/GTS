package net.impactdev.gts.messaging.interpreters;

import com.google.common.collect.Lists;
import net.impactdev.gts.GTSSpongePlugin;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
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
import net.impactdev.impactor.api.services.text.MessageService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

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
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
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
                    if(response.wasSuccessful()) {
                        GTSPlugin.getInstance().getStorage().getListing(response.getAuctionID()).thenAccept(listing -> {
                            PlayerSettingsManager manager = GTSService.getInstance().getPlayerSettingsManager();
                            listing.map(l -> l.as(Auction.class)).ifPresent(info -> {
                                info.getUniqueBiddersWithHighestBids().forEach((user, bid) -> {
                                    if(!response.getActor().equals(user)) {
                                        Sponge.getServer().getPlayer(user).ifPresent(player -> {
                                            manager.retrieve(user).thenAccept(settings -> {
                                                if (settings.getOutbidListenState()) {
                                                    double difference = response.getAmountBid() - bid.getAmount();
                                                    ConfigKey<String> key = response.wasSniped() ? MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_OUTBIDSNIPED : MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_OUTBID;
                                                    player.sendMessages(service.parse(Utilities.readMessageConfigOption(key),
                                                            Lists.newArrayList(() -> info, response::getActor, () -> difference, () -> GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.AUCTIONS_SET_TIME))
                                                    ));
                                                }
                                            });
                                        });
                                    }
                                });

                                Sponge.getServer().getPlayer(info.getLister()).ifPresent(player -> {
                                    manager.retrieve(info.getLister()).thenAccept(settings -> {
                                        if(settings.getBidListenState()) {
                                            ConfigKey<String> key = response.wasSniped() ? MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_NEWBIDSNIPED : MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_NEWBID;
                                            player.sendMessage(service.parse(
                                                    Utilities.readMessageConfigOption(key),
                                                    Lists.newArrayList(() -> info, response::getActor, () -> new Auction.BidContext(response.getActor(), new Auction.Bid(response.getAmountBid())), () -> GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.AUCTIONS_SET_TIME))
                                            ));

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
                            .thenAccept(response -> plugin.getMessagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        consumer.registerInternalConsumer(
                AuctionCancelMessage.Response.class, response -> {
                    consumer.processRequest(response.getRequestID(), response);

                    if(response.wasSuccessful()) {
                        List<Supplier<Object>> sources = Lists.newArrayList();
                        sources.add(response::getData);

                        EconomyService economy = GTSPlugin.getInstance().as(GTSSpongePlugin.class).getEconomy();

                        for(UUID bidder : response.getBidders()) {
                            Sponge.getServer().getPlayer(bidder).ifPresent(player -> {
                                player.sendMessage(service.parse(
                                        Utilities.readMessageConfigOption(MsgConfigKeys.GENERAL_FEEDBACK_AUCTIONS_CANCELLED),
                                        sources
                                ));

                                economy.getOrCreateAccount(bidder).ifPresent(account -> {
                                    account.deposit(
                                            economy.getDefaultCurrency(),
                                            BigDecimal.valueOf(response.getData().getCurrentBid(bidder).map(Auction.Bid::getAmount).orElseThrow(() -> new IllegalStateException("Unable to find highest bid for player marked as a bidder"))),
                                            Cause.builder()
                                                    .append(bidder)
                                                    .build(EventContext.builder()
                                                            .add(EventContextKeys.PLUGIN, GTSPlugin.getInstance().as(GTSSpongePlugin.class).getPluginContainer())
                                                            .build()
                                                    )
                                    );
                                });
                            });
                        }
                    }
                }
        );
    }

}
