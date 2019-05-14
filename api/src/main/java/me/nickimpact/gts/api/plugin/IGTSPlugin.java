package me.nickimpact.gts.api.plugin;

import com.google.gson.Gson;
import com.nickimpact.impactor.api.plugin.Configurable;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.Translatable;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.dependencies.DependencyManager;
import me.nickimpact.gts.api.dependencies.classloader.PluginClassLoader;

import java.io.InputStream;
import java.util.concurrent.ScheduledExecutorService;

public interface IGTSPlugin extends ImpactorPlugin, Configurable, Translatable, IGTSBacking {

	ScheduledExecutorService getAsyncExecutor();

	Gson getGson();

	default InputStream getResourceStream(String path) {
		return getClass().getClassLoader().getResourceAsStream(path);
	}

	PluginClassLoader getPluginClassLoader();

	DependencyManager getDependencyManager();
}
