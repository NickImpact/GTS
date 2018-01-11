package com.nickimpact.gts.api.configuration;

import com.nickimpact.gts.GTS;

import java.util.List;
import java.util.Map;

/**
 * The ConfigAdapter interface represents the forefront for deserialization of a key. A key will
 * implement a specified type, and use the adapter here to receive its desired value.
 *
 * @author NickImpact
 */
public interface ConfigAdapter {

	GTS getPlugin();

	void init(String resource);

	boolean contains(String path);

	String getString(String path, String def);

	int getInt(String path, int def);

	double getDouble(String path, double def);

	boolean getBoolean(String path, boolean def);

	List<String> getList(String path, List<String> def);

	List<String> getObjectList(String path, List<String> def);

	Map<String, String> getMap(String path, Map<String, String> def);
}
