package com.nickimpact.gts.storage.dao.sql.connection.hikari;

import com.nickimpact.gts.storage.StorageCredentials;
import com.zaxxer.hikari.HikariConfig;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class MySqlConnectionFactory extends HikariConnectionFactory {

	public MySqlConnectionFactory(StorageCredentials configuration) {
		super("MySQL", configuration);
	}

	@Override
	protected void appendConfigurationInfo(HikariConfig config) {
		String address = configuration.getAddress();
		String[] addressSplit = address.split(":");
		address = addressSplit[0];
		String port = addressSplit.length > 1 ? addressSplit[1] : "3306";
		String database = configuration.getDatabase();

		config.setJdbcUrl("jdbc:mysql://" + address + ":" + port + "/" + database);
		config.setUsername(configuration.getUsername());
		config.setPassword(configuration.getPassword());
	}

	@Override
	protected void appendProperties(HikariConfig config, StorageCredentials credentials) {
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("alwaysSendSetIsolation", "false");
		config.addDataSourceProperty("cacheServerConfiguration", "true");
		config.addDataSourceProperty("elideSetAutoCommits", "true");
		config.addDataSourceProperty("userLocalSessionState", "true");

		config.addDataSourceProperty("userServerPrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		config.addDataSourceProperty("cacheCallableStmts", "true");

		super.appendProperties(config, credentials);

	}
}
