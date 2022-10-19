package net.impactdev.gts.configuration;

import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.configuration.ConfigPath;
import net.impactdev.impactor.api.configuration.loader.KeyProvider;

import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.booleanKey;

@KeyProvider
public class GTSConfigKeys {

    public static final ConfigKey<Boolean> AUTO_INSTALL_TRANSLATIONS = booleanKey(ConfigPath.path("translations.auto-install"), true);

}
