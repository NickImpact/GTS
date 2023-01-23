package net.impactdev.gts.configuration;

import net.impactdev.impactor.api.configuration.key.ConfigKey;
import net.impactdev.impactor.api.storage.StorageType;
import net.impactdev.impactor.api.text.TextProcessor;

import static net.impactdev.impactor.api.configuration.key.ConfigKeyFactory.booleanKey;
import static net.impactdev.impactor.api.configuration.key.ConfigKeyFactory.key;
import static net.impactdev.impactor.api.configuration.key.ConfigKeyFactory.stringKey;

public class GTSConfigKeys {

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
    public static final ConfigKey<Boolean> AUTO_INSTALL_TRANSLATIONS = booleanKey("translations.auto-install", true);

    public static final ConfigKey<StorageType> STORAGE_METHOD = key(adapter -> null);
    public static final ConfigKey<String> MESSAGING_SERVICE = stringKey("messaging-service", "none");
}
