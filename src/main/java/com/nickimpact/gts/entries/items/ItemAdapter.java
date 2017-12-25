package com.nickimpact.gts.entries.items;

import com.google.gson.*;
import com.nickimpact.gts.GTS;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class ItemAdapter implements JsonSerializer<DataContainer>, JsonDeserializer<DataContainer> {

	@Override
	public DataContainer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		
		String data = GTS.prettyGson.fromJson(json, String.class);
		try {
			return DataFormats.JSON.read(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public JsonElement serialize(DataContainer src, Type typeOfSrc, JsonSerializationContext ctx) {
		try {
			String data = DataFormats.JSON.write(src);
			return ctx.serialize(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
