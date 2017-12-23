package com.nickimpact.gts.storage;

import com.google.common.collect.ImmutableList;
import lombok.Getter;

import java.util.List;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public enum StorageType {

	//JSON("JSON", "json", "flatfile"),
	//YAML("YAML", "yaml", "yml"),
	//HOCON("HOCON", "hocon"),
	//MONGODB("MongoDB", "mongodb"),
	//MARIADB("MariaDB", "mariadb"),
	MYSQL("MySQL", "mysql"),
	//POSTGRESQL("PostgreSQL", "postgresql"),
	//SQLITE("SQLite", "sqlite"),
	H2("H2", "h2");

	@Getter
	private final String name;

	@Getter
	private final List<String> identifiers;

	StorageType(String name, String... identifiers) {
		this.name = name;
		this.identifiers = ImmutableList.copyOf(identifiers);
	}

	public static StorageType parse(String name) {
		for (StorageType t : values()) {
			for (String id : t.getIdentifiers()) {
				if (id.equalsIgnoreCase(name)) {
					return t;
				}
			}
		}
		return null;
	}

}
