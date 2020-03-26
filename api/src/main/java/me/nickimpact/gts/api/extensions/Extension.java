package me.nickimpact.gts.api.extensions;

import com.nickimpact.impactor.api.plugin.Configurable;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.Translatable;
import me.nickimpact.gts.api.GTSService;

public interface Extension extends ImpactorPlugin, Configurable, Translatable {

	String getName();

	String getVersion();

	GTSService getAPIService();


}
