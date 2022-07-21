package net.impactdev.gts.bungee.messaging.interpreters;

import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.admin.ForceDeleteMessageImpl;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;

public class BungeeAdminInterpreters implements Interpreter {

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
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        final IncomingMessageConsumer consumer = plugin.messagingService().getMessenger().getMessageConsumer();

        consumer.registerInternalConsumer(
                ForceDeleteMessageImpl.ForceDeleteRequest.class, request -> {
                    request.respond().thenAccept(response -> {
                        GTSPlugin.instance().messagingService().getMessenger().sendOutgoingMessage(response);
                        GTSPlugin.instance().logger().info("Response sent");
                    })
                    .exceptionally(e -> {
                        ExceptionWriter.write(e);
                        return null;
                    });
                }
        );
    }
}
