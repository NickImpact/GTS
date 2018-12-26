package me.nickimpact.gts.generations.utils;

import com.google.common.collect.Lists;
import me.nickimpact.gts.GTS;
import net.minecraft.nbt.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class GsonUtils {

	public static String serialize(NBTTagCompound nbt) {
		Map<String, Object> map = nbtToMap(nbt);
		return GTS.prettyGson.toJson(map);
	}

	@SuppressWarnings("unchecked")
	public static NBTTagCompound deserialize(String json) {
		Map<String, Object> map = GTS.prettyGson.fromJson(json, Map.class);
		return nbtFromMap(map);
	}

	private static Map<String, Object> nbtToMap(NBTTagCompound nbt) {
		Map<String, Object> map = new HashMap<>();

		for (String key : nbt.getKeySet()) {
			try {
				NBTBase base = nbt.getTag(key);
				map.put(key, fetch(base));
			} catch (Exception ignored) {}
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> nbtToList(NBTTagList base) {
		List<Map<String, Object>> result = Lists.newArrayList();
		base.iterator().forEachRemaining(b -> result.add((Map<String, Object>) fetch(b)));
		return result;
	}

	private static Object fetch(NBTBase base) {
		if (base instanceof NBTTagString) {
			return ((NBTTagString) base).getString();
		} else if (base instanceof NBTPrimitive) {
			return ((NBTPrimitive) base).getDouble();
		} else if (base instanceof NBTTagCompound) {
			return nbtToMap((NBTTagCompound) base);
		} else if (base instanceof NBTTagList) {
			return nbtToList((NBTTagList) base);
		}

		return null;
	}

	private static NBTTagCompound nbtFromMap(Map<String, Object> map) {
		NBTTagCompound nbt = new NBTTagCompound();

		for (String key : map.keySet()) {
			try {
				apply(nbt, key, map.get(key));
			} catch (Exception ignored) {}
		}

		return nbt;
	}

	private static NBTTagList nbtFromList(List<Map<String, Object>> list) {
		NBTTagList nList = new NBTTagList();
		list.forEach(entry -> nList.appendTag(nbtFromMap(entry)));
		return nList;
	}

	@SuppressWarnings("unchecked")
	private static void apply(NBTTagCompound nbt, String key, Object obj) throws Exception {
		if (obj instanceof String)
			nbt.setString(key, (String) obj);
		else if (obj instanceof Map)
			nbt.setTag(key, nbtFromMap((Map<String, Object>) obj));
		else if (obj instanceof List)
			nbt.setTag(key, nbtFromList((List<Map<String, Object>>) obj));
		else
			nbt.setDouble(key, (Double) obj);
	}
}
