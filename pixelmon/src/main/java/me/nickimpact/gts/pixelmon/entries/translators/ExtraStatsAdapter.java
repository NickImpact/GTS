package me.nickimpact.gts.pixelmon.entries.translators;

import com.google.gson.*;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.ExtraStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.LakeTrioStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.MewStats;
import me.nickimpact.gts.GTS;

import java.lang.reflect.Type;

public class ExtraStatsAdapter implements JsonSerializer<ExtraStats>, JsonDeserializer<ExtraStats> {

	@Override
	public ExtraStats deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
		if(((JsonObject) json).get("numCloned") != null) {
			return GTS.prettyGson.fromJson(json, MewStats.class);
		} else {
			return GTS.prettyGson.fromJson(json, LakeTrioStats.class);
		}
	}

	@Override
	public JsonElement serialize(ExtraStats src, Type type, JsonSerializationContext ctx) {
		return ctx.serialize(src, type);
	}
}
