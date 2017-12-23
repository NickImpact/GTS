package com.nickimpact.gts.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Getter
@AllArgsConstructor
public class StorageCredentials {

	private final String address;

	private final String database;

	private final String username;

	private final String password;
}
