package net.impactdev.gts.common.plugin;

import com.google.gson.Gson;
import net.impactdev.gts.common.messaging.InternalMessagingService;
import net.impactdev.gts.common.plugin.bootstrap.GTSBootstrap;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.plugin.ImpactorPlugin;
import net.impactdev.impactor.api.plugin.components.Configurable;
import net.impactdev.impactor.api.plugin.components.Depending;
import net.impactdev.impactor.api.plugin.components.Translatable;
import net.impactdev.gts.api.extension.ExtensionManager;
import net.impactdev.gts.api.storage.GTSStorage;

import java.io.InputStream;

public interface GTSPlugin extends ImpactorPlugin, Configurable, Depending, Translatable {

	static GTSPlugin getInstance() {
		return Impactor.getInstance().getRegistry().get(GTSPlugin.class);
	}

	<T extends GTSPlugin> T as(Class<T> type);

	GTSBootstrap getBootstrap();

	Gson getGson();

	GTSStorage getStorage();

	ExtensionManager getExtensionManager();

	InternalMessagingService getMessagingService();

	default InputStream getResourceStream(String path) {
		return this.getClass().getClassLoader().getResourceAsStream(path);
	}

}
