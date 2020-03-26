package me.nickimpact.gts.common.messaging.messages;

import com.google.gson.JsonElement;
import lombok.Getter;
import me.nickimpact.gts.api.messaging.message.type.UpdateMessage;
import me.nickimpact.gts.common.messaging.GTSMessagingService;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * This message is intended to be sent to servers only when a user successfully interacts with a listing. These
 * interactions include removing a listing and purchasing a listing, namely. As servers will need to be able to
 * receive updates every so often, this message is meant to be informational for any connected servers, informing
 * them it's time to update their cache.
 */
@Getter
public class ListingsUpdateMessage extends AbstractMessage implements UpdateMessage {

	public static final String TYPE = "listing";

//	public static ListingsUpdateMessage decode(@Nullable JsonElement content, UUID id) {
//		if(content == null) {
//			throw new IllegalStateException("Missing content");
//		}
//
//		JsonElement listing = content.getAsJsonObject().get("listing");
//		if(listing == null) {
//			throw new IllegalStateException("Incoming message has no listing argument: " + content);
//		}
//		JsonElement action = content.getAsJsonObject().get("action");
//		if(action == null) {
//			throw new IllegalStateException("Incoming message has no action argument: " + content);
//		}
//
//		return new ListingsUpdateMessage(id, UUID.fromString(listing.getAsString()), Action.valueOf(action.getAsString()));
//	}
//
//	private Action action;
//
//	public ListingsUpdateMessage(UUID id, UUID listing, Action action) {
//		super(id, listing);
//		this.action = action;
//	}
//
//	@Override
//	public @NonNull String asEncodedString() {
//		return GTSMessagingService.encodeMessageAsString(
//				TYPE,
//				getID(),
//				new JObject()
//						.add("listing", this.getContent().toString())
//						.add("action", this.action.name())
//						.toJson()
//		);
//	}

	public static ListingsUpdateMessage decode(@Nullable JsonElement content, UUID id) {
		return new ListingsUpdateMessage(id);
	}

	public ListingsUpdateMessage(UUID id) {
		super(id);
	}

	@Override
	public @NonNull String asEncodedString() {
		return GTSMessagingService.encodeMessageAsString(TYPE, getID(), null);
	}

}
