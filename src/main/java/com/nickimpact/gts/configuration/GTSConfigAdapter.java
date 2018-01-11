package com.nickimpact.gts.configuration;

import com.google.common.base.Splitter;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.configuration.ConfigAdapter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@RequiredArgsConstructor
public class GTSConfigAdapter implements ConfigAdapter {

	@Getter
	private final GTS plugin;

	private ConfigurationNode root;

	private Path makeFile(String resource) throws IOException {
		File cfg = plugin.getConfigDir().resolve(resource).toFile();
		//noinspection ResultOfMethodCallIgnored
		cfg.getParentFile().mkdirs();

		if (!cfg.exists()) {
			try (InputStream is = plugin.getClass().getClassLoader().getResourceAsStream(resource)) {
				Files.copy(is, cfg.toPath());
			}
		}

		return cfg.toPath();
	}

	@Override
	public void init(String resource) {
		try {
			ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
					.setPath(makeFile(resource))
					.build();

			root = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ConfigurationNode resolvePath(String path) {
		Iterable<String> paths = Splitter.on('.').split(path);
		ConfigurationNode node = root;

		if (node == null) {
			throw new RuntimeException("Config is not loaded.");
		}

		for (String s : paths) {
			node = node.getNode(s);

			if (node == null) {
				return SimpleConfigurationNode.root();
			}
		}

		return node;
	}

	@Override
	public boolean contains(String path) {
		return !resolvePath(path).isVirtual();
	}

	@Override
	public String getString(String path, String def) {
		return resolvePath(path).getString(def);
	}

	@Override
	public int getInt(String path, int def) {
		return resolvePath(path).getInt(def);
	}

	@Override
	public double getDouble(String path, double def) {
		return resolvePath(path).getDouble(def);
	}

	@Override
	public boolean getBoolean(String path, boolean def) {
		return resolvePath(path).getBoolean(def);
	}

	@Override
	public List<String> getList(String path, List<String> def) {
		ConfigurationNode node = resolvePath(path);
		if (node.isVirtual()) {
			return def;
		}

		return node.getList(Object::toString);
	}

	@Override
	public List<String> getObjectList(String path, List<String> def) {
		ConfigurationNode node = resolvePath(path);
		if (node.isVirtual()) {
			return def;
		}

		return node.getChildrenMap().keySet().stream().map(Object::toString).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> getMap(String path, Map<String, String> def) {
		ConfigurationNode node = resolvePath(path);
		if (node.isVirtual()) {
			return def;
		}

		Map<String, Object> m = (Map<String, Object>) node.getValue(Collections.emptyMap());
		return m.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().toString()));
	}
}
