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
		plugin.messagingService().registerDecoder(PingPongMessage.Ping.TYPE, PingPongMessage.Ping::decode);
		plugin.messagingService().registerDecoder(PingPongMessage.Pong.TYPE, PingPongMessage.Pong::decode);
	}

	@Override
	public void getInterpreters(GTSPlugin plugin) {
		plugin.messagingService().getMessenger().getMessageConsumer().registerInternalConsumer(
				PingPongMessage.Ping.class, ping -> {
					try {
						ping.respond()
								.thenAccept(pong -> {
									GTSPlugin.instance().messagingService().getMessenger().sendOutgoingMessage(pong);
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

		plugin.messagingService().getMessenger().getMessageConsumer().registerInternalConsumer(
				PingPongMessage.Pong.class, pong -> {
					GTSPlugin.instance().messagingService().getMessenger()
							.getMessageConsumer()
							.processRequest(pong.getRequestID(), pong);
				}
		);
	}

}
