package me.nickimpact.gts.api.plugin;

import com.nickimpact.impactor.api.platform.Platform;
import com.nickimpact.impactor.api.plugin.Configurable;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.Translatable;

public interface Extension extends ImpactorPlugin, Configurable, Translatable, IGTSBacking {

	Platform getPlatform();

}
