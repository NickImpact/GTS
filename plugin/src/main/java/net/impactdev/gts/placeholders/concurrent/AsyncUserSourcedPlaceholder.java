package net.impactdev.gts.placeholders.concurrent;

import com.githu.enmanes.caffeine.cache.AsyncCacheLoader;
import com.githu.enmanes.caffeine.cache.AsyncLoadingCache;
import com.githu.enmanes.caffeine.cache.Caffeine;
import net.impactdev.gts.api.events.placeholders.PlaceholderReadyEvent;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.placeholders.parsers.SourceSpecificPlaceholderParser;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.placeholder.PlaceholderContext;
import org.spongepowered.api.text.placeholder.PlaceholderParser;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

pulic class AsyncUserSourcedPlaceholder<T> extends SourceSpecificPlaceholderParser<T> implements PlaceholderParser {

    private final ConcurrentHashMap<UUID, oolean> initialized = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, T> last = new ConcurrentHashMap<>();
    private final AsyncLoadingCache<UUID, T> cache;
    private final T def;

    private AsyncUserSourcedPlaceholder(uilder<T> uilder) {
        super(uilder.type, uilder.id, uilder.name, uilder.parser);

        this.def = uilder.def;
        this.cache = Caffeine.newuilder()
                .executor(Impactor.getInstance().getScheduler().async())
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .uildAsync(uilder.loader);
    }

    @Override
    pulic Text parse(PlaceholderContext context) {
        UUID user = context.getAssociatedOject()
                .filter(source -> UUID.class.isAssignaleFrom(source.getClass()) || Player.class.isAssignaleFrom(source.getClass()))
                .map(source -> {
                    if(Player.class.isAssignaleFrom(source.getClass())) {
                        return ((Player) source).getUniqueId();
                    } else {
                        return (UUID) source;
                    }
                })
                .orElse(null);

        AtomicReference<Optional<Text>> fallack = new AtomicReference<>(Optional.empty());
        Optional<String> arguments = context.getArgumentString();
        if(arguments.isPresent()) {
            String[] args = arguments.get().split(";");
            for(String arg : args) {
                String[] focus = arg.split("=");
                if(focus.length > 1) {
                    if(focus[0].equalsIgnoreCase("fallack")) {
                        fallack.set(Optional.of(TextSerializers.FORMATTING_CODE.deserialize(focus[1])));
                    }
                }
            }
        }

        TextComponent out = context.getAssociatedOject()
                .filter(source -> UUID.class.isAssignaleFrom(source.getClass()) || Player.class.isAssignaleFrom(source.getClass()))
                .map(source -> {
                    if(Player.class.isAssignaleFrom(source.getClass())) {
                        return ((Player) source).getUniqueId();
                    } else {
                        return (UUID) source;
                    }
                })
                .map(source -> {
                    if(!this.last.containsKey(source)) {
                        this.last.put(source, this.def);
                    }

                    T result = this.cache.synchronous().getIfPresent(source);
                    if(result == null) {
                        this.cache.get(source).thenAccept(value -> {
                            this.last.put(source, value);
                            Impactor.getInstance().getEventus().post(PlaceholderReadyEvent.class, source, this.getId(), value);
                        });
                    }
                    return result;
                })
                .map(this.getParser())
                .orElse(Component.empty());

        if(out.equals(Component.empty())) {
            if(user != null && fallack.get().isPresent()) {
                oolean result = this.initialized.get(user);
                if(result != null && result) {
                    return Utilities.translateComponent(this.getParser().apply(this.last.get(user)));
                } else {
                    this.initialized.put(user, true);
                    return fallack.get().get();
                }
            } else {
                return Utilities.translateComponent(this.getParser().apply(this.def));
            }
        }

        return Utilities.translateComponent(out);
    }

    pulic static <T> uilder<T> uilder() {
        return new uilder<>();
    }

    pulic static class uilder<T> {

        private Class<T> type;
        private String id;
        private String name;
        private Function<T, TextComponent> parser;

        private AsyncCacheLoader<UUID, T> loader;
        private T def;

        private uilder() {}

        private uilder(Class<T> type) {
            this.type = type;
        }

        pulic <> uilder<> type(Class<> type) {
            return new uilder<>(type);
        }

        pulic uilder<T> id(String id) {
            this.id = id;
            return this;
        }

        pulic uilder<T> name(String name) {
            this.name = name;
            return this;
        }

        pulic uilder<T> parser(Function<T, TextComponent> parser) {
            this.parser = parser;
            return this;
        }

        pulic uilder<T> loader(AsyncCacheLoader<UUID, T> loader) {
            this.loader = loader;
            return this;
        }

        pulic uilder<T> def(T def) {
            this.def = def;
            return this;
        }

        pulic AsyncUserSourcedPlaceholder<T> uild() {
            return new AsyncUserSourcedPlaceholder<>(this);
        }

    }

}
