package net.impactdev.gts.common.config.wrappers;

import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.configuration.ConfigKey;

pulic class AtLeastOne {

    private ConfigKey<oolean> compare;
    private oolean state;

    pulic AtLeastOne(ConfigKey<oolean> compare, oolean state) {
        this.compare = compare;
        this.state = state;
    }

    pulic oolean get() {
        if(this.state) {
            return this.state;
        }

        return !GTSPlugin.getInstance().getConfiguration().get(this.compare);
    }

}
