package net.impactdev.gts.placeholders.parsers.concurrent;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.impactdev.gts.api.event.factory.GTSEventFactory;
import net.impactdev.gts.placeholders.parsers.SourceSpecificPlaceholderParser;
import net.impactdev.gts.api.events.placeholders.PlaceholderReadyEvent;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.placeholder.PlaceholderContext;
import org.spongepowered.api.placeholder.PlaceholderParser;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class AsyncUserSourcedPlaceholder<T> extends SourceSpecificPlaceholderParser<T> implements PlaceholderParser {

    private final ConcurrentHashMap<UUID, Boolean> initialized = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, T> last = new ConcurrentHashMap<>();
    private final AsyncLoadingCache<UUID, T> cache;
    private final T def;

    private AsyncUserSourcedPlaceholder(Builder<T> builder) {
        super(builder.type, builder.id, builder.parser);

        this.def = builder.def;
        this.cache = Caffeine.newBuilder()
                .executor(Impactor.getInstance().getScheduler().async())
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .buildAsync(builder.loader);
    }

    @Override
    public Component parse(PlaceholderContext context) {
        UUID user = context.associatedObject()
                .filter(source -> UUID.class.isAssignableFrom(source.getClass()) || Player.class.isAssignableFrom(source.getClass()))
                .map(source -> {
                    if(Player.class.isAssignableFrom(source.getClass())) {
                        return ((Player) source).uniqueId();
                    } else {
                        return (UUID) source;
                    }
                })
                .orElse(null);

        AtomicReference<Optional<Component>> fallback = new AtomicReference<>(Optional.empty());
        Optional<String> arguments = context.argumentString();
        if(arguments.isPresent()) {
            String[] args = arguments.get().split(";");
            for(String arg : args) {
                String[] focus = arg.split("=");
                if(focus.length > 1) {
                    if(focus[0].equalsIgnoreCase("fallback")) {
                        fallback.set(Optional.of(LegacyComponentSerializer.legacyAmpersand().deserialize(focus[1])));
                    }
                }
            }
        }

        Component out = context.associatedObject()
                .filter(source -> UUID.class.isAssignableFrom(source.getClass()) || Player.class.isAssignableFrom(source.getClass()))
                .map(source -> {
                    if(Player.class.isAssignableFrom(source.getClass())) {
                        return ((Player) source).uniqueId();
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
                            Impactor.getInstance().getEventBus().post(GTSEventFactory.createPlaceholderReadyEvent(this.key(), source, value));
                        });
                    }
                    return result;
                })
                .map(this.getParser())
                .orElse(Component.empty());

        if(out.equals(Component.empty())) {
            if(user != null && fallback.get().isPresent()) {
                Boolean result = this.initialized.get(user);
                if(result != null && result) {
                    return this.getParser().apply(this.last.get(user));
                } else {
                    this.initialized.put(user, true);
                    return fallback.get().get();
                }
            } else {
                return this.getParser().apply(this.def);
            }
        }

        return out;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {

        private Class<T> type;
        private String id;
        private String name;
        private Function<T, Component> parser;

        private AsyncCacheLoader<UUID, T> loader;
        private T def;

        private Builder() {}

        private Builder(Class<T> type) {
            this.type = type;
        }

        public <B> Builder<B> type(Class<B> type) {
            return new Builder<>(type);
        }

        public Builder<T> id(String id) {
            this.id = id;
            return this;
        }

        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> parser(Function<T, Component> parser) {
            this.parser = parser;
            return this;
        }

        public Builder<T> loader(AsyncCacheLoader<UUID, T> loader) {
            this.loader = loader;
            return this;
        }

        public Builder<T> def(T def) {
            this.def = def;
            return this;
        }

        public AsyncUserSourcedPlaceholder<T> build() {
            return new AsyncUserSourcedPlaceholder<>(this);
        }

    }

}
