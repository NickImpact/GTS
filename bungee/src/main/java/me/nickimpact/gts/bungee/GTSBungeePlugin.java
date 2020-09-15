package me.nickimpact.gts.bungee;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nickimpact.impactor.api.Impactor;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.plugin.PluginMetadata;
import com.nickimpact.impactor.api.storage.StorageType;
import com.nickimpact.impactor.bungee.configuration.BungeeConfig;
import com.nickimpact.impactor.bungee.configuration.BungeeConfigAdapter;
import com.nickimpact.impactor.bungee.plugin.AbstractBungeePlugin;
import me.nickimpact.gts.api.blacklist.Blacklist;
import me.nickimpact.gts.api.storage.GTSStorage;
import me.nickimpact.gts.bungee.messaging.BungeeMessagingFactory;
import me.nickimpact.gts.bungee.messaging.interpreters.BungeeBINRemoveInterpreter;
import me.nickimpact.gts.bungee.messaging.interpreters.BungeePingPongInterpreter;
import me.nickimpact.gts.common.blacklist.BlacklistImpl;
import me.nickimpact.gts.common.config.updated.ConfigKeys;
import me.nickimpact.gts.common.messaging.InternalMessagingService;
import me.nickimpact.gts.common.messaging.MessagingFactory;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.common.storage.StorageFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class GTSBungeePlugin extends AbstractBungeePlugin implements GTSPlugin {

	private final GTSBungeeBootstrap bootstrap;

	private Config config;

	private GTSStorage storage;
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
		Impactor.getInstance().getRegistry().register(Blacklist.class, new BlacklistImpl());
	}

	public void enable() {
		this.config = new BungeeConfig(new BungeeConfigAdapter(this, new File(this.getConfigDir().toFile(), "main.conf")), new ConfigKeys());
		this.storage = new StorageFactory(this).getInstance(StorageType.MARIADB);

		this.messagingService = this.getMessagingFactory().getInstance();

		BungeePingPongInterpreter.registerDecoders(this);
		BungeePingPongInterpreter.registerInterpreters(this);
		new BungeeBINRemoveInterpreter().register(this);
	}

	@Override
	public <T extends GTSPlugin> T as(Class<T> type) {
		if(!type.isAssignableFrom(this.getClass())) {
			throw new RuntimeException("Invalid plugin typing");
		}
		return (T) this;
	}

	@Override
	public GTSBungeeBootstrap getBootstrap() {
		return this.bootstrap;
	}

	@Override
	public Gson getGson() {
		return new GsonBuilder().create();
	}

	@Override
	public GTSStorage getStorage() {
		return this.storage;
	}

	@Override
	public InternalMessagingService getMessagingService() {
		return this.messagingService;
	}

	@Override
	public Path getConfigDir() {
		return this.bootstrap.getConfigDirectory();
	}

	@Override
	public Config getConfiguration() {
		return this.config;
	}

	@Override
	public List<StorageType> getStorageRequirements() {
		return Lists.newArrayList(StorageType.MARIADB);
	}

	@Override
	public Config getMsgConfig() {
		return null;
	}

	public MessagingFactory<?> getMessagingFactory() {
		return new BungeeMessagingFactory(this);
	}
}
