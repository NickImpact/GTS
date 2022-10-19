package net.impactdev.gts.locale.configs;

import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.configuration.loader.KeyProvider;
import net.kyori.adventure.text.Component;

import static net.impactdev.gts.locale.configs.PathCreator.create;
import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.listKey;
import static net.impactdev.impactor.api.configuration.ConfigKeyTypes.stringKey;


@KeyProvider
public final class GeneralKeys {

    // Metadata
    public static final ConfigKey<String> PREFIX = stringKey(create("gts.metadata.prefix"), "<#A6EAF7>GTS <gray>»");
    public static final ConfigKey<String> ERROR = stringKey(create("gts.metadata.prefix"), "<#A6EAF7>GTS <gray>(<red>ERROR<gray>)");

    // Generic
    public static final ConfigKey<String> SAFE_MODE = stringKey(
            create("gts.sade-mode.reason"),
            "<gts:error> GTS is currently in safe mode! All functionality is disabled! Reason: <gray>(<red><gts:error_code><gray>)"
    );
    public static final ConfigKey<String> PUBLISH_LISTING_BIN = stringKey(
            create("gts.broadcasts.publish-listing"),
            "<gts:prefix> <gts:seller> has added a <green><gts:listing.name> <gray>to the GTS for <green><gts:price><gray>!"
    );
}
