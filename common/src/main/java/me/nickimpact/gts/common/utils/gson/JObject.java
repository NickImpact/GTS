package me.nickimpact.gts.common.utils.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class JObject implements JElement<JsonObject> {

	private final JsonObject object = new JsonObject();

	@Override
	public JsonObject toJson() {
		return this.object;
	}

	public JObject add(String key, JsonElement value) {
		this.object.add(key, value);
		return this;
	}

	public JObject add(String key, String value) {
		if (value == null) {
			return add(key, JsonNull.INSTANCE);
		}
		return add(key, new JsonPrimitive(value));
	}

	public JObject add(String key, Number value) {
		if (value == null) {
			return add(key, JsonNull.INSTANCE);
		}
		return add(key, new JsonPrimitive(value));
	}

	public JObject add(String key, Boolean value) {
		if (value == null) {
			return add(key, JsonNull.INSTANCE);
		}
		return add(key, new JsonPrimitive(value));
	}

	public JObject add(String key, JElement value) {
		if (value == null) {
			return add(key, JsonNull.INSTANCE);
		}
		return add(key, value.toJson());
	}

	public JObject add(String key, Supplier<? extends JElement> value) {
		return add(key, value.get().toJson());
	}

	public JObject consume(Consumer<? super JObject> consumer) {
		consumer.accept(this);
		return this;
	}

}
