package net.impactdev.gts.locale.placeholders;

import net.impactdev.gts.locale.Translatables;
import net.impactdev.gts.locale.translations.TranslationProvider;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.gts.util.GTSKeys;
import net.impactdev.gts.util.MiniMessageColorTranslator;
import net.impactdev.impactor.api.platform.audience.LocalizedAudience;
import net.impactdev.impactor.api.platform.sources.PlatformSource;
import net.impactdev.impactor.api.text.placeholders.PlaceholderArguments;
import net.impactdev.impactor.api.text.placeholders.PlaceholderParser;
import net.impactdev.impactor.api.utility.Context;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static net.kyori.adventure.text.Component.text;

public final class GTSPlaceholders {

    private static final Map<Key, PlaceholderParser> PARSERS = new HashMap<>();
    private static final PlatformSource FALLBACK = PlatformSource.console();

    public static final PlaceholderParser PREFIX = create(
            GTSKeys.gts("prefix"),
            (audience, context) -> resolve(Translatables.PREFIX, audience, context)
    );
    public static final PlaceholderParser ERROR_PREFIX = create(
            GTSKeys.gts("error"),
            (audience, context) -> resolve(Translatables.ERROR, audience, context)
    );

    public static final PlaceholderParser AVAILABLE_LISTINGS = create(
            GTSKeys.gts("available-listings"),
            (audience, context) -> text("TODO").color(NamedTextColor.RED)
    );
    public static final PlaceholderParser STASH_SIZE = create(
            GTSKeys.gts("stash-size"),
            (audience, context) -> {
                Optional<PlaceholderArguments> queue = context.request(PlaceholderArguments.class);
                if(queue.isPresent()) {
                    int size = 0;

                    PlaceholderArguments args = queue.get();
                    final TextColor color;
                    if(args.hasNext()) {
                        String arg = args.pop();
                        if(args.hasNext() && size == 0) {
                            arg = args.pop();
                        }

                        color = MiniMessageColorTranslator.resolve(arg);
                    } else {
                        color = null;
                    }

                    return text(size).color(color);
                } else {
                    return text(0);
                }
            }
    );

    public static Map<Key, PlaceholderParser> parsers() {
        return PARSERS;
    }

    private static PlaceholderParser create(Key key, PlaceholderParser parser) {
        GTSPlugin.instance().logger().debug("Registering placeholder: " + key.asString());
        PARSERS.put(key, parser);
        return parser;
    }

    private static <T> T resolve(TranslationProvider<T> provider, @Nullable LocalizedAudience audience, @NotNull Context context) {
        if(audience == null) {
            return provider.resolve(FALLBACK, context);
        } else {
            return provider.resolve(audience, context);
        }
    }

}
