package me.nickimpact.gts;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.nickimpact.impactor.api.logging.Logger;
import com.nickimpact.impactor.api.storage.dependencies.classloader.PluginClassLoader;
import com.nickimpact.impactor.sponge.logging.SpongeLogger;
import lombok.Getter;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.GTSServiceProvider;
import me.nickimpact.gts.api.scheduling.SchedulerAdapter;
import me.nickimpact.gts.common.api.ApiRegistrationUtil;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.common.plugin.bootstrap.GTSBootstrap;
import me.nickimpact.gts.scheduling.SpongeSchedulerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.AsynchronousExecutor;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.SynchronousExecutor;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static me.nickimpact.gts.GTSInfo.*;

@Plugin(
		id = ID,
		name = NAME,
		version = VERSION,
		description = DESCRIPTION,
		dependencies = {
				@Dependency(id = "impactor", version = "[2.2.0,)"),
				@Dependency(id = "nucleus")
		})
public class GTSSpongeBootstrap implements GTSBootstrap {

	private GTSSpongePlugin plugin;

	@Getter
	@Inject
	private PluginContainer container;

	@Inject
	private org.slf4j.Logger fallback;
	private Logger logger;

	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;

	private SpongeSchedulerAdapter scheduler;

	private Throwable exception;

	@Inject
	public GTSSpongeBootstrap(@SynchronousExecutor SpongeExecutorService sync, @AsynchronousExecutor SpongeExecutorService async) {
		this.plugin = new GTSSpongePlugin(this);
		this.logger = new SpongeLogger(this.plugin, fallback);
		this.scheduler = SpongeSchedulerAdapter.builder()
				.bootstrap(this)
				.scheduler(Sponge.getScheduler())
				.sync(sync)
				.async(async)
				.build();
	}

	@Listener(order = Order.EARLY)
	public void onPreInit(GamePreInitializationEvent event) {
		try {
			this.plugin.preInit();
		} catch (Exception e) {
			this.exception = e;
			this.disable();
			e.printStackTrace();
		}
	}

	@Listener
	public void onInit(GameInitializationEvent event) {
		try {
			this.plugin.init();
		} catch (Exception e) {
			this.exception = e;
			this.disable();
			e.printStackTrace();
		}
	}

	@Listener
	public void onLogin(ClientConnectionEvent.Join event) {
		//this.plugin.getMessagingService().pushTest(String.format("Server on port %d says \"Hello\"!", Sponge.getServer().getBoundAddress().get().getPort()));
		this.plugin.getMessagingService().sendPing();
	}

	@Override
	public Logger getPluginLogger() {
		return this.logger;
	}

	@Override
	public Path getDataDirectory() {
		return this.configDir.resolve("data");
	}

	@Override
	public Path getConfigDirectory() {
		return this.configDir;
	}

	@Override
	public SchedulerAdapter getScheduler() {
		return this.scheduler;
	}

	@Override
	public PluginClassLoader getPluginClassLoader() {
		return null;
	}

	@Override
	public InputStream getResourceStream(String path) {
		return null;
	}

	@Override
	public Optional<Throwable> getLaunchError() {
		return Optional.ofNullable(exception);
	}

	@Override
	public void disable() {
		Sponge.getEventManager().unregisterPluginListeners(this);
		Sponge.getCommandManager().getOwnedBy(this).forEach(Sponge.getCommandManager()::removeMapping);
		Sponge.getScheduler().getScheduledTasks(this).forEach(Task::cancel);

		Sponge.getEventManager().registerListener(this, GameStartedServerEvent.class, e -> this.displayErrorOnStart());
	}

	private void appendX(List<Text> messages, int spacing) {
		Text space = Text.of(String.join("", Collections.nCopies(spacing, " ")));
		messages.add(Text.of(space, TextColors.RED, "\\              /"));
		messages.add(Text.of(space, TextColors.RED, " \\            /"));
		messages.add(Text.of(space, TextColors.RED, "  \\          /"));
		messages.add(Text.of(space, TextColors.RED, "   \\        /"));
		messages.add(Text.of(space, TextColors.RED, "    \\      /"));
		messages.add(Text.of(space, TextColors.RED, "     \\    /"));
		messages.add(Text.of(space, TextColors.RED, "      \\  /"));
		messages.add(Text.of(space, TextColors.RED, "       \\/"));
		messages.add(Text.of(space, TextColors.RED, "       /\\"));
		messages.add(Text.of(space, TextColors.RED, "      /  \\"));
		messages.add(Text.of(space, TextColors.RED, "     /    \\"));
		messages.add(Text.of(space, TextColors.RED, "    /      \\"));
		messages.add(Text.of(space, TextColors.RED, "   /        \\"));
		messages.add(Text.of(space, TextColors.RED, "  /          \\"));
		messages.add(Text.of(space, TextColors.RED, " /            \\"));
		messages.add(Text.of(space, TextColors.RED, "/              \\"));
	}

	private void displayErrorOnStart() {
		List<Text> output = Lists.newArrayList();
		output.add(Text.of(TextColors.RED, "+-" + String.join("", Collections.nCopies(26, "-")) + "-+"));
		output.add(Text.of(TextColors.RED, "| " + StringUtils.center("GTS FAILED TO LOAD", 26) + " |"));
		output.add(Text.of(TextColors.RED, "+-" + String.join("", Collections.nCopies(26, "-")) + "-+"));
		this.appendX(output, 7);
		output.add(Text.of(TextColors.RED, "+-" + String.join("", Collections.nCopies(26, "-")) + "-+"));

		output.add(Text.EMPTY);
		output.add(Text.of(TextColors.RED, "GTS encountered an error during server start and did not enable successfully"));
		output.add(Text.of(TextColors.RED, "No commands, listeners, or tasks are registered"));
		output.add(Text.of(Text.EMPTY));

		output.add(Text.of(TextColors.RED, "The encountered error will be listed below:"));
		output.add(Text.of(TextColors.YELLOW, "+-" + String.join("", Collections.nCopies(26, "-")) + "-+"));
		if(!this.getLaunchError().isPresent()) {
			output.add(Text.of(TextColors.YELLOW, "No exception was logged..."));
		} else {
			Throwable exception = this.getLaunchError().get();

			try(StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
				exception.printStackTrace(pw);
				pw.flush();
				String[] trace = sw.toString().split("(\r)?\n");
				for(String s : trace) {
					output.add(Text.of(TextColors.YELLOW, s));
				}
			} catch (IOException e) {
				exception.printStackTrace();
			}
		}
		output.add(Text.of(TextColors.YELLOW, "+-" + String.join("", Collections.nCopies(26, "-")) + "-+"));
		output.add(Text.of(TextColors.RED, "If this error persists, ensure you are running the latest GTS versions"));
		output.add(Text.of(TextColors.RED, "If you do, please report this error to the GTS team at ", TextColors.AQUA, "https://github.com/NickImpact/GTS/issues"));
		output.add(Text.of(TextColors.YELLOW, "+-" + String.join("", Collections.nCopies(26, "-")) + "-+"));
		output.add(Text.of(TextColors.YELLOW, "| " + StringUtils.center("Server Information", 26) + " |"));
		output.add(Text.of(TextColors.YELLOW, "+-" + String.join("", Collections.nCopies(26, "-")) + "-+"));
		output.add(Text.of(TextColors.YELLOW, "GTS Version: ", TextColors.AQUA, this.plugin.getPluginInfo().getVersion()));
		output.add(Text.of(TextColors.YELLOW, "Sponge Version: ", TextColors.AQUA, Sponge.getGame().getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getName() + " " + Sponge.getGame().getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getVersion().orElse("")));

		if(!GTSService.getInstance().getRegistry().getLoadedExtensions().isEmpty()) {
			output.add(Text.of(TextColors.YELLOW, "+-" + String.join("", Collections.nCopies(26, "-")) + "-+"));
			output.add(Text.of(TextColors.YELLOW, "| " + StringUtils.center("Installed Extensions", 26) + " |"));
			output.add(Text.of(TextColors.YELLOW, "+-" + String.join("", Collections.nCopies(26, "-")) + "-+"));
			GTSService.getInstance().getRegistry().getLoadedExtensions().forEach(x -> output.add(Text.of(TextColors.YELLOW, x.getName(), " - ", x.getVersion())));
		}

		output.add(Text.of(TextColors.YELLOW, "+-" + String.join("", Collections.nCopies(26, "-")) + "-+"));
		output.add(Text.of(TextColors.YELLOW, "| " + StringUtils.center("END GTS ERROR REPORT", 26) + " |"));
		output.add(Text.of(TextColors.YELLOW, "+-" + String.join("", Collections.nCopies(26, "-")) + "-+"));

		for(Text out : output) {
			Sponge.getServer().getConsole().sendMessage(out);
		}
	}
}
