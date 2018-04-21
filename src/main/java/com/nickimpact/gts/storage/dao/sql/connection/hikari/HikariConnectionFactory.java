package com.nickimpact.gts.storage.dao.sql.connection.hikari;

import com.nickimpact.gts.storage.StorageCredentials;
import com.nickimpact.gts.storage.dao.sql.connection.AbstractConnectionFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public abstract class HikariConnectionFactory extends AbstractConnectionFactory {

	protected final StorageCredentials configuration;

	private HikariDataSource hikari;

	public HikariConnectionFactory(String name, StorageCredentials configuration) {
		super(name);
		this.configuration = configuration;
	}

	protected String getDriverClass() {
		return null;
	}

	protected void appendProperties(HikariConfig config, StorageCredentials credentials) {}

	protected void appendConfigurationInfo(HikariConfig config) {
		String address = configuration.getAddress();
		String[] addressSplit = address.split(":");
		address = addressSplit[0];
		String port = addressSplit.length > 1 ? addressSplit[1] : "3306";

		config.setDataSourceClassName(getDriverClass());
		config.addDataSourceProperty("serverName", address);
		config.addDataSourceProperty("port", port);
		config.addDataSourceProperty("databasename", configuration.getDatabase());
		config.setUsername(configuration.getUsername());
		config.setPassword(configuration.getPassword());
	}

	@Override
	public void init() {
		HikariConfig config = new HikariConfig();
		config.setPoolName("gts");

		appendConfigurationInfo(config);
		appendProperties(config, configuration);

		config.setMaximumPoolSize(10);
		config.setMinimumIdle(10);
		config.setMaxLifetime(1800000);
		config.setConnectionTimeout(5000);

		config.setLeakDetectionThreshold(TimeUnit.SECONDS.toMillis(10));

		config.setConnectionTestQuery("/* GTS ping */ SELECT 1");

		config.setInitializationFailTimeout(1);

		hikari = new HikariDataSource(config);
	}

	@Override
	public void shutdown() throws Exception {
		if(hikari != null)
			hikari.close();
	}

	@Override
	public Connection getConnection() throws SQLException {
		Connection connection = hikari.getConnection();
		if(connection == null)
			throw new SQLException("Unable to get a connection from the pool");

		return connection;
	}
}
