package net.impactdev.gts.common.messaging.messages.utility;

import com.google.gson.JsonElement;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.gts.api.messaging.message.type.utility.PingMessage;
import net.impactdev.gts.common.messaging.messages.AbstractMessage;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.future.CompletableFutureManager;
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
				this.getID(),
				-1
		), Impactor.getInstance().getScheduler().async());
	}

	@Override
	public void print(PrettyPrinter printer) {

	}
}
