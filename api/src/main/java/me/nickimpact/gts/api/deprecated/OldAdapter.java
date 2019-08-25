package me.nickimpact.gts.api.deprecated;

import com.google.gson.*;
import com.nickimpact.impactor.api.json.Registry;

import java.lang.reflect.Type;

@Deprecated
public abstract class OldAdapter<E> implements JsonSerializer<E>, JsonDeserializer<E> {

	@Override
	@SuppressWarnings({"unchecked", "ConstantConditions"})
	public E deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
		JsonObject obj = (JsonObject)json;
		return (E) getGson().fromJson(obj, getRegistry().get(obj.get("id").getAsString().toLowerCase()));
	}

	@Override
	public JsonElement serialize(E src, Type type, JsonSerializationContext ctx) {
		return ctx.serialize(src);
	}

	public abstract Gson getGson();

	public abstract Registry<E> getRegistry();
}
