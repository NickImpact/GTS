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

package net.impactdev.gts.common.storage;

import com.google.common.collect.Lists;
import net.impactdev.gts.common.storage.implementation.sql.SqlImplementation;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.dependencies.DependencyManager;
import net.impactdev.impactor.api.storage.StorageType;
import net.impactdev.impactor.api.storage.sql.file.H2ConnectionFactory;
import net.impactdev.impactor.api.storage.sql.hikari.MariaDBConnectionFactory;
import net.impactdev.impactor.api.storage.sql.hikari.MySQLConnectionFactory;
import net.impactdev.gts.common.config.updated.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.storage.implementation.StorageImplementation;
import net.impactdev.gts.common.storage.implementation.file.ConfigurateStorage;
import net.impactdev.gts.common.storage.implementation.file.loaders.HoconLoader;
import net.impactdev.gts.common.storage.implementation.file.loaders.JsonLoader;
import net.impactdev.gts.common.storage.implementation.file.loaders.YamlLoader;

import java.io.File;

public class StorageFactory {

    private final GTSPlugin plugin;

    public StorageFactory(GTSPlugin plugin) {
        this.plugin = plugin;
    }

    public GTSStorageImpl getInstance(StorageType defaultMethod) {
        GTSStorageImpl storage;
        StorageType type = this.plugin.getConfiguration().get(ConfigKeys.STORAGE_METHOD);
        if(type == null) {
            type = defaultMethod;
        }

        this.plugin.getPluginLogger().info("Loading storage provider... [" + type.getName() + "]");
        Impactor.getInstance().getRegistry().get(DependencyManager.class).loadStorageDependencies(Lists.newArrayList(type));
        storage = this.makeInstance(type);
        storage.init();
        return storage;
    }

    private GTSStorageImpl makeInstance(StorageType type) {
        return new GTSStorageImpl(this.plugin, this.createNewImplementation(type));
    }

    private StorageImplementation createNewImplementation(StorageType type) {
        switch(type) {
            case MARIADB:
                return new SqlImplementation(
                        this.plugin,
                        new MariaDBConnectionFactory(this.plugin.getConfiguration().get(ConfigKeys.STORAGE_CREDENTIALS)),
                        this.plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
                );
            case MYSQL:
                return new SqlImplementation(
                        this.plugin,
                        new MySQLConnectionFactory(this.plugin.getConfiguration().get(ConfigKeys.STORAGE_CREDENTIALS)),
                        this.plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
                );
            case H2:
                return new SqlImplementation(
                        this.plugin,
                        new H2ConnectionFactory(new File("gts").toPath().resolve("gts-h2")),
                        this.plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
                );
            case YAML:
            default:
                return new ConfigurateStorage(
                        this.plugin,
                        "YAML",
                        new YamlLoader(),
                        ".yml",
                        "yaml-storage"
                );
            case JSON:
                return new ConfigurateStorage(
                        this.plugin,
                        "JSON",
                        new JsonLoader(),
                        ".json",
                        "json-storage"
                );
            case HOCON:
                return new ConfigurateStorage(
                        this.plugin,
                        "HOCON",
                        new HoconLoader(),
                        ".hocon",
                        "hocon-storage"
                );
        }
    }
}
