package net.impactdev.gts.messaging.interpreters;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.purchase.BINPurchaseMessage;
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal.BINRemoveMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.services.text.MessageService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

public class SpongeBINInterpreters implements Interpreter {

    @Override
    public void register(GTSPlugin plugin) {
        this.getDecoders(plugin);
        this.getInterpreters(plugin);
    }

    @Override
    public void getDecoders(GTSPlugin plugin) {
        plugin.getMessagingService().registerDecoder(
                BINRemoveMessage.Request.TYPE, BINRemoveMessage.Request::decode
        );
        plugin.getMessagingService().registerDecoder(
                BINRemoveMessage.Response.TYPE, BINRemoveMessage.Response::decode
        );
        plugin.getMessagingService().registerDecoder(
                BINPurchaseMessage.Request.TYPE, BINPurchaseMessage.Request::decode
        );
        plugin.getMessagingService().registerDecoder(
                BINPurchaseMessage.Response.TYPE, BINPurchaseMessage.Response::decode
        );
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        final IncomingMessageConsumer consumer = plugin.getMessagingService().getMessenger().getMessageConsumer();

        consumer.registerInternalConsumer(
                BINRemoveMessage.Request.class, request -> {
                    GTSPlugin.getInstance().getStorage()
                            .processListingRemoveRequest(request)
                            .thenAccept(response -> plugin.getMessagingService().getMessenger().sendOutgoingMessage(response));
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
                    GTSPlugin.getInstance().getStorage()
                            .processPurchase(request)
                            .thenAccept(response -> plugin.getMessagingService().getMessenger().sendOutgoingMessage(response));
                }
        );
        consumer.registerInternalConsumer(
                BINPurchaseMessage.Response.class, response -> {
                    consumer.processRequest(response.getRequestID(), response);

                    if(response.wasSuccessful()) {
                        GTSPlugin.getInstance().getStorage().getListing(response.getListingID()).thenAccept(listing -> {
                            listing.ifPresent(info -> {
                                Sponge.getServer().getPlayer(response.getSeller()).ifPresent(player -> {
                                    player.sendMessages(service.parse(
                                            Utilities.readMessageConfigOption(MsgConfigKeys.PURCHASE_RECEIVE),
                                            Lists.newArrayList(() -> info, response::getActor)
                                    ));
                                });
                            });
                        });
                    }
                }
        );
    }

}
