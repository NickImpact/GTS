package net.impactdev.gts.common.plugin;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import net.impactdev.gts.common.messaging.InternalMessagingService;
import net.impactdev.gts.common.plugin.bootstrap.GTSBootstrap;
import net.impactdev.gts.common.utils.EconomicFormatter;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.plugin.ImpactorPlugin;
import net.impactdev.impactor.api.plugin.components.Configurable;
import net.impactdev.impactor.api.plugin.components.Depending;
import net.impactdev.impactor.api.plugin.components.Translatable;
import net.impactdev.gts.api.extension.ExtensionManager;
import net.impactdev.gts.api.storage.GTSStorage;
import net.impactdev.impactor.api.storage.StorageType;

import java.io.InputStream;
import java.util.UUID;

public interface GTSPlugin extends ImpactorPlugin, Configurable, Depending, Translatable {

	static GTSPlugin getInstance() {
		return Impactor.getInstance().getRegistry().get(GTSPlugin.class);
	}

	<T extends GTSPlugin> T as(Class<T> type);

	/**
	 *
	 *
	 * @return
	 */
	GTSBootstrap getBootstrap();

	/**
	 * Fetches information regarding the environment
	 *
	 * @return
	 */
	Environment getEnvironment();

	/**
	 *
	 * @return
	 */
	Gson getGson();

	/**
	 *
	 * @return
	 */
	GTSStorage getStorage();

	ExtensionManager getExtensionManager();

	InternalMessagingService getMessagingService();

	ImmutableList<StorageType> getMultiServerCompatibleStorageOptions();

	String getPlayerDisplayName(UUID id);

	default InputStream getResourceStream(String path) {
		return this.getClass().getClassLoader().getResourceAsStream(path);
	}
}
