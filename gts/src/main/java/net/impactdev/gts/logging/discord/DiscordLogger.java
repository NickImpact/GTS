package net.impactdev.gts.logging.discord;


import com.google.common.base.Preconditions;
import net.impactdev.gts.api.logging.LogAction;
import net.impactdev.gts.logging.discord.element.DiscordElement;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.impactor.api.configuration.Config;
import org.jetbrains.annotations.NotNull;

public final class DiscordLogger {

    private final Config config;

    public DiscordLogger(GTSPlugin plugin) {
        this.config = Config.builder()
                .path(plugin.configurationDirectory().resolve("discord.conf"))
                .provider(DiscordConfig.class)
                .provideIfMissing(() -> plugin.resource(root -> root.resolve("discord.conf")))
                .build();
    }

    public void publish(@NotNull DiscordElement element, @NotNull LogAction action) {
        Preconditions.checkNotNull(element);
        Preconditions.checkNotNull(action);


    }

}
