package net.impactdev.gts.common.config;

import net.impactdev.impactor.api.configuration.Config;

public class ConfigProvider {

    private final Config main;
    private final Config lang;

    public ConfigProvider(Config main, Config lang) {
        this.main = main;
        this.lang = lang;
    }

    public Config main() {
        return this.main;
    }

    public Config language() {
        return this.lang;
    }

}
