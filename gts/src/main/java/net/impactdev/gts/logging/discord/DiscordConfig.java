package net.impactdev.gts.logging.discord;

import net.impactdev.impactor.api.configuration.key.ConfigKey;

import static net.impactdev.impactor.api.configuration.key.ConfigKeyFactory.booleanKey;
import static net.impactdev.impactor.api.configuration.key.ConfigKeyFactory.stringKey;

public final class DiscordConfig {

    public static final ConfigKey<Boolean> ENABLED = booleanKey("enabled", false);
    public static final ConfigKey<String> AVATAR = stringKey("avatar", "https://cdn.bulbagarden.net/upload/thumb/f/f5/399Bidoof.png/600px-399Bidoof.png");
    public static final ConfigKey<String> NAME = stringKey("name", "GTS Discord Logger");

}
