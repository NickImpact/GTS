package me.nickimpact.gts.common.plugin;

import com.google.gson.Gson;
import com.nickimpact.impactor.api.plugin.Configurable;
import com.nickimpact.impactor.api.plugin.Dependable;
import com.nickimpact.impactor.api.plugin.ImpactorPlugin;
import com.nickimpact.impactor.api.plugin.Translatable;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.scheduling.SchedulerAdapter;
import me.nickimpact.gts.api.storage.GTSStorage;
import me.nickimpact.gts.common.messaging.InternalMessagingService;
import me.nickimpact.gts.common.plugin.bootstrap.GTSBootstrap;
import me.nickimpact.gts.common.tasks.SyncTask;

import java.io.InputStream;

public interface GTSPlugin extends ImpactorPlugin, Configurable, Translatable, Dependable {

	static GTSPlugin getInstance() {
		return GTSService.getInstance().getRegistry().get(GTSPlugin.class);
	}

	GTSBootstrap getBootstrap();

	Gson getGson();

	GTSStorage getStorage();

	SchedulerAdapter getScheduler();

	InternalMessagingService getMessagingService();

	SyncTask.Buffer getSyncTaskBuffer();

	default InputStream getResourceStream(String path) {
		return getClass().getClassLoader().getResourceAsStream(path);
	}

}
