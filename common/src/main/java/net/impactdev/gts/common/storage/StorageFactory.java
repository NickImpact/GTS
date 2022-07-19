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
import net.impactdev.impactor.api.storage.connection.configurate.ConfigurateLoaders;
import net.impactdev.impactor.api.storage.connection.sql.SQLConnections;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.storage.implementation.StorageImplementation;
import net.impactdev.gts.common.storage.implementation.file.ConfigurateStorage;

import java.nio.file.Paths;

public class StorageFactory {

    private final GTSPlugin plugin;

    public StorageFactory(GTSPlugin plugin) {
        this.plugin = plugin;
    }

    public GTSStorageImpl getInstance(StorageType defaultMethod) {
        GTSStorageImpl storage;
        StorageType type = this.plugin.configuration().main().get(ConfigKeys.STORAGE_METHOD);
        if(type == null) {
            type = defaultMethod;
        }

        this.plugin.logger().info("Loading storage provider... [" + type.getName() + "]");
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
                        SQLConnections.mariaDB(this.plugin.configuration().main().get(ConfigKeys.STORAGE_CREDENTIALS)),
                        this.plugin.configuration().main().get(ConfigKeys.SQL_TABLE_PREFIX)
                );
            case MYSQL:
                return new SqlImplementation(
                        this.plugin,
                        SQLConnections.mysql(this.plugin.configuration().main().get(ConfigKeys.STORAGE_CREDENTIALS)),
                        this.plugin.configuration().main().get(ConfigKeys.SQL_TABLE_PREFIX)
                );
            case H2:
                return new SqlImplementation(
                        this.plugin,
                        SQLConnections.h2(Paths.get("gts").resolve("gts-h2")),
                        this.plugin.configuration().main().get(ConfigKeys.SQL_TABLE_PREFIX)
                );
            case YAML:
            default:
                return new ConfigurateStorage(
                        this.plugin,
                        ConfigurateLoaders.yaml(),
                        "yaml"
                );
            case JSON:
                return new ConfigurateStorage(
                        this.plugin,
                        ConfigurateLoaders.json(),
                        "json"
                );
            case HOCON:
                return new ConfigurateStorage(
                        this.plugin,
                        ConfigurateLoaders.hocon(),
                        "hocon"
                );
        }
    }
}
