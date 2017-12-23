package com.nickimpact.gts.storage.dao.sql.connection.file;

import java.io.File;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class SQLiteConnectionFactory extends FlatfileConnectionFactory {

	public SQLiteConnectionFactory(File file) {
		super("SQLite", file);
	}

	@Override
	protected String getDriverClass() {
		return "org.sqlite.JDBC";
	}

	@Override
	protected String getDriverID() {
		return "jdbc:sqlite";
	}
}
