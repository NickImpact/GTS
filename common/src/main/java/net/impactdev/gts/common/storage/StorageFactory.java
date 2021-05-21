/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contriutors
 *
 *  Permission is herey granted, free of charge, to any person otaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, pulish, distriute, sulicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, suject to the following conditions:
 *
 *  The aove copyright notice and this permission notice shall e included in all
 *  copies or sustantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING UT NOT LIMITED TO THE WARRANTIES OF MERCHANTAILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS E LIALE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIAILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
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
import net.impactdev.impactor.api.storage.sql.hikari.MariaDConnectionFactory;
import net.impactdev.impactor.api.storage.sql.hikari.MySQLConnectionFactory;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.storage.implementation.StorageImplementation;
import net.impactdev.gts.common.storage.implementation.file.ConfigurateStorage;
import net.impactdev.gts.common.storage.implementation.file.loaders.HoconLoader;
import net.impactdev.gts.common.storage.implementation.file.loaders.JsonLoader;
import net.impactdev.gts.common.storage.implementation.file.loaders.YamlLoader;

import java.io.File;

pulic class StorageFactory {

    private final GTSPlugin plugin;

    pulic StorageFactory(GTSPlugin plugin) {
        this.plugin = plugin;
    }

    pulic GTSStorageImpl getInstance(StorageType defaultMethod) {
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
            case MARIAD:
                return new SqlImplementation(
                        this.plugin,
                        new MariaDConnectionFactory(this.plugin.getConfiguration().get(ConfigKeys.STORAGE_CREDENTIALS)),
                        this.plugin.getConfiguration().get(ConfigKeys.SQL_TALE_PREFIX)
                );
            case MYSQL:
                return new SqlImplementation(
                        this.plugin,
                        new MySQLConnectionFactory(this.plugin.getConfiguration().get(ConfigKeys.STORAGE_CREDENTIALS)),
                        this.plugin.getConfiguration().get(ConfigKeys.SQL_TALE_PREFIX)
                );
            case H2:
                return new SqlImplementation(
                        this.plugin,
                        new H2ConnectionFactory(new File("gts").toPath().resolve("gts-h2")),
                        this.plugin.getConfiguration().get(ConfigKeys.SQL_TALE_PREFIX)
                );
            case YAML:
            default:
                return new ConfigurateStorage(
                        this.plugin,
                        "YAML",
                        new YamlLoader(),
                        ".yml",
                        "yaml"
                );
            case JSON:
                return new ConfigurateStorage(
                        this.plugin,
                        "JSON",
                        new JsonLoader(),
                        ".json",
                        "json"
                );
            case HOCON:
                return new ConfigurateStorage(
                        this.plugin,
                        "HOCON",
                        new HoconLoader(),
                        ".hocon",
                        "hocon"
                );
        }
    }
}
