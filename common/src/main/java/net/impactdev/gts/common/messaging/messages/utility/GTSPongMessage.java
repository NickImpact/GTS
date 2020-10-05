package net.impactdev.gts.common.messaging.messages.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.gts.common.messaging.GTSMessagingService;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.type.utility.PingMessage;
import net.impactdev.gts.common.messaging.messages.AbstractMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("Duplicates")
public class GTSPongMessage extends AbstractMessage.Response implements PingMessage.Pong {

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

	GTSPongMessage(UUID id, UUID request) {
		super(id, request);
	}

	@Override
	public boolean wasSuccessful() {
		return true;
	}

	@Override
	public Optional<ErrorCode> getErrorCode() {
		return Optional.empty();
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
