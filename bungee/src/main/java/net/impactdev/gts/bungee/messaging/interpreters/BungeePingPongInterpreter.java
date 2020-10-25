package net.impactdev.gts.bungee.messaging.interpreters;

import net.impactdev.gts.api.messaging.message.type.utility.PingMessage;
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
						plugin.getPluginLogger().info("[Messaging] Translating ping...");
						ping.respond()
								.thenAccept(pong -> {
									GTSPlugin.getInstance().getPluginLogger().info("[Messaging] Pong response info:");
									GTSPlugin.getInstance().getPluginLogger().info("[Messaging]   ID = " + pong.getID());
									GTSPlugin.getInstance().getPluginLogger().info("[Messaging]   Request = " + pong.getRequestID());
									GTSPlugin.getInstance().getMessagingService().getMessenger().sendOutgoingMessage(pong);
									plugin.getPluginLogger().info("[Messaging] Pong response published!");
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
