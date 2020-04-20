package me.nickimpact.gts.messaging.interpreters;

import me.nickimpact.gts.common.messaging.messages.utility.GTSPongMessage;
import me.nickimpact.gts.common.plugin.GTSPlugin;

public class SpongePingPongInterpreter {

	public static void registerDecoders(GTSPlugin plugin) {
		plugin.getMessagingService().registerDecoder(GTSPongMessage.TYPE, GTSPongMessage::decode);
	}

	public static void registerInterpreters(GTSPlugin plugin) {
		plugin.getMessagingService().getMessenger().getMessageConsumer().registerInternalConsumer(
				GTSPongMessage.class, pong -> {
					// Only process pong when a request is available. Servers that don't make the ping request
					// don't care for this functionality
					if(plugin.getMessagingService()
							.getMessenger()
							.getMessageConsumer()
							.locateAndFilterRequestIfPresent(pong.getRequestID())) {
						plugin.getPluginLogger().noTag("&eGTS &7(&aMessaging&7) &fReceived pong response");
					}
				}
		);
	}

}
