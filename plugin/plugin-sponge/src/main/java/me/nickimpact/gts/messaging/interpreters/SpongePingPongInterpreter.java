package me.nickimpact.gts.messaging.interpreters;

import me.nickimpact.gts.common.messaging.interpreters.Interpreter;
import me.nickimpact.gts.common.messaging.messages.utility.GTSPongMessage;
import me.nickimpact.gts.common.plugin.GTSPlugin;

public class SpongePingPongInterpreter implements Interpreter {

	public void register(GTSPlugin plugin) {
		this.getDecoders(plugin);
	}

	@Override
	public void getDecoders(GTSPlugin plugin) {
		plugin.getMessagingService().registerDecoder(GTSPongMessage.TYPE, GTSPongMessage::decode);
	}

	@Override
	public void getInterpreters(GTSPlugin plugin) {
		plugin.getMessagingService().getMessenger().getMessageConsumer().registerInternalConsumer(
				GTSPongMessage.class, pong -> {
					GTSPlugin.getInstance().getPluginLogger().debug(String.format(
							"Received pong response (%s) for ping request (%s)",
							pong.getID(),
							pong.getRequestID()
					));
					GTSPlugin.getInstance().getMessagingService().getMessenger().getMessageConsumer()
							.processRequest(pong.getRequestID(), pong);
				}
		);
	}

}
