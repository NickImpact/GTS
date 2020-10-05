package net.impactdev.gts.common.plugin;

import net.impactdev.gts.common.api.ApiRegistrationUtil;
import net.impactdev.gts.common.api.GTSAPIProvider;
import net.impactdev.gts.common.messaging.InternalMessagingService;
import net.impactdev.gts.common.messaging.MessagingFactory;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.gts.api.storage.GTSStorage;

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
