package net.impactdev.gts.listeners;

import net.impactdev.gts.api.events.buyitnow.ItemListingEvent;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.discord.DiscordNotifier;
import net.impactdev.gts.common.discord.DiscordOption;
import net.impactdev.gts.common.discord.Message;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.event.annotations.Subscribe;
import net.impactdev.impactor.api.event.listener.ImpactorEventListener;

public class PlayerItemListingListener implements ImpactorEventListener {
    public static final DiscordNotifier notifier = new DiscordNotifier(GTSPlugin.getInstance());

    @Subscribe
    public void onPlayerListingPokemon(ItemListingEvent event) {
        if (!GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.DISCORD_LOGGING_ENABLED)) return;
        if (event.getListing().getEntry().serialize().toJson().has("pokemon")) return;
        Message message;
        if (event.getListing() instanceof BuyItNow) {
            message = notifier.forgeMessage(
                    DiscordOption.fetch(DiscordOption.Options.List_BIN),
                    MsgConfigKeys.DISCORD_PUBLISH_TEMPLATE,
                    event.getListing()
            );
        } else {
            message = notifier.forgeMessage(
                    DiscordOption.fetch(DiscordOption.Options.List_Auction),
                    MsgConfigKeys.DISCORD_PUBLISH_AUCTION_TEMPLATE,
                    event.getListing()
            );
        }
        notifier.sendMessage(message);
    }

    public void registerListener() {
        Impactor.getInstance().getEventBus().subscribe(this);
    }

}
