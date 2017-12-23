package com.nickimpact.gts.utils;

import com.nickimpact.gts.GTS;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;

import java.util.HashMap;
import java.util.Map;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class GsonUtils {

	public static String serialize(NBTTagCompound nbt)
	{
		Map<String, Object> map = nbtToMap(nbt);
		return GTS.prettyGson.toJson(map);
	}

	@SuppressWarnings("unchecked")
	public static NBTTagCompound deserialize(String json)
	{
		Map<String, Object> map = GTS.prettyGson.fromJson(json, Map.class);
		return nbtFromMap(map);
	}

	public static Map<String, Object> nbtToMap(NBTTagCompound nbt)
	{
		Map<String, Object> map = new HashMap<>();

		for (String key : nbt.getKeySet())
		{
			try
			{
				NBTBase base = nbt.getTag(key);

				if (base instanceof NBTTagString)
					map.put(key, ((NBTTagString)base).getString());
				else if (base instanceof NBTPrimitive)
					map.put(key, ((NBTPrimitive)base).getDouble());
				else if (base instanceof NBTTagCompound)
					map.put(key, nbtToMap((NBTTagCompound)base));
			}
			catch (Exception exc) { ; }
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	public static NBTTagCompound nbtFromMap(Map<String, Object> map)
	{
		NBTTagCompound nbt = new NBTTagCompound();

		for (String key : map.keySet())
		{
			try
			{
				if (map.get(key) instanceof String)
					nbt.setString(key, (String)map.get(key));
				else if (map.get(key) instanceof Map)
					nbt.setTag(key, nbtFromMap((Map<String, Object>)map.get(key)));
				else
					nbt.setDouble(key, (Double)map.get(key));
			}
			catch (Exception exc) { ; }
		}

		return nbt;
	}


}
