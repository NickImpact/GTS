package net.impactdev.gts.common.config.wrappers;

import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.configuration.ConfigKey;

public class AtLeastOne {

    private ConfigKey<Boolean> compare;
    private boolean state;

    public AtLeastOne(ConfigKey<Boolean> compare, boolean state) {
        this.compare = compare;
        this.state = state;
    }

    public boolean get() {
        if(this.state) {
            return this.state;
        }

        return !GTSPlugin.getInstance().getConfiguration().get(this.compare);
    }

}
