package net.impactdev.gts.storage.implementation.file;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.impactdev.gts.api.elements.listings.Listing;
import net.impactdev.gts.api.elements.listings.models.Auction;
import net.impactdev.gts.api.elements.listings.models.BuyItNow;
import net.impactdev.gts.api.registries.components.ComponentDeserializerRegistry;
import net.impactdev.gts.elements.listings.models.GTSBuyItNow;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.gts.registries.RegistryAccessors;
import net.impactdev.gts.storage.implementation.GTSStorageImplementation;
import net.impactdev.impactor.api.storage.connection.configurate.ConfigurateLoader;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;
import net.impactdev.json.JArray;
import net.impactdev.json.JElement;
import net.impactdev.json.JObject;
import net.kyori.adventure.key.Key;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ConfigurateStorage implements GTSStorageImplementation {

    private final GTSPlugin plugin;
    private final ConfigurateLoader loader;
    private final Path root;
    private final String extension;

    private final Map<Group, Path> components = Maps.newHashMap();
    private final LoadingCache<Path, ReentrantLock> locks = Caffeine.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .build(ignore -> new ReentrantLock());

    public ConfigurateStorage(GTSPlugin plugin, ConfigurateLoader loader, Path root) {
        this.plugin = plugin;
        this.loader = loader;
        this.root = root;

        this.extension = "." + this.loader.name().toLowerCase(Locale.US);
    }

    @Override
    public void init() throws Exception {
        Files.createDirectories(this.root);

        this.components.put(Group.Listings, this.root.resolve("listings"));
        this.components.put(Group.Bazaar, this.root.resolve("bazaar"));
        this.components.put(Group.Players, this.root.resolve("players"));

        for(Path target : this.components.values()) {
            Files.createDirectories(target);
        }
    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void meta(PrettyPrinter printer) {

    }

    @Override
    public List<Listing> listings() throws Exception {
        Path root = this.components.get(Group.Listings);
        try (Stream<Path> listings = Files.walk(root)) {
            return listings.filter(path -> path.getFileName().toString().endsWith(this.extension))
                    .map(path -> {
                        try {
                            ConfigurationNode node = this.readFile(path);
                            JsonObject json = this.toJson(node);

                            ComponentDeserializerRegistry deserializers = RegistryAccessors.DESERIALIZERS;
                            return deserializers.<Listing>deserialize(Key.key(json.get("key").getAsString()), json);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void publishListing(Listing listing) throws Exception {
        Path root = this.components.get(Group.Listings);
        UUID id = listing.id();
        Files.createDirectories(root = root.resolve(id.toString().substring(0, 2)));

        this.writeFile(
                root.resolve(listing.id().toString() + this.extension),
                listing.serialize()
        );
    }

    @Override
    public void deleteListing(UUID uuid) throws Exception {

    }

    private enum Group {

        Listings,
        Bazaar,
        Players,

    }

    private ConfigurationNode readFile(Path target) throws IOException {
        ReentrantLock lock = Objects.requireNonNull(this.locks.get(target));
        lock.lock();
        try {
            if(!target.toFile().exists()) {
                return null;
            }

            return this.loader.loader(target).load();
        } finally {
            lock.unlock();
        }
    }

    private void writeFile(Path target, JsonObject json) throws Exception {
        ReentrantLock lock = Objects.requireNonNull(this.locks.get(target));
        lock.lock();
        try {
            if(json == null) {
                Files.deleteIfExists(target);
            }

            ConfigurationNode node = BasicConfigurationNode.root();
            this.write(target, node, json);
        } finally {
            lock.unlock();
        }
    }
    
    private JsonObject toJson(ConfigurationNode node) throws Exception {
        JObject json = new JObject();
        this.fill(json, node, true);
        return json.toJson();
    }

    private void fill(JObject target, ConfigurationNode working, boolean empty) throws Exception {
        if(working.isList()) {
            JArray array = new JArray();
            for(ConfigurationNode child : working.childrenList()) {
                this.fillArray(array, child);
            }
            target.add(working.key().toString(), array);
        } else if(working.isMap()) {
            JObject child = new JObject();
            for(Map.Entry<Object, ? extends ConfigurationNode> entry : working.childrenMap().entrySet()) {
                this.fill(empty ? target : child, entry.getValue(), false);
            }
            if(!empty) {
                target.add(working.key().toString(), child);
            }
        } else {
            String key = working.key().toString();
            Class<?> typing = working.get(Object.class).getClass();
            if(typing.equals(String.class)) {
                target.add(key, working.getString());
            } else if(Number.class.isAssignableFrom(typing)) {
                double value = working.getDouble();
                target.add(key, value);
            } else if(typing.equals(Boolean.class)) {
                target.add(key, working.getBoolean());
            } else {
                throw new IllegalStateException("Invalid value for location: " + typing);
            }
        }
    }

    private void fillArray(JArray array, ConfigurationNode working) throws Exception {
        if(!working.childrenMap().isEmpty()) {
            JObject child = new JObject();
            for(Map.Entry<Object, ? extends ConfigurationNode> entry : working.childrenMap().entrySet()) {
                this.fill(child, entry.getValue(), false);
            }
            array.add(child);
        } else if(!working.childrenList().isEmpty()) {
            JArray aChild = new JArray();
            for(ConfigurationNode child : working.childrenList()) {
                this.fillArray(array, child);
            }
            array.add(aChild);
        } else {
            Class<?> typing = working.get(Object.class).getClass();
            if(typing.equals(String.class)) {
                array.add(working.getString());
            } else if(Number.class.isAssignableFrom(typing)) {
                double value = working.getDouble();
                array.add(value);
            } else {
                throw new IllegalStateException("Invalid value for location: " + typing);
            }
        }
    }

    private void write(Path target, ConfigurationNode node, JsonObject json) throws Exception {
        for(Map.Entry<String, JsonElement> elements : json.entrySet()) {
            this.writePath(node, elements.getKey(), elements.getValue());
        }

        this.loader.loader(target).save(node);
    }

    private void writePath(ConfigurationNode parent, String key, JsonElement value) throws Exception {
        if(value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            ConfigurationNode child = BasicConfigurationNode.root();
            for(Map.Entry<String, JsonElement> path : object.entrySet()) {
                this.writePath(child, path.getKey(), path.getValue());
            }

            if(child.virtual()) {
                child.set(Maps.newHashMap());
            }

            parent.node(key).set(child);
        } else {
            if(value.isJsonPrimitive()) {
                JsonPrimitive primitive = value.getAsJsonPrimitive();
                if(primitive.isNumber()) {
                    Number number = primitive.getAsNumber();
                    parent.node(key).set(number);
                } else if(primitive.isBoolean()) {
                    parent.node(key).set(primitive.getAsBoolean());
                } else {
                    parent.node(key).set(primitive.getAsString());
                }
            } else if(value.isJsonArray()) {
                this.writeArrayToPath(parent, key, value.getAsJsonArray());
            }
        }
    }

    private void writeArrayToPath(ConfigurationNode parent, String key, JsonArray array) throws Exception {
        List<Object> output = Lists.newArrayList();
        for(JsonElement element : array) {
            if(element.isJsonObject()) {
                ConfigurationNode target = BasicConfigurationNode.root();
                JsonObject json = element.getAsJsonObject();
                for(Map.Entry<String, JsonElement> child : json.entrySet()) {
                    this.writePath(target, child.getKey(), child.getValue());
                }
                output.add(target);
            } else if(element.isJsonArray()) {
                ConfigurationNode target = BasicConfigurationNode.root();
                this.writeArrayToPath(target, key, element.getAsJsonArray());
                output.add(target);
            } else {
                if(element.isJsonPrimitive()) {
                    JsonPrimitive primitive = element.getAsJsonPrimitive();
                    if(primitive.isNumber()) {
                        Number number = primitive.getAsNumber();
                        output.add(number);
                    } else if(primitive.isBoolean()) {
                        output.add(primitive.getAsBoolean());
                    } else {
                        output.add(primitive.getAsString());
                    }
                }
            }
        }

        parent.node(key).set(output);
    }

}
