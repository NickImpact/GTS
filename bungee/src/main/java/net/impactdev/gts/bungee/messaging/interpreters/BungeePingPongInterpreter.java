package net.impactdev.gts.bungee.messaging.interpreters;

import net.impactdev.gts.common.messaging.messages.utility.PingPongMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;

public class BungeePingPongInterpreter {

	public static void registerDecoders(GTSPlugin plugin) {
		plugin.getMessagingService().registerDecoder(PingPongMessage.Ping.TYPE, PingPongMessage.Ping::decode);
	}

	public static void registerInterpreters(GTSPlugin plugin) {
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
