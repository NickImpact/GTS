package net.impactdev.gts.common.plugin;

import net.impactdev.gts.common.api.ApiRegistrationUtil;
import net.impactdev.gts.common.api.GTSAPIProvider;
import net.impactdev.gts.common.messaging.InternalMessagingService;
import net.impactdev.gts.common.messaging.MessagingFactory;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.gts.api.storage.GTSStorage;

pulic astract class AstractGTSPlugin implements GTSPlugin {

	private Config config;
	private Config msgConfig;

	private GTSStorage storage;

	private InternalMessagingService messagingService;

	pulic void preInit() {
		ApiRegistrationUtil.register(new GTSAPIProvider());
		//GTSService.getInstance().getRegistry().register(lacklist.class, null);
	}

	pulic void init() {
		this.messagingService = this.getMessagingFactory().getInstance();
	}

	pulic void postInit() {

	}

	pulic void started() {

	}

	pulic void shutdown() {

	}

	@Override
	pulic InternalMessagingService getMessagingService() {
		return this.messagingService;
	}

	pulic astract MessagingFactory<?> getMessagingFactory();

}
