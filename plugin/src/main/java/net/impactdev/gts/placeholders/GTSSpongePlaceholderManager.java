package net.impactdev.gts.placeholders;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.leangen.geantyref.TypeToken;
import net.impactdev.gts.api.listings.makeup.Fees;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.communication.message.errors.ErrorCode;
import net.impactdev.gts.api.util.groupings.SimilarPair;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.utils.EconomicFormatter;
import net.impactdev.gts.common.utils.future.CompletableFutureManager;
import net.impactdev.gts.placeholders.parsers.IdentifiableParser;
import net.impactdev.gts.placeholders.parsers.concurrent.AsyncUserSourcedPlaceholder;
import net.impactdev.gts.SpongeGTSPlugin;
import net.impactdev.gts.ui.submenu.SpongeListingMenu;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.utilities.Time;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.placeholders.parsers.SourceSpecificPlaceholderParser;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.placeholder.PlaceholderContext;
import org.spongepowered.api.placeholder.PlaceholderParser;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.plugin.PluginContainer;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GTSSpongePlaceholderManager {

    private final List<PlaceholderMetadata> parsers = Lists.newArrayList();
    private final ResourceKey.Builder key = ResourceKey.builder()
            .namespace("gts");

    public GTSSpongePlaceholderManager() {
        this.populate();
    }

    public void register(PlaceholderMetadata parser) {
        this.parsers.add(parser);
    }

    private void register(IdentifiableParser parser) {
        this.parsers.add(PlaceholderMetadata.of(
                this.key.value(parser.key()).build(),
                parser
        ));
    }

    public ImmutableList<PlaceholderMetadata> getAllParsers() {
        return ImmutableList.copyOf(this.parsers);
    }

    public void populate() {
        Config msgConf = GTSPlugin.instance().configuration().language();
        MessageService processor = Impactor.getInstance().getRegistry().get(MessageService.class);
        PluginContainer container = GTSPlugin.instance().as(SpongeGTSPlugin.class).container();

        this.register(this.create("prefix", context -> processor.parse(msgConf.get(MsgConfigKeys.PREFIX))));
        this.register(this.create("error", context -> processor.parse(msgConf.get(MsgConfigKeys.ERROR_PREFIX))));
        this.register(new SourceSpecificPlaceholderParser.Decorative<>(
                Listing.class,
                "seller",
                listing -> this.userCache.get(listing.getLister()).getNow(Component.text("Pooling..."))
        ));
        this.register(new SourceSpecificPlaceholderParser.Decorative<>(
                UUID.class,
                "purchaser",
                id -> this.userCache.get(id).getNow(Component.text("Pooling..."))
        ));

        // Listing Related Placeholders
        this.register(new SourceSpecificPlaceholderParser<>(
                Listing.class,
                "listing_id",
                listing -> Component.text(listing.getID().toString())
        ));
        this.register(new SourceSpecificPlaceholderParser.Decorative<>(
                Listing.class,
                "listing_name",
                listing -> listing.getEntry().getName()
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Listing.class,
                "listing_details",
                listing -> listing.getEntry().getDescription()
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Listing.class,
                "listing_status",
                listing -> {
                    final MessageService parser = Impactor.getInstance().getRegistry().get(MessageService.class);

                    if(listing instanceof BuyItNow && ((BuyItNow) listing).isPurchased()) {
                        return parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.STATUS_PURCHASED));
                    }

                    LocalDateTime expiration = listing.getExpiration();
                    LocalDateTime now = LocalDateTime.now();
                    Time time = new Time(Duration.between(now, expiration).getSeconds());
                    if(time.getTime() <= 0) {
                        return parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.STATUS_TIME_EXPIRED));
                    }

                    PlaceholderSources sources = PlaceholderSources.builder()
                            .append(Time.class, () -> time)
                            .build();
                    return parser.parse(
                            Utilities.readMessageConfigOption(MsgConfigKeys.TIME_REMAINING_TRANSLATION),
                            sources
                    );
                }
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                Price.class,
                "price_selection",
                Price::getText
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Tuple.class,
                "price_fee",
                wrapper -> {
                    if(wrapper.getFirst() instanceof Price && wrapper.getSecond() instanceof Boolean) {
                        Price<?, ?, ?> price = (Price<?, ?, ?>) wrapper.getFirst();
                        boolean listingType = (Boolean) wrapper.getSecond();

                        return Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(price.calculateFee(listingType));
                    }

                    return Component.empty();
                }
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Boolean.class,
                "price_fee_rate",
                state -> {
                    ConfigKey<Float> key = state ? ConfigKeys.FEES_STARTING_PRICE_RATE_BIN : ConfigKeys.FEES_STARTING_PRICE_RATE_AUCTION;
                    float rate = GTSPlugin.instance().configuration().main().get(key);
                    DecimalFormat df = new DecimalFormat("#0.##");

                    return Component.text(df.format(rate * 100) + "%");
                }
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                Time.class,
                "time",
                Utilities::translateTime
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Time.class,
                "time_short",
                time -> Component.text(time.asPatternized())
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Time.class,
                "time_fee",
                time -> {
                    org.mariuszgromada.math.mxparser.Function function = GTSPlugin.instance().configuration().main().get(ConfigKeys.FEE_TIME_EQUATION);
                    SimilarPair<Argument> arguments = Utilities.calculateTimeFee(time);
                    Expression expression = new Expression("f(hours,minutes)", function, arguments.getFirst(), arguments.getSecond());
                    return Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(expression.calculate());
                }
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                Fees.class,
                "fees",
                fees -> {
                    Component result = Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(fees.getTotal());

                    final MessageService parser = Impactor.getInstance().getRegistry().get(MessageService.class);

                    PlaceholderSources sources = PlaceholderSources.builder()
                            .append(new TypeToken<Tuple<Price<?, ?, ?>, Boolean>>() {}, fees::getPrice)
                            .append(Time.class, () -> fees.getTime().getFirst())
                            .build();

                    TextComponent hover = Component.text()
                            .append(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.FEE_PRICE_FORMAT), sources))
                            .append(Component.newline())
                            .append(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.FEE_TIME_FORMAT), sources))
                            .build();
                    return result.hoverEvent(HoverEvent.showText(hover));
                }
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                Double.class,
                "min_price",
                value -> Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(value))
        );
        this.register(new SourceSpecificPlaceholderParser<>(
                Double.class,
                "max_price",
                value -> Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(value))
        );

        // Buy It Now
        this.register(new SourceSpecificPlaceholderParser<>(
                BuyItNow.class,
                "bin_price",
                bin -> bin.getPrice().getText()
        ));

        // Auction Related Placeholders
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.class,
                "auction_bids",
                auction -> Component.text(auction.getBids().size())
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.class,
                "auction_start_price",
                auction -> Sponge.server().serviceProvider()
                        .economyService()
                        .orElseThrow(IllegalStateException::new)
                        .defaultCurrency()
                        .format(BigDecimal.valueOf(auction.getStartingPrice()))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.class,
                "auction_current_price",
                auction -> Sponge.server().serviceProvider()
                        .economyService()
                        .orElseThrow(IllegalStateException::new)
                        .defaultCurrency()
                        .format(BigDecimal.valueOf(auction.getCurrentPrice()))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.class,
                "auction_high_bid",
                auction -> Sponge.server().serviceProvider()
                        .economyService()
                        .orElseThrow(IllegalStateException::new)
                        .defaultCurrency()
                        .format(BigDecimal.valueOf(auction.getHighBid().map(Tuple::getSecond).map(Auction.Bid::getAmount).orElseThrow(() -> new IllegalStateException("Unable to locate bid amount"))))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.class,
                "auction_high_bidder",
                auction -> {
                    if(auction.getHighBid().isPresent()) {
                        return this.userCache.get(auction.getLister())
                                .getNow(Component.text("Pooling..."));
                    }

                    return Component.empty();
                }
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                UUID.class,
                "auction_bidder",
                id -> this.userCache.get(id).getNow(Component.text("Pooling..."))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.class,
                "auction_next_required_bid",
                auction -> Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(auction.getNextBidRequirement())
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Double.class,
                "auction_previous_user_bid",
                value -> Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(value)
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.BidContext.class,
                "auction_bid_amount",
                bid -> Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(bid.getBid().getAmount())
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.BidContext.class,
                "auction_bid_actor",
                bid -> this.userCache.get(bid.getBidder()).getNow(Component.text("Pooling..."))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.BidContext.class,
                "auction_bid_since_placed",
                bid -> {
                    Duration duration = Duration.between(bid.getBid().getTimestamp(), LocalDateTime.now());
                    Time time = new Time(duration.getSeconds());
                    if(time.getTime() < 60) {
                        return Component.text(GTSPlugin.instance().configuration().language().get(MsgConfigKeys.TIME_MOMENTS_TRANSLATION));
                    }

                    return Utilities.translateTimeHighest(time);
                }
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Double.class,
                "auction_outbid_amount",
                difference -> Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(difference)
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                Integer.class,
                "stash_returned",
                Component::text
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                SpongeListingMenu.Searching.class,
                "search_query",
                query -> Component.text(query.getQuery())
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                ErrorCode.class,
                "error_code",
                error -> Component.text(error.getKey())
                        .hoverEvent(HoverEvent.showText(Component.text(error.getDescription())))
        ));
        this.register(this.create(
                "max_listings",
                context -> Component.text(GTSPlugin.instance().configuration().main().get(ConfigKeys.MAX_LISTINGS_PER_USER))
        ));
        this.register(AsyncUserSourcedPlaceholder.builder()
                .type(Integer.class)
                .id("active_bids")
                .parser(Component::text)
                .loader((uuid, executor) -> GTSPlugin.instance().storage()
                        .fetchListings(Lists.newArrayList(
                                listing -> listing instanceof Auction,
                                listing -> !listing.hasExpired()
                        ))
                        .thenApply(listings -> listings.stream().map(listing -> (Auction) listing).collect(Collectors.toList()))
                        .thenApply(auctions -> {
                            int bids = 0;
                            for (Auction auction : auctions) {
                                if (auction.getBids().containsKey(uuid)) {
                                    ++bids;
                                }
                            }

                            return bids;
                        })
                )
                .def(0)
                .build()
        );
        this.register(new SourceSpecificPlaceholderParser<>(
                TextComponent.class,
                "claim_item",
                result -> result
        ));

        // Item Based Placeholders
        this.register(new SourceSpecificPlaceholderParser<>(
                ItemStackSnapshot.class,
                "item_lore",
                snapshot -> {
                    TextComponent.Builder result = Component.text();
                    List<Component> lines = snapshot.get(Keys.LORE).orElse(Lists.newArrayList());

                    if(lines.size() > 0) {
                        for (int i = 0; i < lines.size() - 1; i++) {
                            result.append(lines.get(i)).append(Component.newline());
                        }

                        result.append(lines.get(lines.size() - 1));
                    }

                    return result.build();
                }
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                ItemStackSnapshot.class,
                "item_enchantments",
                snapshot -> {
                    TextComponent.Builder result = Component.text();
                    List<ComponentLike> lines = snapshot.get(Keys.APPLIED_ENCHANTMENTS)
                            .map(enchantments -> {
                                List<ComponentLike> data = Lists.newArrayList();
                                for(Enchantment enchantment : enchantments) {
                                    data.add(enchantment.type());
                                }

                                return data;
                            })
                            .orElse(Lists.newArrayList());

                    if(lines.size() > 0) {
                        for (int i = 0; i < lines.size() - 1; i++) {
                            result.append(lines.get(i)).append(Component.newline());
                        }

                        result.append(lines.get(lines.size() - 1));
                    }

                    return result.build();
                }
        ));
    }

    private PlaceholderMetadata create(String key, Function<PlaceholderContext, Component> parser) {
        return PlaceholderMetadata.of(
                this.key.value(key).build(),
                PlaceholderParser.builder()
                        .parser(parser)
                        .build()
        );
    }

    private Optional<String> getOptionFromSubject(Subject subject, String... options) {
        for (String option : options) {
            String o = option.toLowerCase();

            Optional<String> os = subject.option(o);
            if (os.isPresent()) {
                return os.map(r -> r.isEmpty() ? null : r);
            }
        }

        return Optional.empty();
    }

    private AsyncLoadingCache<UUID, Component> userCache = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .buildAsync(new AsyncCacheLoader<UUID, Component>() {
                @Override
                public @NonNull CompletableFuture<Component> asyncLoad(@NonNull UUID key, @NonNull Executor executor) {
                    return calculateDisplayName(key);
                }
            });

    private CompletableFuture<Component> calculateDisplayName(UUID id) {
        return Sponge.server().userManager().loadOrCreate(id)
                .thenApply(user -> {
                    TextComponent.Builder component = Component.text();

                    if(GTSPlugin.instance().configuration().main().get(ConfigKeys.SHOULD_SHOW_USER_PREFIX)) {
                        Optional<String> prefix = this.getOptionFromSubject(user, "prefix");
                        prefix.ifPresent(pre -> component.append(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix.get())));
                    }

                    Optional<String> color = this.getOptionFromSubject(user, "color");
                    NamedTextColor translated = color.map(NamedTextColor.NAMES::value).orElse(null);

                    Component name = Component.text(user.name());
                    if(translated != null) {
                        name = name.color(translated);
                    } else {
                        Style style = this.getDeepestStyle(component.build());
                        name = name.style(style);
                    }

                    return component.append(Component.space()).append(name).build();
                }).applyToEither(
                        CompletableFutureManager.timeoutAfter(5, TimeUnit.SECONDS),
                        Component::compact
                );
    }

    private Style getDeepestStyle(Component component) {
        if(component.children().isEmpty()) {
            return component.style();
        }

        int size = component.children().size();
        return this.getDeepestStyle(component.children().get(size - 1));
    }

}
