package com.nickimpact.gts.storage.dao.file;

import com.nickimpact.gts.GTS;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class JsonDao extends ConfigurateDao {

	public JsonDao(GTS plugin) {
		super(plugin, "JSON", ".json");
	}

	@Override
	protected ConfigurationLoader<? extends ConfigurationNode> loader(Path path) {
		return GsonConfigurationLoader.builder()
				.setIndent(2)
				.setSource(() -> Files.newBufferedReader(path, StandardCharsets.UTF_8))
				.setSink(() -> Files.newBufferedWriter(path, StandardCharsets.UTF_8))
				.build();
	}
}
