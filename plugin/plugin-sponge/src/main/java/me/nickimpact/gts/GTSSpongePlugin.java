package me.nickimpact.gts;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.SpongeCommandManager;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nickimpact.impactor.api.Impactor;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.plugin.PluginMetadata;
import com.nickimpact.impactor.api.storage.StorageType;
import com.nickimpact.impactor.sponge.configuration.SpongeConfig;
import com.nickimpact.impactor.sponge.configuration.SpongeConfigAdapter;
import com.nickimpact.impactor.sponge.plugin.AbstractSpongePlugin;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.data.registry.StorableRegistry;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.storage.GTSStorage;
import me.nickimpact.gts.commands.GTSCommand;
import me.nickimpact.gts.common.api.ApiRegistrationUtil;
import me.nickimpact.gts.common.api.GTSAPIProvider;
import me.nickimpact.gts.common.config.MsgConfigKeys;
import me.nickimpact.gts.common.messaging.InternalMessagingService;
import me.nickimpact.gts.common.messaging.MessagingFactory;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.listeners.PingListener;
import me.nickimpact.gts.listings.SpongeItemEntry;
import me.nickimpact.gts.listings.data.SpongeItemManager;
import me.nickimpact.gts.listings.legacy.SpongeLegacyItemStorable;
import me.nickimpact.gts.manager.SpongeListingManager;
import me.nickimpact.gts.messaging.SpongeMessagingFactory;
import me.nickimpact.gts.messaging.interpreters.SpongePingPongInterpreter;
import me.nickimpact.gts.sponge.listings.SpongeBuyItNow;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class GTSSpongePlugin extends AbstractSpongePlugin implements GTSPlugin {

	private final GTSSpongeBootstrap bootstrap;

	private Config config;
	private Config msgConfig;

	private InternalMessagingService messagingService;

	public GTSSpongePlugin(GTSSpongeBootstrap bootstrap, org.slf4j.Logger fallback) {
		super(PluginMetadata.builder()
				.id("gts")
				.name("GTS")
				.version("@version@")
				.description("@gts_description@")
				.build(),
				fallback
		);
		this.bootstrap = bootstrap;
	}

	public void preInit() {
		ApiRegistrationUtil.register(new GTSAPIProvider());
		Impactor.getInstance().getRegistry().register(GTSPlugin.class, this);

		this.displayBanner();

		ApiRegistrationUtil.register(new GTSAPIProvider());
		Sponge.getServiceManager().setProvider(this.bootstrap, GTSService.class, GTSService.getInstance());
		Impactor.getInstance().getRegistry().registerBuilderSupplier(Listing.ListingBuilder.class, SpongeBuyItNow.SpongeListingBuilder::new);
		Impactor.getInstance().getRegistry().register(SpongeListingManager.class, new SpongeListingManager());

		StorableRegistry storables = GTSService.getInstance().getStorableRegistry();
		storables.register(SpongeItemEntry.class, new SpongeItemManager());
		storables.registerLegacyStorable("item", new SpongeLegacyItemStorable());

		this.msgConfig = new SpongeConfig(new SpongeConfigAdapter(this, new File(this.getConfigDir().toFile(), "lang/en_us.conf")), new MsgConfigKeys());
	}

	public void init() {
		this.messagingService = this.getMessagingFactory().getInstance();
		SpongePingPongInterpreter.register(this);

		Impactor.getInstance().getEventBus().subscribe(new PingListener());

//		Sponge.getServiceManager().provideUnchecked(ProtocolService.class).events().register(new SignListener());

		SpongeCommandManager commands = new SpongeCommandManager(this.bootstrap.getContainer());
		commands.registerCommand(new GTSCommand());
	}

	public void started() {
		ItemStack test = ItemStack.builder()
				.itemType(ItemTypes.BARRIER)
				.add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Testing"))
				.build();
		SpongeItemEntry testing = new SpongeItemEntry(test.createSnapshot());
		this.getPluginLogger().debug(testing.getInternalData().toJson().toString());
	}

	public MessagingFactory<?> getMessagingFactory() {
		return new SpongeMessagingFactory(this);
	}

	@Override
	public List<Config> getConfigs() {
		return Lists.newArrayList();
	}

	@Override
	public List<BaseCommand> getCommands() {
		return Lists.newArrayList();
	}

	@Override
	public List<Object> getListeners() {
		return Lists.newArrayList();
	}

	public EconomyService getEconomy() {
		return null;
	}

	public PluginContainer getPluginContainer() {
		return this.bootstrap.getContainer();
	}

	@Override
	public GTSSpongeBootstrap getBootstrap() {
		return this.bootstrap;
	}

	@Override
	public Gson getGson() {
		return new GsonBuilder().create();
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

	public Config getMsgConfig() {
		return this.msgConfig;
	}

	private void displayBanner() {
		List<String> output = Lists.newArrayList(
				"",
				"&3     _________________",
				"&3    / ____/_  __/ ___/       &aGTS " + this.getMetadata().getVersion(),
				"&3   / / __  / /  \\__ \\        &aRunning on: &e" + Sponge.getGame().getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getName() + " " + Sponge.getGame().getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getVersion().orElse(""),
				"&3  / /_/ / / /  ___/ /        &aAuthor: &3NickImpact",
				"&3  \\____/ /_/  /____/",
				""
		);

		GTSPlugin.getInstance().getPluginLogger().noTag(output);
	}
}
