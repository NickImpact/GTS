package me.nickimpact.gts.common.plugin;

import com.nickimpact.impactor.api.configuration.Config;
import me.nickimpact.gts.api.storage.GTSStorage;
import me.nickimpact.gts.common.api.ApiRegistrationUtil;
import me.nickimpact.gts.common.api.GTSAPIProvider;
import me.nickimpact.gts.common.messaging.InternalMessagingService;
import me.nickimpact.gts.common.messaging.MessagingFactory;

public abstract class AbstractGTSPlugin implements GTSPlugin {

	private Config config;
	private Config msgConfig;

	private GTSStorage storage;

	private InternalMessagingService messagingService;

	public void preInit() {
		ApiRegistrationUtil.register(new GTSAPIProvider());
		//GTSService.getInstance().getRegistry().register(Blacklist.class, null);
	}

	public void init() {
		this.messagingService = this.getMessagingFactory().getInstance();
	}

	public void postInit() {

	}

	public void started() {

	}

	public void shutdown() {

	}

	@Override
	public InternalMessagingService getMessagingService() {
		return this.messagingService;
	}

	public abstract MessagingFactory<?> getMessagingFactory();

}
