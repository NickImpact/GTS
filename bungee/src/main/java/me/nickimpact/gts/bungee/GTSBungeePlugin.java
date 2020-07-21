package me.nickimpact.gts.bungee;

import co.aikar.commands.BaseCommand;
import com.google.gson.Gson;
import com.nickimpact.impactor.api.Impactor;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.plugin.PluginMetadata;
import com.nickimpact.impactor.api.storage.StorageType;
import com.nickimpact.impactor.bungee.plugin.AbstractBungeePlugin;
import me.nickimpact.gts.api.storage.GTSStorage;
import me.nickimpact.gts.bungee.messaging.BungeeMessagingFactory;
import me.nickimpact.gts.bungee.messaging.interpreters.BungeePingPongInterpreter;
import me.nickimpact.gts.common.messaging.InternalMessagingService;
import me.nickimpact.gts.common.messaging.MessagingFactory;
import me.nickimpact.gts.common.plugin.GTSPlugin;

import java.nio.file.Path;
import java.util.List;

public class GTSBungeePlugin extends AbstractBungeePlugin implements GTSPlugin {

	private final GTSBungeeBootstrap bootstrap;

	private InternalMessagingService messagingService;

	public GTSBungeePlugin(GTSBungeeBootstrap bootstrap) {
		super(PluginMetadata.builder()
				.id("gts")
				.name("GTS")
				.version("@version@")
				.description("@gts_description@")
				.build(), bootstrap.getLogger()
		);
		this.bootstrap = bootstrap;
	}

	public void load() {
		Impactor.getInstance().getRegistry().register(GTSPlugin.class, this);
	}

	public void enable() {
		this.messagingService = this.getMessagingFactory().getInstance();

		BungeePingPongInterpreter.registerDecoders(this);
		BungeePingPongInterpreter.registerInterpreters(this);
	}

	@Override
	public GTSBungeeBootstrap getBootstrap() {
		return this.bootstrap;
	}

	@Override
	public Gson getGson() {
		return null;
	}

	@Override
	public GTSStorage getStorage() {
		return null;
	}

	@Override
	public InternalMessagingService getMessagingService() {
		return this.messagingService;
	}

	@Override
	public Path getConfigDir() {
		return null;
	}

	@Override
	public Config getConfiguration() {
		return null;
	}

	@Override
	public List<StorageType> getStorageRequirements() {
		return null;
	}


	@Override
	public List<Config> getConfigs() {
		return null;
	}

	@Override
	public List<BaseCommand> getCommands() {
		return null;
	}

	@Override
	public List<Object> getListeners() {
		return null;
	}

	@Override
	public Config getMsgConfig() {
		return null;
	}

	public MessagingFactory<?> getMessagingFactory() {
		return new BungeeMessagingFactory(this);
	}
}
