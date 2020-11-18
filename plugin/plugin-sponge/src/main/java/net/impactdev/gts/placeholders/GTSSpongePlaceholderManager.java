package net.impactdev.gts.placeholders;

import com.google.common.collect.BiMap;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.sun.org.apache.xpath.internal.operations.Bool;
import net.impactdev.gts.api.listings.makeup.Fees;
import net.impactdev.gts.api.listings.prices.Price;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.util.WaitingInteger;
import net.impactdev.gts.api.util.groupings.SimilarPair;
import net.impactdev.gts.common.config.updated.ConfigKeys;
import net.impactdev.gts.common.utils.EconomicFormatter;
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
import net.kyori.text.TextComponent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.format.Style;
import net.kyori.text.format.TextColor;
import net.kyori.text.format.TextDecoration;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.placeholder.PlaceholderContext;
import org.spongepowered.api.text.placeholder.PlaceholderParser;
import org.spongepowered.api.text.serializer.TextSerializers;

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
                listing -> this.calculateDisplayName(listing.getLister())
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
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
                Price.class,
                "price_selection",
                "GTS - Price Selection for creating Listing",
                price -> Utilities.translateComponent(price.getText())
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Tuple.class,
                "price_fee",
                "GTS - Price Selection Fee",
                wrapper -> {
                    if(wrapper.getFirst() instanceof Price && wrapper.getSecond() instanceof Boolean) {
                        Price<?, ?, ?> price = (Price<?, ?, ?>) wrapper.getFirst();
                        boolean listingType = (Boolean) wrapper.getSecond();

                        return Text.of(Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(price.calculateFee(listingType)));
                    }

                    return Text.EMPTY;
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

                    return Text.of(df.format(rate * 100), "%");
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
                "time_fee",
                "GTS - Calculated fee for chosen time",
                time -> {
                    org.mariuszgromada.math.mxparser.Function function = GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.FEE_TIME_EQUATION);
                    SimilarPair<Argument> arguments = Utilities.calculateTimeFee(time);
                    Expression expression = new Expression("f(hours,minutes)", function, arguments.getFirst(), arguments.getSecond());
                    return Text.of(Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(expression.calculate()));
                }
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                Fees.class,
                "fees",
                "GTS - Fee Wrapper",
                fees -> {
                    TextComponent.Builder result = TextComponent.builder(
                            Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(fees.getTotal())
                    );

                    final MessageService<Text> parser = Impactor.getInstance().getRegistry().get(MessageService.class);

                    List<Supplier<Object>> sources = Lists.newArrayList();
                    sources.add(fees::getPrice);
                    sources.add(() -> fees.getTime().getFirst());

                    TextComponent hover = TextComponent.builder()
                            .append(Utilities.toComponent(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.FEE_PRICE_FORMAT), sources)))
                            .append(TextComponent.newline())
                            .append(Utilities.toComponent(parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.FEE_TIME_FORMAT), sources)))
                            .build();
                    return Utilities.translateComponent(result.hoverEvent(HoverEvent.showText(hover)).build());
                }
        ));

        this.register(new SourceSpecificPlaceholderParser<>(
                Double.class,
                "min_price",
                "GTS - Minimum Price Descriptor",
                value -> Text.of(Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(value))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Double.class,
                "max_price",
                "GTS - Maximum Price Descriptor",
                value -> Text.of(Impactor.getInstance().getRegistry().get(EconomicFormatter.class).format(value))
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
                auction -> Sponge.getServiceManager().provideUnchecked(EconomyService.class).getDefaultCurrency()
                        .format(BigDecimal.valueOf(auction.getHighBid().map(Tuple::getSecond).orElseThrow(() -> new IllegalStateException("Unable to locate bid amount"))))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Auction.class,
                "auction_high_bidder",
                "GTS - High Bidder of an Auction",
                auction -> {
                    if(auction.getHighBid().isPresent()) {
                        return this.calculateDisplayName(auction.getHighBid().get().getFirst());
                    }

                    return Text.EMPTY;
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
                SpongeListingMenu.Searching.class,
                "search_query",
                "GTS - A search query applied by a user",
                query -> Text.of(query.getQuery())
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                ErrorCode.class,
                "error_code",
                "GTS - An error code indicating why a request failed",
                error -> {
                    TextComponent component = TextComponent.builder(error.getKey())
                            .hoverEvent(HoverEvent.showText(TextComponent.builder().append().build()))
                            .build();
                    return Utilities.translateComponent(component);
                }
        ));
        this.register(this.create(
                "max_listings",
                "GTS - Max Listings Configuration Response",
                container,
                context -> Text.of(GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.MAX_LISTINGS_PER_USER))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                WaitingInteger.class,
                "active_bids",
                "GTS - Active Bids for a Player",
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

    private Text calculateDisplayName(UUID id) {
        UserStorageService service = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        return service.get(id)
                .map(user -> {
                    TextComponent.Builder component = TextComponent.builder();

                    AtomicReference<Style> style = new AtomicReference<>(Style.empty());

                    Optional<String> prefix = this.getOptionFromSubject(user, "prefix");
                    prefix.ifPresent(pre -> {
                        style.set(this.parseStyle(prefix.get()));

                        TextComponent p = TextComponent.builder()
                                .append(pre.replaceAll(STYLE_LOCATOR.pattern(), ""))
                                .style(style.get())
                                .build();
                        component.append(p);
                    });

                    Optional<String> color = this.getOptionFromSubject(user, "color");
                    TextColor translated = color.flatMap(TextColor.NAMES::value).orElse(null);
                    Style inherit = style.get().merge(Style.builder().color(translated).build(), Style.Merge.COLOR);

                    return Utilities.translateComponent(component.append(TextComponent.space())
                            .append(TextComponent.builder()
                                    .append(user.getName())
                                    .style(inherit)
                                    .build()
                            )
                            .build()
                    );
                })
                .orElse(Text.of("Unknown"));
    }

    private static final Pattern STYLE_LOCATOR = Pattern.compile("([&][a-f0-9klmnor])");

    private static final BiMap<Character, net.kyori.text.format.TextColor> ID_TO_COLOR =
            HashBiMap.create(
                    ImmutableMap.<Character, net.kyori.text.format.TextColor>builder()
                            .put('0', net.kyori.text.format.TextColor.BLACK)
                            .put('1', net.kyori.text.format.TextColor.DARK_BLUE)
                            .put('2', net.kyori.text.format.TextColor.DARK_GREEN)
                            .put('3', net.kyori.text.format.TextColor.DARK_AQUA)
                            .put('4', net.kyori.text.format.TextColor.DARK_RED)
                            .put('5', net.kyori.text.format.TextColor.DARK_PURPLE)
                            .put('6', net.kyori.text.format.TextColor.GOLD)
                            .put('7', net.kyori.text.format.TextColor.GRAY)
                            .put('8', net.kyori.text.format.TextColor.DARK_GRAY)
                            .put('9', net.kyori.text.format.TextColor.BLUE)
                            .put('a', net.kyori.text.format.TextColor.GREEN)
                            .put('b', net.kyori.text.format.TextColor.AQUA)
                            .put('c', net.kyori.text.format.TextColor.RED)
                            .put('d', net.kyori.text.format.TextColor.LIGHT_PURPLE)
                            .put('e', net.kyori.text.format.TextColor.YELLOW)
                            .put('f', net.kyori.text.format.TextColor.WHITE)
                            .build()
            );
    private static final BiMap<Character, Style> ID_TO_STYLE =
            HashBiMap.create(
                    ImmutableMap.<Character, Style>builder()
                            .put('l', Style.of(TextDecoration.BOLD))
                            .put('o', Style.of(TextDecoration.ITALIC))
                            .put('n', Style.of(TextDecoration.UNDERLINED))
                            .put('m', Style.of(TextDecoration.STRIKETHROUGH))
                            .put('k', Style.of(TextDecoration.OBFUSCATED))
                            .put('r', Style.empty())
                            .build()
            );

    private net.kyori.text.format.TextColor getColor(String style) {
        Pattern pattern = Pattern.compile("[a-f0-9]");
        Matcher matcher = pattern.matcher(style);

        net.kyori.text.format.TextColor color = null;
        while(matcher.find()) {
            color = ID_TO_COLOR.get(matcher.group().charAt(0));
        }

        return color;
    }

    private Style parseStyle(String in) {
        Queue<String> queue = EvictingQueue.create(2);
        Matcher matcher = STYLE_LOCATOR.matcher(in);
        while(matcher.find()) {
            queue.add(matcher.group(1));
        }

        StringJoiner joiner = new StringJoiner("");
        for(String s : queue) {
            joiner.add(s);
        }

        return this.getStyle(joiner.toString());
    }

    private Style getStyle(String style) {
        Style result = Style.empty();
        net.kyori.text.format.TextColor color = this.getColor(style);
        if(color != null) {
            result = result.color(color);
        }

        Pattern pattern = Pattern.compile("[k-or]");
        Matcher matcher = pattern.matcher(style);
        while(matcher.find()) {
            result = result.merge(ID_TO_STYLE.get(matcher.group().charAt(0)));
        }

        return result;
    }

}
