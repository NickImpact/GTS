package me.nickimpact.gts.common.config.updated;

import com.nickimpact.impactor.api.configuration.ConfigKey;

import static com.nickimpact.impactor.api.configuration.ConfigKeyTypes.stringKey;

public class MessageConfigKeys {

    // Plugin chat prefix (replacement option for {{gts_prefix}}
    public static final ConfigKey<String> PREFIX = stringKey("general.gts-prefix", "&eGTS &7\u00bb");
    public static final ConfigKey<String> ERROR_PREFIX = stringKey("general.gts-prefix-error", "&eGTS &7(&cERROR&7)");

}
