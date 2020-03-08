package me.nickimpact.gts.common.plugin.bootstrap;

import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.storage.dependencies.classloader.PluginClassLoader;
import me.nickimpact.gts.api.scheduling.SchedulerAdapter;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * Represents the bootstrap plugin interface.
 *
 * <p>Instances of this interface are responsible for loading the GTS plugin on their respective platforms,
 * and provide the plugin with its essential information for it to perform as intended.</p>
 */
public interface GTSBootstrap {

	Logger getPluginLogger();

	Path getDataDirectory();

	Path getConfigDirectory();

	SchedulerAdapter getScheduler();

	PluginClassLoader getPluginClassLoader();

	InputStream getResourceStream(String path);

}
