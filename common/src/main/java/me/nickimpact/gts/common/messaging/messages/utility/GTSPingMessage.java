package me.nickimpact.gts.common.messaging.messages.utility;

import com.google.gson.JsonElement;
import com.nickimpact.impactor.api.json.factory.JObject;
import me.nickimpact.gts.api.messaging.message.type.utility.PingMessage;
import me.nickimpact.gts.common.messaging.GTSMessagingService;
import me.nickimpact.gts.common.messaging.messages.AbstractMessage;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.common.utils.CompletableFutureManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("Duplicates")
public class GTSPingMessage extends AbstractMessage implements PingMessage.Ping {

	public static final String TYPE = "PING";

	public static GTSPingMessage decode(@Nullable JsonElement content, UUID id) {
		return new GTSPingMessage(id);
	}

	public GTSPingMessage(UUID id) {
		super(id);
	}

	@Override
	public @NonNull String asEncodedString() {
		return GTSMessagingService.encodeMessageAsString(
				TYPE,
				this.getID(),
				new JObject().toJson()
		);
	}

	@Override
	public CompletableFuture<Pong> respond() {
		return CompletableFutureManager.makeFuture(() -> new GTSPongMessage(
				GTSPlugin.getInstance().getMessagingService().generatePingID(),
				this.getID()
		));
	}

}
