package me.nickimpact.gts.placeholders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.Impactor;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.services.text.MessageService;
import com.nickimpact.impactor.api.utilities.Time;
import me.nickimpact.gts.GTSSpongePlugin;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.auctions.Auction;
import me.nickimpact.gts.common.config.MsgConfigKeys;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.placeholders.parsers.SourceSpecificPlaceholderParser;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.placeholder.PlaceholderContext;
import org.spongepowered.api.text.placeholder.PlaceholderParser;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public class GTSSpongePlaceholderManager {

    private final List<PlaceholderParser> parsers = Lists.newArrayList();

    public GTSSpongePlaceholderManager() {
        this.populate();
    }

    public void register(PlaceholderParser parser) {
        this.parsers.add(parser);
    }

    public ImmutableList<PlaceholderParser> getAllParsers() {
        return ImmutableList.copyOf(this.parsers);
    }

    @SuppressWarnings("unchecked")
    public void populate() {
        Config msgConf = GTSPlugin.getInstance().getMsgConfig();
        MessageService<Text> processor = Impactor.getInstance().getRegistry().get(MessageService.class);
        PluginContainer container = GTSPlugin.getInstance().as(GTSSpongePlugin.class).getPluginContainer();

        this.register(this.create("prefix", "GTS Prefix", container, context -> processor.parse(msgConf.get(MsgConfigKeys.PREFIX))));
        this.register(this.create("error", "GTS Error Prefix", container, context -> processor.parse(msgConf.get(MsgConfigKeys.ERROR_PREFIX))));
        this.register(new SourceSpecificPlaceholderParser<>(
                Listing.class,
                "seller",
                "GTS - Listing Seller",
                listing -> {
                    UserStorageService service = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
                    return service.get(listing.getLister())
                            .map(User::getName)
                            .map(Text::of)
                            .orElse((LiteralText) Text.EMPTY);
                }
        ));

        // Listing Related Placeholders
        this.register(new SourceSpecificPlaceholderParser<>(
                Listing.class,
                "listing_id",
                "GTS - Listing ID",
                listing -> Text.of(listing.getID())
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Listing.class,
                "listing_name",
                "GTS - Listing Name",
                listing -> TextSerializers.JSON.deserialize(GsonComponentSerializer.INSTANCE.serialize(listing.getEntry().getName()))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Listing.class,
                "time_left",
                "GTS - Listing Time Remaining",
                listing -> {
                    LocalDateTime expiration = listing.getExpiration();
                    LocalDateTime now = LocalDateTime.now();
                    return Text.of(new Time(Duration.between(now, expiration).getSeconds()).toString());
                }
        ));

        // Auction Related Placeholders
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.class,
                "auction_bid_count",
                "GTS - Current Bid Count on an Auction",
                auction -> Text.of(auction.getBids().size())
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.class,
                "auction_start_price",
                "GTS - Starting Price of an Auction",
                auction -> Sponge.getServiceManager().provideUnchecked(EconomyService.class).getDefaultCurrency().format(BigDecimal.valueOf(auction.getStartingPrice()))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.class,
                "auction_current_price",
                "GTS - Current Price of an Auction",
                auction -> Sponge.getServiceManager().provideUnchecked(EconomyService.class).getDefaultCurrency().format(BigDecimal.valueOf(auction.getCurrentPrice()))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.class,
                "auction_high_bidder",
                "GTS - High Bidder of an Auction",
                auction -> {
                    UserStorageService service = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
                    return service.get(auction.getHighBid().getFirst())
                            .map(User::getName)
                            .map(Text::of)
                            .orElse((LiteralText) Text.EMPTY);
                }
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                String.class, // Would use UUID but
                "auction_bidder",
                "GTS - Bidder on an Auction",
                Text::of
        ));
    }

    private PlaceholderParser create(String id, String name, PluginContainer plugin, Function<PlaceholderContext, Text> parser) {
        return PlaceholderParser.builder()
                .id(id)
                .name(name)
                .plugin(plugin)
                .parser(parser)
                .build();
    }



}
