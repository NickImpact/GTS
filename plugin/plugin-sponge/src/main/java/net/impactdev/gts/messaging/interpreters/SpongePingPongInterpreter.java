package net.impactdev.gts.messaging.interpreters;

import net.impactdev.gts.common.messaging.interpreters.Interpreter;
import net.impactdev.gts.common.messaging.messages.utility.PingPongMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;

public class SpongePingPongInterpreter implements Interpreter {

	public void register(GTSPlugin plugin) {
		this.getDecoders(plugin);
		this.getInterpreters(plugin);
	}

	@Override
	public void getDecoders(GTSPlugin plugin) {
		plugin.getMessagingService().registerDecoder(PingPongMessage.Ping.TYPE, PingPongMessage.Ping::decode);
		plugin.getMessagingService().registerDecoder(PingPongMessage.Pong.TYPE, PingPongMessage.Pong::decode);
	}

	@Override
	public void getInterpreters(GTSPlugin plugin) {
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

		plugin.getMessagingService().getMessenger().getMessageConsumer().registerInternalConsumer(
				PingPongMessage.Pong.class, pong -> {
					GTSPlugin.getInstance().getPluginLogger().debug(String.format(
							"Received pong response (%s) for ping request (%s)",
							pong.getID(),
							pong.getRequestID()
					));
					GTSPlugin.getInstance().getMessagingService().getMessenger()
							.getMessageConsumer()
							.processRequest(pong.getRequestID(), pong);
				}
		);
	}

}
