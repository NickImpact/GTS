package com.nickimpact.gts.storage.dao.sql.connection.file;

import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@AllArgsConstructor
public final class NonClosableConnection implements Connection {

	@Delegate(excludes = Exclude.class)
	private Connection delegate;

	@Override
	public void close() throws SQLException {}

	private interface Exclude {
		void close() throws SQLException;
	}
}
