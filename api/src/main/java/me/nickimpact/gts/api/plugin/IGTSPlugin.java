package me.nickimpact.gts.api.plugin;

import com.google.gson.Gson;
import com.nickimpact.impactor.api.plugin.Configurable;
import com.nickimpact.impactor.api.plugin.Dependable;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.Translatable;

import java.io.InputStream;
import java.util.concurrent.ScheduledExecutorService;

public interface IGTSPlugin extends ImpactorPlugin, Configurable, Translatable, IGTSBacking, Dependable {

	ScheduledExecutorService getAsyncExecutor();

	Gson getGson();

	default InputStream getResourceStream(String path) {
		return getClass().getClassLoader().getResourceAsStream(path);
	}

}
