/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.nickimpact.gts.storage;

import me.nickimpact.gts.GTS;
import me.nickimpact.gts.GTSInfo;
import me.nickimpact.gts.configuration.ConfigKeys;
import me.nickimpact.gts.api.utils.MessageUtils;
import me.nickimpact.gts.storage.dao.AbstractDao;
import me.nickimpact.gts.storage.dao.sql.SqlDao;
import me.nickimpact.gts.storage.dao.sql.connection.file.H2ConnectionFactory;
import me.nickimpact.gts.storage.dao.sql.connection.hikari.MySqlConnectionFactory;
import org.spongepowered.api.text.Text;

import java.io.File;

public class StorageFactory {

	public static StorageType getStorageType(GTS gts, StorageType defaultType) {
		String type = gts.getConfig().get(ConfigKeys.STORAGE_METHOD);
		StorageType st = StorageType.parse(type);

		if(st == null) {
			MessageUtils.genAndSendErrorMessage(
					"Config Error",
					"Invalid Storage Type found",
					"Type Specified: " + type,
					"Defaulting to: " + defaultType.getName()
			);
			st = defaultType;
		}

		return st;
	}

	public static Storage getInstance(GTS gts, StorageType defaultMethod) {
		String method = gts.getConfig().get(ConfigKeys.STORAGE_METHOD);
		StorageType type = StorageType.parse(method);
		if (type == null) {
			type = defaultMethod;
		}

		final StorageType declared = type;

		gts.getConsole().ifPresent(console -> console.sendMessages(Text.of(GTSInfo.PREFIX, "Loading storage provider... [" + declared.getName() + "]")));
		Storage storage = makeInstance(declared, gts);
		storage.init();
		return storage;
	}

	private static Storage makeInstance(StorageType type, GTS gts) {
		return AbstractStorage.create(gts, makeDao(type, gts));
	}

	private static AbstractDao makeDao(StorageType type, GTS plugin) {
		switch(type) {
			case MYSQL:
				return new SqlDao(
						plugin,
						new MySqlConnectionFactory(plugin.getConfig().get(ConfigKeys.DATABASE_VALUES)),
						plugin.getConfig().get(ConfigKeys.SQL_TABLE_PREFIX)
				);
			case H2:
				return new SqlDao(
						plugin,
						new H2ConnectionFactory(new File(plugin.getDataDirectory(), "gts-h2")),
						plugin.getConfig().get(ConfigKeys.SQL_TABLE_PREFIX)
				);
			default:
				//return new JsonDao(plugin);
				return new SqlDao(
						plugin,
						new H2ConnectionFactory(new File(plugin.getDataDirectory(), "gts-h2")),
						plugin.getConfig().get(ConfigKeys.SQL_TABLE_PREFIX)
				);
		}
	}
}
