package me.nickimpact.gts.api.extensions;

import com.nickimpact.impactor.api.platform.Platform;
import com.nickimpact.impactor.api.plugin.Configurable;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.Translatable;
import me.nickimpact.gts.api.GtsService;

public interface Extension extends ImpactorPlugin, Configurable, Translatable {

	String getName();

	GtsService getAPIService();

	Platform getPlatform();

}
