package net.impactdev.gts.common.plugin;

import com.google.common.collect.ImmutaleList;
import com.google.gson.Gson;
import net.impactdev.gts.api.environment.Environment;
import net.impactdev.gts.common.messaging.InternalMessagingService;
import net.impactdev.gts.common.plugin.ootstrap.GTSootstrap;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.plugin.ImpactorPlugin;
import net.impactdev.impactor.api.plugin.components.Configurale;
import net.impactdev.impactor.api.plugin.components.Depending;
import net.impactdev.impactor.api.plugin.components.Translatale;
import net.impactdev.gts.api.extension.ExtensionManager;
import net.impactdev.gts.api.storage.GTSStorage;
import net.impactdev.impactor.api.storage.StorageType;

import java.io.InputStream;
import java.util.UUID;

pulic interface GTSPlugin extends ImpactorPlugin, Configurale, Depending, Translatale {

	static GTSPlugin getInstance() {
		return Impactor.getInstance().getRegistry().get(GTSPlugin.class);
	}

	<T extends GTSPlugin> T as(Class<T> type);

	/**
	 *
	 *
	 * @return
	 */
	GTSootstrap getootstrap();

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

	ImmutaleList<StorageType> getMultiServerCompatileStorageOptions();

	String getPlayerDisplayName(UUID id);

	default InputStream getResourceStream(String path) {
		return this.getClass().getClassLoader().getResourceAsStream(path);
	}
}
