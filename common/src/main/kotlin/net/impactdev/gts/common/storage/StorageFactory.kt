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
package net.impactdev.gts.common.storage

import com.google.common.collect.Lists
import net.impactdev.gts.common.config.ConfigKeys
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.gts.common.storage.implementation.StorageImplementation
import net.impactdev.gts.common.storage.implementation.file.ConfigurateStorage
import net.impactdev.gts.common.storage.implementation.file.loaders.HoconLoader
import net.impactdev.gts.common.storage.implementation.file.loaders.JsonLoader
import net.impactdev.gts.common.storage.implementation.file.loaders.YamlLoader
import net.impactdev.gts.common.storage.implementation.sql.SqlImplementation
import net.impactdev.impactor.api.Impactor
import net.impactdev.impactor.api.dependencies.DependencyManager
import net.impactdev.impactor.api.storage.StorageType
import net.impactdev.impactor.api.storage.sql.file.H2ConnectionFactory
import net.impactdev.impactor.api.storage.sql.hikari.MariaDBConnectionFactory
import net.impactdev.impactor.api.storage.sql.hikari.MySQLConnectionFactory
import java.io.File

class StorageFactory(private val plugin: GTSPlugin) {
    fun getInstance(defaultMethod: StorageType?): GTSStorageImpl {
        val storage: GTSStorageImpl
        var type = plugin.configuration.get(ConfigKeys.STORAGE_METHOD)
        if (type == null) {
            type = defaultMethod
        }
        plugin.pluginLogger.info("Loading storage provider... [" + type!!.getName() + "]")
        Impactor.getInstance().registry.get(DependencyManager::class.java)
            .loadStorageDependencies(Lists.newArrayList(type))
        storage = makeInstance(type)
        storage.init()
        return storage
    }

    private fun makeInstance(type: StorageType?): GTSStorageImpl {
        return GTSStorageImpl(plugin, createNewImplementation(type))
    }

    private fun createNewImplementation(type: StorageType?): StorageImplementation {
        return when (type) {
            StorageType.MARIADB -> SqlImplementation(
                plugin,
                MariaDBConnectionFactory(plugin.configuration.get(ConfigKeys.STORAGE_CREDENTIALS)),
                plugin.configuration.get(ConfigKeys.SQL_TABLE_PREFIX)
            )
            StorageType.MYSQL -> SqlImplementation(
                plugin,
                MySQLConnectionFactory(plugin.configuration.get(ConfigKeys.STORAGE_CREDENTIALS)),
                plugin.configuration.get(ConfigKeys.SQL_TABLE_PREFIX)
            )
            StorageType.H2 -> SqlImplementation(
                plugin,
                H2ConnectionFactory(File("gts").toPath().resolve("gts-h2")),
                plugin.configuration.get(ConfigKeys.SQL_TABLE_PREFIX)
            )
            StorageType.YAML -> ConfigurateStorage(
                plugin,
                "YAML",
                YamlLoader(),
                ".yml",
                "yaml"
            )
            StorageType.JSON -> ConfigurateStorage(
                plugin,
                "JSON",
                JsonLoader(),
                ".json",
                "json"
            )
            StorageType.HOCON -> ConfigurateStorage(
                plugin,
                "HOCON",
                HoconLoader(),
                ".hocon",
                "hocon"
            )
            else -> ConfigurateStorage(
                plugin,
                "YAML",
                YamlLoader(),
                ".yml",
                "yaml"
            )
        }
    }
}