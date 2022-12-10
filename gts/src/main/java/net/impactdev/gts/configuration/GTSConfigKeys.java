package net.impactdev.gts.configuration;

import net.impactdev.impactor.api.adventure.TextProcessor;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.configuration.loader.KeyProvider;

import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.booleanKey;
import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.customKey;
import static net.impactdev.impactor.api.configuration.ConfigPath.path;

@KeyProvider
public class GTSConfigKeys {

    public static final ConfigKey<TextProcessor> TEXT_PROCESSOR = customKey(adapter -> {
        String input = adapter.getString(path("translations.processor.type"), "mini-message");
        switch (input) {
            case "mini-message":
                return TextProcessor.mini();
            case "legacy":
                return TextProcessor.legacy(adapter.getString(path("translations.processor.legacy-key"), "&").charAt(0));
            default:
                throw new IllegalArgumentException("Invalid text processor specified!");
        }
    });
    public static final ConfigKey<Boolean> AUTO_INSTALL_TRANSLATIONS = booleanKey(path("translations.auto-install"), true);

}
