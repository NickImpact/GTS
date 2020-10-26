package net.impactdev.gts.placeholders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.utilities.Time;
import net.impactdev.gts.GTSSpongePlugin;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.placeholders.parsers.SourceSpecificPlaceholderParser;
import net.impactdev.gts.sponge.utils.Utilities;
import net.kyori.text.TextComponent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.placeholder.PlaceholderContext;
import org.spongepowered.api.text.placeholder.PlaceholderParser;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
                            .map(user -> {
                                Text result = Text.EMPTY;

                                Optional<String> prefix = this.getOptionFromSubject(user, "prefix");
                                if(prefix.isPresent()) {
                                    result = TextSerializers.FORMATTING_CODE.deserialize(prefix.get());
                                }

                                Text.Builder builder = Text.builder(" " + user.getName());

                                Optional<String> color = this.getOptionFromSubject(user, "color");

                                if(color.isPresent()) {
                                    builder.color(color
                                            .map(in -> Sponge.getRegistry().getType(TextColor.class, in.toUpperCase()).orElse(TextColors.NONE))
                                            .orElse(TextColors.NONE)
                                    );
                                }

                                return Text.join(result, Text.of(TextColors.RESET, builder.build()));
                            })
                            .orElse(Text.of("Unknown"));
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
                "listing_details",
                "GTS - Listing Details",
                listing -> TextSerializers.JSON.deserialize(GsonComponentSerializer.INSTANCE.serialize(listing.getEntry().getDescription()))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Listing.class,
                "time_left",
                "GTS - Listing Time Remaining",
                listing -> {
                    LocalDateTime expiration = listing.getExpiration();
                    LocalDateTime now = LocalDateTime.now();
                    return Text.of(new Time(Duration.between(now, expiration).getSeconds()).asPatternized());
                }
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                Time.class,
                "time",
                "GTS - Amount of time representing how long a listing will be listed for",
                Utilities::translateTime
        ));

        // Buy It Now
        this.register(new SourceSpecificPlaceholderParser<>(
                BuyItNow.class,
                "bin_price",
                "GTS - Buy It Now Price",
                bin -> TextSerializers.JSON.deserialize(GsonComponentSerializer.INSTANCE.serialize(bin.getPrice().getText()))
        ));

        // Auction Related Placeholders
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.class,
                "auction_bids",
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
                "auction_high_bid",
                "GTS - High Bid of an Auction",
                auction -> Sponge.getServiceManager().provideUnchecked(EconomyService.class).getDefaultCurrency().format(BigDecimal.valueOf(auction.getHighBid().getSecond()))
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
                String.class, // TODO - Modify to go ask the proxy if in multi-server for the info if the host server does not have it
                "auction_bidder",
                "GTS - Bidder on an Auction",
                Text::of
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                Integer.class,
                "stash_returned",
                "GTS - Stash Contents Returned Successfully",
                Text::of
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                ErrorCode.class,
                "error_code",
                "An error code indicating why a request failed",
                error -> {
                    TextComponent component = TextComponent.builder(error.getKey())
                            .hoverEvent(HoverEvent.showText(TextComponent.builder().append().build()))
                            .build();
                    return Utilities.translateComponent(component);
                }
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

    private Optional<String> getOptionFromSubject(Subject subject, String... options) {
        for (String option : options) {
            String o = option.toLowerCase();

            // Option for context.
            Optional<String> os = subject.getOption(subject.getActiveContexts(), o);
            if (os.isPresent()) {
                return os.map(r -> r.isEmpty() ? null : r);
            }

            // General option
            os = subject.getOption(o);
            if (os.isPresent()) {
                return os.map(r -> r.isEmpty() ? null : r);
            }
        }

        return Optional.empty();
    }

}
