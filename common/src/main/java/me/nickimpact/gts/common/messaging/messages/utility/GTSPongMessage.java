package me.nickimpact.gts.common.messaging.messages.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.nickimpact.gts.api.messaging.message.type.utility.PingMessage;
import me.nickimpact.gts.common.messaging.GTSMessagingService;
import me.nickimpact.gts.common.messaging.messages.AbstractMessage;
import me.nickimpact.gts.api.util.gson.JObject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("Duplicates")
public class GTSPongMessage extends AbstractMessage implements PingMessage.Pong {

	public static final String TYPE = "PONG";

	public static GTSPongMessage decode(@Nullable JsonElement content, UUID id) {
		if(content == null) {
			throw new IllegalStateException("Raw JSON data was null");
		}

		JsonObject raw = content.getAsJsonObject();

		UUID requestID = Optional.ofNullable(raw.get("request"))
				.map(x -> UUID.fromString(x.getAsString()))
				.orElseThrow(() -> new IllegalStateException("Unable to locate or parse request ID"));

		return new GTSPongMessage(id, requestID);
	}

	private UUID request;

	GTSPongMessage(UUID id, UUID request) {
		super(id);
		this.request = request;
	}

	@Override
	public UUID getRequestID() {
		return this.request;
	}

	@Override
	public @NonNull String asEncodedString() {
		return GTSMessagingService.encodeMessageAsString(
				TYPE,
				this.getID(),
				new JObject()
						.add("request", this.getRequestID().toString())
						.toJson()
		);
	}

}
