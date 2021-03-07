package net.impactdev.gts.velocity.messaging.interpreters;

import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.utility.PingPongMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;

public class VelocityPingPongInterpreter implements Interpreter {
    @Override
    public void register(GTSPlugin plugin) {
        this.getDecoders(plugin);
        this.getInterpreters(plugin);
    }

    @Override
    public void getDecoders(GTSPlugin plugin) {
        plugin.getMessagingService().registerDecoder(PingPongMessage.Ping.TYPE, PingPongMessage.Ping::decode);
    }

    @Override
    public void getInterpreters(GTSPlugin plugin) {
        plugin.getMessagingService().getMessenger().getMessageConsumer().registerInternalConsumer(
                PingPongMessage.Ping.class, ping -> {
                    try {
                        ping.respond()
                                .thenAccept(pong -> {
                                    GTSPlugin.getInstance().getMessagingService().getMessenger().sendOutgoingMessage(pong);
                                })
                                .exceptionally(error -> {
                                    error.printStackTrace();
                                    return null;
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }
}
