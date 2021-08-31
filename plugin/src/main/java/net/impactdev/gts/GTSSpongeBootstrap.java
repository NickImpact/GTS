package net.impactdev.gts;

import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import net.impactdev.gts.placeholders.GTSSpongePlaceholderManager;
import net.impactdev.gts.api.blacklist.Blacklist;
import net.impactdev.gts.api.events.extension.PluginReloadEvent;
import net.impactdev.gts.api.exceptions.LackingServiceException;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.commands.executors.safety.SafeModeExecutor;
import net.impactdev.gts.common.api.GTSAPIProvider;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.dependencies.classloader.PluginClassLoader;
import net.impactdev.impactor.api.dependencies.classloader.ReflectionClassLoader;
import net.impactdev.impactor.api.logging.Logger;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.events.extension.PlaceholderRegistryEvent;
import net.impactdev.gts.common.plugin.bootstrap.GTSBootstrap;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.placeholder.PlaceholderParser;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Plugin(
		id = "gts",
		name = "GTS",
		version = "@version@",
		description = "@description@",
		dependencies = {
				@Dependency(id = "impactor", version = "[3.0.0,)"),
				@Dependency(id = "protocolcontrol", version = "[0.0.2,)")
		})
public class GTSSpongeBootstrap implements GTSBootstrap {

	@Listener(order = Order.EARLY)
	public void onPreInit(GamePreInitializationEvent event) {
		Sponge.setTickSpeed(20);
	}

}
