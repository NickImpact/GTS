package net.impactdev.gts.messaging.interpreters;

import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.api.player.PlayerSettingsManager;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.purchase.BINPurchaseMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal.BINRemoveMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.services.text.MessageService;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;

import java.util.UUID;

public class SpongeBINInterpreters implements Interpreter {

    @Override
    public void register(GTSPlugin plugin) {
        this.getDecoders(plugin);
        this.getInterpreters(plugin);
    }

    @Override
    public void getDecoders(GTSPlugin plugin) {
        plugin.messagingService().registerDecoder(
                BINRemoveMessage.Request.TYPE, BINRemoveMessage.Request::decode
        );
        plugin.messagingService().registerDecoder(
                BINRemoveMessage.Response.TYPE, BINRemoveMessage.Response::decode
        );
        plugin.messagingService().registerDecoder(
                BINPurchaseMessage.Request.TYPE, BINPurchaseMessage.Request::decode
        );
        plugin.messagingService().registerDecoder(
                BINPurchaseMessage.Response.TYPE, BINPurchaseMessage.Response::decode
        );
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
        final IncomingMessageConsumer consumer = plugin.messagingService().getMessenger().getMessageConsumer();

        consumer.registerInternalConsumer(
                BINRemoveMessage.Request.class, request -> {
                    GTSPlugin.instance().storage()
                            .processListingRemoveRequest(request)
                            .thenAccept(response -> plugin.messagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        consumer.registerInternalConsumer(
                BINRemoveMessage.Response.class, response -> {
                    consumer.processRequest(response.getRequestID(), response);
                }
        );

        // Purchase Requests/Responses
        consumer.registerInternalConsumer(
                BINPurchaseMessage.Request.class, request -> {
                    GTSPlugin.instance().storage()
                            .processPurchase(request)
                            .thenAccept(response -> plugin.messagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        consumer.registerInternalConsumer(
                BINPurchaseMessage.Response.class, response -> {
                    consumer.processRequest(response.getRequestID(), response);

                    if(response.wasSuccessful()) {
                        GTSPlugin.instance().storage().getListing(response.getListingID()).thenAccept(listing -> {
                            listing.ifPresent(info -> {
                                Sponge.server().player(response.getSeller()).ifPresent(player -> {
                                    PlayerSettingsManager manager = GTSService.getInstance().getPlayerSettingsManager();
                                    manager.retrieve(response.getSeller()).thenAccept(settings -> {
                                        if(settings.getSoldListenState()) {
                                            PlaceholderSources sources = PlaceholderSources.builder()
                                                    .append(Listing.class, () -> info)
                                                    .append(UUID.class, response::getActor)
                                                    .build();

                                            service.parse(
                                                    Utilities.readMessageConfigOption(MsgConfigKeys.PURCHASE_RECEIVE),
                                                    sources
                                            ).forEach(player::sendMessage);
                                        }
                                    });
                                });
                            });
                        });
                    }
                }
        );
    }

}
