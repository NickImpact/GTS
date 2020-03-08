package me.nickimpact.gts.json;

import com.google.gson.*;
import me.nickimpact.gts.GTS;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.util.ResettableBuilder;

import java.lang.reflect.Type;

public class DataContainerAdapter implements JsonSerializer<DataContainer>, JsonDeserializer<DataContainer> {

	@Override
	public DataContainer deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
		String data = GTS.getInstance().getGson().fromJson(json, String.class);
		try {
			return DataFormats.JSON.read(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public JsonElement serialize(DataContainer src, Type type, JsonSerializationContext ctx) {
		try {
			String data = DataFormats.JSON.write(src);
			return ctx.serialize(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
