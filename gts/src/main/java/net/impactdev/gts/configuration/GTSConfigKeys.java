package net.impactdev.gts.configuration;

import com.google.common.collect.ImmutableMap;
import net.impactdev.impactor.api.configuration.key.ConfigKey;
import net.impactdev.impactor.api.storage.StorageCredentials;
import net.impactdev.impactor.api.storage.StorageType;
import net.impactdev.impactor.api.text.TextProcessor;

import java.util.Map;

import static net.impactdev.impactor.api.configuration.key.ConfigKeyFactory.booleanKey;
import static net.impactdev.impactor.api.configuration.key.ConfigKeyFactory.key;
import static net.impactdev.impactor.api.configuration.key.ConfigKeyFactory.notReloadable;
import static net.impactdev.impactor.api.configuration.key.ConfigKeyFactory.stringKey;

public class GTSConfigKeys {

    // Translation Processing
    public static final ConfigKey<Boolean> AUTO_INSTALL_TRANSLATIONS = booleanKey("translations.auto-install", true);
    public static final ConfigKey<TextProcessor> TEXT_PROCESSOR = key(adapter -> {
        String input = adapter.getString("translations.processor.type", "mini-message");
        switch (input) {
            case "mini-message":
                return TextProcessor.mini();
            case "legacy":
                return TextProcessor.legacy(adapter.getString("translations.processor.legacy-key", "&").charAt(0));
            default:
                throw new IllegalArgumentException("Invalid text processor specified!");
        }
    });

    // Storage
    public static final ConfigKey<StorageType> STORAGE_METHOD = notReloadable(key(adapter -> StorageType.parse(adapter.getString("storage.method", "json"))));
    public static final ConfigKey<StorageCredentials> STORAGE_CREDENTIALS = notReloadable(key(adapter -> {
        String address = adapter.getString("storage.data.address", "localhost");
        String database = adapter.getString("storage.data.database", "minecraft");
        String username = adapter.getString("storage.data.username", "root");
        String password = adapter.getString("storage.data.password", "");

        int maxPoolSize = adapter.getInteger("storage.data.pool-settings.maximum-pool-size", 10);
        int minIdle = adapter.getInteger("storage.data.pool-settings.minimum-idle", maxPoolSize);
        int maxLifetime = adapter.getInteger("storage.data.pool-settings.maximum-lifetime", 1800000);
        int connectionTimeout = adapter.getInteger("storage.data.pool-settings.connection-timeout", 5000);
        int keepAliveTime = adapter.getInteger("storage.data.pool-settings.keep-alive", 0);
        Map<String, String> props = ImmutableMap.copyOf(adapter.getStringMap("storage.data.pool-settings.properties", ImmutableMap.of()));
        return new StorageCredentials(address, database, username, password, maxPoolSize, minIdle, maxLifetime, keepAliveTime, connectionTimeout, props);
    }));
    public static final ConfigKey<String> SQL_TABLE_PREFIX = notReloadable(stringKey("storage.table-prefix", "gts_"));

    // Communication Service
    public static final ConfigKey<String> MESSAGING_SERVICE = notReloadable(stringKey("messaging-service", "none"));
}
