package com.nickimpact.gts.storage.dao.sql.connection.file;

import com.nickimpact.gts.storage.dao.sql.connection.AbstractConnectionFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.concurrent.locks.ReentrantLock;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
abstract class FlatfileConnectionFactory extends AbstractConnectionFactory {

	protected static final DecimalFormat DF = new DecimalFormat("#.##");

	protected final File file;
	private final ReentrantLock lock = new ReentrantLock();
	private Connection connection;

	FlatfileConnectionFactory(String name, File file) {
		super(name);
		this.file = file;
	}

	protected abstract String getDriverClass();
	protected abstract String getDriverID();

	@Override
	public void init() {

	}

	@Override
	public void shutdown() throws Exception {
		if(connection != null && !connection.isClosed())
			connection.close();
	}

	@Override
	public Connection getConnection() throws SQLException {
		lock.lock();
		try {
			if(this.connection == null || this.connection.isClosed()) {
				try {
					Class.forName(getDriverClass());
				} catch (ClassNotFoundException ignored) {}

				Connection connection = DriverManager.getConnection(getDriverID() + ":" + file.getAbsolutePath());
				if(connection != null) {
					this.connection = new NonClosableConnection(connection);
				}
			}
		} finally {
			lock.unlock();
		}

		if(this.connection == null)
			throw new SQLException("Unable to get a connection");

		return this.connection;
	}
}
