package com.nickimpact.gts.storage.dao.sql.connection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@RequiredArgsConstructor
public abstract class AbstractConnectionFactory {

	@Getter
	private final String name;

	public abstract void init();

	public abstract void shutdown() throws Exception;

	public abstract Connection getConnection() throws SQLException;
}
