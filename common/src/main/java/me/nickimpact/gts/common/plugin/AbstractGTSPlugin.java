package me.nickimpact.gts.common.plugin;

import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.logging.Logger;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.blacklist.Blacklist;
import me.nickimpact.gts.api.placeholders.Placeholder;
import me.nickimpact.gts.api.placeholders.PlaceholderVariables;
import me.nickimpact.gts.api.storage.GTSStorage;
import me.nickimpact.gts.common.api.ApiRegistrationUtil;
import me.nickimpact.gts.common.api.GTSAPIProvider;
import me.nickimpact.gts.common.messaging.InternalMessagingService;
import me.nickimpact.gts.common.messaging.MessagingFactory;
import me.nickimpact.gts.common.placeholders.builders.GTSCustomPlaceholderBuilder;
import me.nickimpact.gts.common.placeholders.builders.GTSPlaceholderBuilder;
import me.nickimpact.gts.common.placeholders.builders.variables.GTSPlaceholderVariableKeyBuilder;
import me.nickimpact.gts.common.placeholders.builders.variables.GTSPlaceholderVariablesBuilder;
import me.nickimpact.gts.common.placeholders.types.GTSPlaceholder;
import me.nickimpact.gts.common.tasks.SyncTask;

public abstract class AbstractGTSPlugin implements GTSPlugin {

	/** The instance controlling the logging of the plugin */
	protected Logger logger;

	private Config config;
	private Config msgConfig;

	private GTSStorage storage;

	private InternalMessagingService messagingService;
	private SyncTask.Buffer syncTaskBuffer;

	public void preInit() {
		ApiRegistrationUtil.register(new GTSAPIProvider());

		GTSService.getInstance().getRegistry().registerBuilderSupplier(PlaceholderVariables.KeyBuilder.class, () -> GTSPlaceholderVariableKeyBuilder.INSTANCE);
		GTSService.getInstance().getRegistry().registerBuilderSupplier(PlaceholderVariables.PVBuilder.class, GTSPlaceholderVariablesBuilder::new);
		GTSService.getInstance().getRegistry().registerBuilderSupplier(Placeholder.StandardBuilder.class, GTSPlaceholderBuilder::new);
		GTSService.getInstance().getRegistry().registerBuilderSupplier(Placeholder.CustomBuilder.class, GTSCustomPlaceholderBuilder::new);
		//GTSService.getInstance().getRegistry().register(Blacklist.class, null);
	}

	public void init() {
		this.messagingService = this.getMessagingFactory().getInstance();
		this.syncTaskBuffer = new SyncTask.Buffer(this);
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
