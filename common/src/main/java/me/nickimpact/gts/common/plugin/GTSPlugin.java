package me.nickimpact.gts.common.plugin;

import com.google.gson.Gson;
import com.nickimpact.impactor.api.Impactor;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.components.Configurable;
import com.nickimpact.impactor.api.plugin.components.Depending;
import com.nickimpact.impactor.api.plugin.components.Translatable;
import me.nickimpact.gts.api.storage.GTSStorage;
import me.nickimpact.gts.common.messaging.InternalMessagingService;
import me.nickimpact.gts.common.plugin.bootstrap.GTSBootstrap;

import java.io.InputStream;

public interface GTSPlugin extends ImpactorPlugin, Configurable, Depending, Translatable {

	static GTSPlugin getInstance() {
		return Impactor.getInstance().getRegistry().get(GTSPlugin.class);
	}

	<T extends GTSPlugin> T as(Class<T> type);

	GTSBootstrap getBootstrap();

	Gson getGson();

	GTSStorage getStorage();

	InternalMessagingService getMessagingService();

	default InputStream getResourceStream(String path) {
		return this.getClass().getClassLoader().getResourceAsStream(path);
	}

}
