package net.impactdev.gts.velocity.messaging.interpreters;

import net.impactdev.gts.api.messaging.IncomingMessageConsumer;
import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.admin.ForceDeleteMessageImpl;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;

public class VelocityAdminInterpreters implements Interpreter {
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
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        final IncomingMessageConsumer consumer = plugin.getMessagingService().getMessenger().getMessageConsumer();

        consumer.registerInternalConsumer(
                ForceDeleteMessageImpl.ForceDeleteRequest.class, request -> {
                    request.respond().thenAccept(response -> {
                        GTSPlugin.getInstance().getMessagingService().getMessenger().sendOutgoingMessage(response);
                        GTSPlugin.getInstance().getPluginLogger().info("Response sent");
                    })
                            .exceptionally(e -> {
                                ExceptionWriter.write(e);
                                return null;
                            });
                }
        );
    }
}
