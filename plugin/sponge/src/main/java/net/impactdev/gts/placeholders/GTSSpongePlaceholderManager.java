package net.impactdev.gts.placeholders;

import com.google.common.collect.BiMap;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.impactdev.gts.api.listings.makeup.Fees;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.util.groupings.SimilarPair;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.utils.EconomicFormatter;
import net.impactdev.gts.placeholders.parsers.concurrent.AsyncUserSourcedPlaceholder;
import net.impactdev.gts.ui.submenu.SpongeListingMenu;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.configuration.ConfigKey;
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
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.placeholder.PlaceholderContext;
import org.spongepowered.api.text.placeholder.PlaceholderParser;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        this.register(new SourceSpecificPlaceholderParser.Decorative<>(
                Listing.class,
                "seller",
                "GTS - Listing Seller",
                listing -> this.calculateDisplayName(listing.getLister())
        ));
        this.register(new SourceSpecificPlaceholderParser.Decorative<>(
                UUID.class,
                "purchaser",
                "GTS - Listing Purchaser",
                this::calculateDisplayName
        ));

        // Listing Related Placeholders
        this.register(new SourceSpecificPlaceholderParser<>(
                Listing.class,
                "listing_id",
                "GTS - Listing ID",
                listing -> Component.text(listing.getID().toString())
        ));
        this.register(new SourceSpecificPlaceholderParser.Decorative<>(
                Listing.class,
                "listing_name",
                "GTS - Listing Name",
                listing -> listing.getEntry().getName()
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Listing.class,
                "listing_details",
                "GTS - Listing Details",
                listing -> listing.getEntry().getDescription()
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Listing.class,
                "listing_status",
                "GTS - Listing Time Remaining",
                listing -> {
                    final MessageService<Text> parser = Impactor.getInstance().getRegistry().get(MessageService.class);

                    if(listing instanceof BuyItNow && ((BuyItNow) listing).isPurchased()) {
                        return Utilities.toComponent(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.STATUS_PURCHASED)));
                    }

                    LocalDateTime expiration = listing.getExpiration();
                    LocalDateTime now = LocalDateTime.now();
                    Time time = new Time(Duration.between(now, expiration).getSeconds());
                    if(time.getTime() <= 0) {
                        return Utilities.toComponent(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.STATUS_TIME_EXPIRED)));
                    }
                    return Utilities.toComponent(parser.parse(
                            Utilities.readMessageConfigOption(MsgConfigKeys.TIME_REMAINING_TRANSLATION),
                            Lists.newArrayList(() -> time)
                    ));
                }
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                Price.class,
                "price_selection",
                "GTS - Price Selection for creating Listing",
                Price::getText
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Tuple.class,
                "price_fee",
                "GTS - Price Selection Fee",
                wrapper -> {
                    if(wrapper.getFirst() instanceof Price && wrapper.getSecond() instanceof Boolean) {
                        Price<?, ?, ?> price = (Price<?, ?, ?>) wrapper.getFirst();
                        boolean listingType = (Boolean) wrapper.getSecond();

                        return Component.text(Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(price.calculateFee(listingType)));
                    }

                    return Component.empty();
                }
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Boolean.class,
                "price_fee_rate",
                "GTS - Price Selection Fee Rate",
                state -> {
                    ConfigKey<Float> key = state ? ConfigKeys.FEES_STARTING_PRICE_RATE_BIN : ConfigKeys.FEES_STARTING_PRICE_RATE_AUCTION;
                    float rate = GTSPlugin.getInstance().getConfiguration().get(key);
                    DecimalFormat df = new DecimalFormat("#0.##");

                    return Component.text(df.format(rate * 100) + "%");
                }
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                Time.class,
                "time",
                "GTS - Amount of time representing how long a listing will be listed for",
                Utilities::translateTime
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Time.class,
                "time_short",
                "GTS - A short version of the output of {{gts:time}}",
                time -> Component.text(time.asPatternized())
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Time.class,
                "time_fee",
                "GTS - Calculated fee for chosen time",
                time -> {
                    org.mariuszgromada.math.mxparser.Function function = GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.FEE_TIME_EQUATION);
                    SimilarPair<Argument> arguments = Utilities.calculateTimeFee(time);
                    Expression expression = new Expression("f(hours,minutes)", function, arguments.getFirst(), arguments.getSecond());
                    return Component.text(Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(expression.calculate()));
                }
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                Fees.class,
                "fees",
                "GTS - Fee Wrapper",
                fees -> {
                    TextComponent result = Component.text(
                            Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(fees.getTotal())
                    );

                    final MessageService<Text> parser = Impactor.getInstance().getRegistry().get(MessageService.class);

                    List<Supplier<Object>> sources = Lists.newArrayList();
                    sources.add(fees::getPrice);
                    sources.add(() -> fees.getTime().getFirst());

                    TextComponent hover = Component.text()
                            .append(Utilities.toComponent(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.FEE_PRICE_FORMAT), sources)))
                            .append(Component.newline())
                            .append(Utilities.toComponent(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.FEE_TIME_FORMAT), sources)))
                            .build();
                    return result.hoverEvent(HoverEvent.showText(hover));
                }
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                Double.class,
                "min_price",
                "GTS - Minimum Price Descriptor",
                value -> Component.text(Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(value))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Double.class,
                "max_price",
                "GTS - Maximum Price Descriptor",
                value -> Component.text(Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(value))
        ));

        // Buy It Now
        this.register(new SourceSpecificPlaceholderParser<>(
                BuyItNow.class,
                "bin_price",
                "GTS - Buy It Now Price",
                bin -> bin.getPrice().getText()
        ));

        // Auction Related Placeholders
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.class,
                "auction_bids",
                "GTS - Current Bid Count on an Auction",
                auction -> Component.text(auction.getBids().size())
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.class,
                "auction_start_price",
                "GTS - Starting Price of an Auction",
                auction -> Component.text(Sponge.getServiceManager().provideUnchecked(EconomyService.class).getDefaultCurrency().format(BigDecimal.valueOf(auction.getStartingPrice())).toPlain())
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.class,
                "auction_current_price",
                "GTS - Current Price of an Auction",
                auction -> Component.text(Sponge.getServiceManager().provideUnchecked(EconomyService.class).getDefaultCurrency().format(BigDecimal.valueOf(auction.getCurrentPrice())).toPlain())
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.class,
                "auction_high_bid",
                "GTS - High Bid of an Auction",
                auction -> Component.text(Sponge.getServiceManager().provideUnchecked(EconomyService.class).getDefaultCurrency()
                        .format(BigDecimal.valueOf(auction.getHighBid().map(Tuple::getSecond).map(Auction.Bid::getAmount).orElseThrow(() -> new IllegalStateException("Unable to locate bid amount")))).toPlain())
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.class,
                "auction_high_bidder",
                "GTS - High Bidder of an Auction",
                auction -> {
                    if(auction.getHighBid().isPresent()) {
                        return this.calculateDisplayName(auction.getHighBid().get().getFirst());
                    }

                    return Component.empty();
                }
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                UUID.class,
                "auction_bidder",
                "GTS - Bidder on an Auction",
                this::calculateDisplayName
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.class,
                "auction_next_required_bid",
                "GTS - An auction's next required bid",
                auction -> Component.text(Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(auction.getNextBidRequirement()))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Double.class,
                "auction_previous_user_bid",
                "GTS - The previous highest bid by a user on an auction, if any",
                value -> Component.text(Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(value))
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.BidContext.class,
                "auction_bid_amount",
                "GTS - Amount placed on a Bid",
                bid -> Component.text(Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(bid.getBid().getAmount()))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.BidContext.class,
                "auction_bid_actor",
                "GTS - Actor who placed a Bid",
                bid -> this.calculateDisplayName(bid.getBidder())
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.BidContext.class,
                "auction_bid_since_placed",
                "GTS - How long it has been since a bid was placed",
                bid -> {
                    Duration duration = Duration.between(bid.getBid().getTimestamp(), LocalDateTime.now());
                    Time time = new Time(duration.getSeconds());
                    if(time.getTime() < 60) {
                        return Component.text(GTSPlugin.getInstance().getMsgConfig().get(MsgConfigKeys.TIME_MOMENTS_TRANSLATION));
                    }

                    return Utilities.translateTimeHighest(time);
                }
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Double.class,
                "auction_outbid_amount",
                "GTS - Amount a bid was outbid by",
                difference -> Component.text(Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(difference))
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                Integer.class,
                "stash_returned",
                "GTS - Stash Contents Returned Successfully",
                Component::text
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                SpongeListingMenu.Searching.class,
                "search_query",
                "GTS - A search query applied by a user",
                query -> Component.text(query.getQuery())
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                ErrorCode.class,
                "error_code",
                "GTS - An error code indicating why a request failed",
                error -> Component.text(error.getKey())
                        .hoverEvent(HoverEvent.showText(Component.text(error.getDescription())))
        ));
        this.register(this.create(
                "max_listings",
                "GTS - Max Listings Configuration Response",
                container,
                context -> Text.of(GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.MAX_LISTINGS_PER_USER))
        ));
        this.register(AsyncUserSourcedPlaceholder.builder()
                .type(Integer.class)
                .id("active_bids")
                .name("GTS - Active Bids (Async)")
                .parser(Component::text)
                .loader((uuid, executor) -> GTSPlugin.getInstance().getStorage()
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
                "GTS - The item a user is claiming",
                result -> result
        ));

        // Item Based Placeholders
        this.register(new SourceSpecificPlaceholderParser<>(
                ItemStackSnapshot.class,
                "item_lore",
                "GTS - An Item's Lore",
                snapshot -> {
                    Text result = Text.EMPTY;
                    List<Text> lines = snapshot.get(Keys.ITEM_LORE).orElse(Lists.newArrayList());

                    if(lines.size() > 0) {
                        for (int i = 0; i < lines.size() - 1; i++) {
                            result = Text.of(result, lines.get(i), Text.NEW_LINE);
                        }

                        result = Text.of(result, lines.get(lines.size() - 1));
                    }

                    return Utilities.toComponent(result);
                }
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                ItemStackSnapshot.class,
                "item_enchantments",
                "GTS - An Item's Enchantments",
                snapshot -> {
                    Text result = Text.EMPTY;
                    List<Text> lines = snapshot.get(Keys.ITEM_ENCHANTMENTS)
                            .map(enchantments -> {
                                List<Text> data = Lists.newArrayList();
                                for(Enchantment enchantment : enchantments) {
                                    data.add(Text.of(enchantment.getType().getTranslation().get()));
                                }

                                return data;
                            })
                            .orElse(Lists.newArrayList());

                    if(lines.size() > 0) {
                        for (int i = 0; i < lines.size() - 1; i++) {
                            result = Text.of(result, lines.get(i), Text.NEW_LINE);
                        }

                        result = Text.of(result, lines.get(lines.size() - 1));
                    }

                    return Utilities.toComponent(result);
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

    private TextComponent calculateDisplayName(UUID id) {
        UserStorageService service = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        return service.get(id)
                .map(user -> {
                    TextComponent.Builder component = Component.text();

                    if(GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.SHOULD_SHOW_USER_PREFIX)) {
                        Optional<String> prefix = this.getOptionFromSubject(user, "prefix");
                        prefix.ifPresent(pre -> component.append(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix.get())));
                    }

                    Optional<String> color = this.getOptionFromSubject(user, "color");
                    NamedTextColor translated = color.map(NamedTextColor.NAMES::value).orElse(null);

                    Component name = Component.text(user.getName());
                    if(translated != null) {
                        name = name.color(translated);
                    }

                    return component.append(name).build();
                })
                .orElse(Component.text("Unknown"));
    }

}
