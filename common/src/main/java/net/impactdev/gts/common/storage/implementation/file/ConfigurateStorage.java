/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package net.impactdev.gts.common.storage.implementation.file;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage;
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.gts.api.stashes.Stash;
import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.storage.implementation.StorageImplementation;
import net.impactdev.gts.common.storage.implementation.file.loaders.ConfigurateLoader;
import net.impactdev.impactor.api.json.factory.JArray;
import net.impactdev.impactor.api.json.factory.JObject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ConfigurateStorage implements StorageImplementation {

    private final GTSPlugin plugin;
    private final String implementationName;

    // The loader responsible for I/O
    private final ConfigurateLoader loader;
    private String extension;
    private String dataDirName;
    private Map<Group, Path> fileGroups;

    private enum Group {
        USERS,
        LISTINGS,
    }

    private final LoadingCache<Path, ReentrantLock> ioLocks;

    public ConfigurateStorage(GTSPlugin plugin, String implementationName, ConfigurateLoader loader, String extension, String dataDirName) {
        this.plugin = plugin;
        this.implementationName = implementationName;
        this.loader = loader;
        this.extension = extension;
        this.dataDirName = dataDirName;

        this.ioLocks = Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(key -> new ReentrantLock());
    }

    @Override
    public GTSPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public String getName() {
        return this.implementationName;
    }

    @Override
    public void init() throws Exception {
        Path dataDir = this.getResourcePath();
        this.createDirectoriesIfNotExists(dataDir);

        Path users = dataDir.resolve(this.dataDirName).resolve("users");
        Path listings = dataDir.resolve(this.dataDirName).resolve("listings");

        EnumMap<Group, Path> fileGroups = new EnumMap<>(Group.class);
        fileGroups.put(Group.USERS, users);
        fileGroups.put(Group.LISTINGS, listings);
        this.fileGroups = ImmutableMap.copyOf(fileGroups);
    }

    protected Path getResourcePath() {
        return Paths.get("gts");
    }

    @Override
    public void shutdown() throws Exception {}

    @Override
    public boolean addListing(Listing listing) throws Exception {
        ConfigurationNode file = SimpleConfigurationNode.root();

        for(Map.Entry<String, JsonElement> entry : listing.serialize().toJson().entrySet()) {
            this.writePath(file, entry.getKey(), entry.getValue());
        }

        this.saveFile(Group.LISTINGS, listing.getID(), file);
        return true;
    }

    @Override
    public boolean deleteListing(UUID uuid) throws Exception {
        this.saveFile(Group.LISTINGS, uuid, null);
        return true;
    }

    @Override
    public Optional<Listing> getListing(UUID id) throws Exception {
        return Optional.ofNullable(this.from(this.readFile(Group.LISTINGS, id)));
    }

    @Override
    public List<Listing> getListings() throws Exception {
        List<Listing> output = Lists.newArrayList();

        Path parent = this.fileGroups.get(Group.LISTINGS);
        File[] categories = parent.toFile().listFiles(((dir, name) -> dir.isDirectory()));
        if(categories == null) {
            return output;
        }

        for(File category : categories) {
            for(File data : category.listFiles(((dir, name) -> name.endsWith(this.extension)))) {
                output.add(this.from(this.readFile(Group.LISTINGS, UUID.fromString(data.getName().split("[.]")[0]))));
            }
        }

        return output;
    }

    @Override
    public boolean hasMaxListings(UUID user) throws Exception {
        return false;
    }

    @Override
    public boolean purge() throws Exception {
        return false;
    }

    @Override
    public boolean clean() throws Exception {
        return false;
    }

    @Override
    public Stash getStash(UUID user) throws Exception {
        return Stash.builder().build();
    }

    @Override
    public Optional<PlayerSettings> getPlayerSettings(UUID user) throws Exception {
        return Optional.empty();
    }

    @Override
    public boolean applyPlayerSettings(UUID user, PlayerSettings updates) throws Exception {
        return false;
    }

    @Override
    public BuyItNowMessage.Purchase.Response processPurchase(BuyItNowMessage.Purchase.Request request) throws Exception {
        return null;
    }

    @Override
    public boolean sendListingUpdate(Listing listing) throws Exception {
        return false;
    }

    @Override
    public AuctionMessage.Bid.Response processBid(AuctionMessage.Bid.Request request) {
        return null;
    }

    @Override
    public ClaimMessage.Response processClaimRequest(ClaimMessage.Request request) throws Exception {
        return null;
    }

    @Override
    public boolean appendOldClaimStatus(UUID auction, boolean lister, boolean winner, List<UUID> others) throws Exception {
        return false;
    }

    @Override
    public AuctionMessage.Cancel.Response processAuctionCancelRequest(AuctionMessage.Cancel.Request request) throws Exception {
        return null;
    }

    @Override
    public BuyItNowMessage.Remove.Response processListingRemoveRequest(BuyItNowMessage.Remove.Request request) throws Exception {
        return null;
    }

    @Override
    public ForceDeleteMessage.Response processForcedDeletion(ForceDeleteMessage.Request request) throws Exception {
        return null;
    }

    private ConfigurationNode readFile(Group group, UUID uuid) throws IOException {
        Path target = this.fileGroups.get(group).resolve(uuid.toString().substring(0, 2)).resolve(uuid + this.extension);

        ReentrantLock lock = Objects.requireNonNull(this.ioLocks.get(target));
        lock.lock();
        try {
            return this.loader.loader(target).load();
        } finally {
            lock.unlock();
        }
    }

    private Listing from(ConfigurationNode node) {
        JObject json = new JObject();
        this.fill(json, node, true);
        JsonObject result = json.toJson();

        String type = result.get("type").getAsString();
        if(type.equals("bin")) {
            return GTSService.getInstance().getGTSComponentManager()
                    .getListingResourceManager(BuyItNow.class)
                    .get()
                    .getDeserializer()
                    .deserialize(result);
        } else {
            return GTSService.getInstance().getGTSComponentManager()
                    .getListingResourceManager(Auction.class)
                    .get()
                    .getDeserializer()
                    .deserialize(result);
        }
    }

    private void saveFile(Group group, UUID name, ConfigurationNode node) throws IOException {
        Path target = this.fileGroups.get(group).resolve(name.toString().substring(0, 2)).resolve(name + this.extension);

        this.createDirectoriesIfNotExists(target.getParent());
        ReentrantLock lock = Objects.requireNonNull(this.ioLocks.get(target));
        lock.lock();
        try {
            if(node == null) {
                Files.deleteIfExists(target);
                return;
            }

            this.loader.loader(target).save(node);
        } finally {
            lock.unlock();
        }
    }

    private void createDirectoriesIfNotExists(Path path) throws IOException {
        if (Files.exists(path) && (Files.isDirectory(path) || Files.isSymbolicLink(path))) {
            return;
        }

        Files.createDirectories(path);
    }

    private void fill(JObject target, ConfigurationNode working, boolean empty) {
        if(working.hasListChildren()) {
            JArray array = new JArray();
            for(ConfigurationNode child : working.getChildrenList()) {
                this.fillArray(array, child);
            }
            target.add(working.getKey().toString(), array);
        } else if(working.hasMapChildren()) {
            JObject child = new JObject();
            for(Map.Entry<Object, ? extends ConfigurationNode> entry : working.getChildrenMap().entrySet()) {
                this.fill(empty ? target : child, entry.getValue(), false);
            }
            if(!empty) {
                target.add(working.getKey().toString(), child);
            }
        } else {
            String key = working.getKey().toString();
            Class<?> typing = working.getValue().getClass();
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

    private void fillArray(JArray array, ConfigurationNode working) {
        if(working.hasMapChildren()) {
            JObject child = new JObject();
            for(Map.Entry<Object, ? extends ConfigurationNode> entry : working.getChildrenMap().entrySet()) {
                this.fill(child, entry.getValue(), true);
            }
            array.add(child);
        } else if(working.hasListChildren()) {
            JArray aChild = new JArray();
            for(ConfigurationNode child : working.getChildrenList()) {
                this.fillArray(array, child);
            }
            array.add(aChild);
        } else {
            Class<?> typing = working.getValue().getClass();
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

    private void writePath(ConfigurationNode parent, String key, JsonElement value) {
        if(value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            ConfigurationNode child = SimpleConfigurationNode.root();
            for(Map.Entry<String, JsonElement> path : object.entrySet()) {
                this.writePath(child, path.getKey(), path.getValue());
            }

            parent.getNode(key).setValue(child);
        } else {
            if(value.isJsonPrimitive()) {
                JsonPrimitive primitive = value.getAsJsonPrimitive();
                if(primitive.isNumber()) {
                    Number number = primitive.getAsNumber();
                    parent.getNode(key).setValue(number);
                } else if(primitive.isBoolean()) {
                    parent.getNode(key).setValue(primitive.getAsBoolean());
                } else {
                    parent.getNode(key).setValue(primitive.getAsString());
                }
            } else if(value.isJsonArray()) {
                this.writeArrayToPath(parent, key, value.getAsJsonArray());
            }
        }
    }

    private void writeArrayToPath(ConfigurationNode parent, String key, JsonArray array) {
        List<Object> output = Lists.newArrayList();
        for(JsonElement element : array) {
            if(element.isJsonObject()) {
                ConfigurationNode target = SimpleConfigurationNode.root();
                JsonObject json = element.getAsJsonObject();
                for(Map.Entry<String, JsonElement> child : json.entrySet()) {
                    this.writePath(target, child.getKey(), child.getValue());
                }
                output.add(target);
            } else if(element.isJsonArray()) {
                ConfigurationNode target = SimpleConfigurationNode.root();
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
        parent.getNode(key).setValue(output);
    }
}
