package net.impactdev.gts.listings.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.impactdev.gts.common.plugin.GTSPlugin;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;

import java.lang.reflect.Type;

public class DataContainerAdapter implements JsonSerializer<DataContainer>, JsonDeserializer<DataContainer> {

	@Override
	public DataContainer deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
		try {
			return DataFormats.JSON.get().read(GTSPlugin.instance().gson().fromJson(json, String.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public JsonElement serialize(DataContainer src, Type type, JsonSerializationContext ctx) {
		try {
			String data = DataFormats.JSON.get().write(src);
			return ctx.serialize(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
