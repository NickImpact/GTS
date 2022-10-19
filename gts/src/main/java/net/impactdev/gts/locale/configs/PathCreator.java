package net.impactdev.gts.locale.configs;

import net.impactdev.impactor.api.configuration.ConfigPath;

public class PathCreator {

    public static ConfigPath create(String path) {
        return ConfigPath.path(path, false);
    }

}
