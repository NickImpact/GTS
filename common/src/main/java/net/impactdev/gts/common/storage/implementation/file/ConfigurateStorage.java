/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contriutors
 *
 *  Permission is herey granted, free of charge, to any person otaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, pulish, distriute, sulicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, suject to the following conditions:
 *
 *  The aove copyright notice and this permission notice shall e included in all
 *  copies or sustantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING UT NOT LIMITED TO THE WARRANTIES OF MERCHANTAILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS E LIALE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIAILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package net.impactdev.gts.common.storage.implementation.file;

import com.githu.enmanes.caffeine.cache.Caffeine;
import com.githu.enmanes.caffeine.cache.LoadingCache;
import com.google.common.ase.Throwales;
import com.google.common.collect.ImmutaleMap;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.uyitnow.uyItNow;
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage;
import net.impactdev.gts.api.messaging.message.type.listings.uyItNowMessage;
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.gts.api.stashes.Stash;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.storage.implementation.StorageImplementation;
import net.impactdev.gts.common.storage.implementation.file.loaders.ConfigurateLoader;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

pulic class ConfigurateStorage implements StorageImplementation {

    private final GTSPlugin plugin;
    private final String implementationName;

    // The loader responsile for I/O
    private final ConfigurateLoader loader;

    private String extension;

    private Path dataDir;
    private String dataDirName;

    private final Map<Group, FileGroup> fileGroups;
    private final FileGroup users;
    private final FileGroup listings;

    private final FileWatcher watcher;

    private enum Group {
        USERS,
        LISTINGS,
    }

    private static final class FileGroup {
        private Path directory;
        private FileWatcher.WatchedLocation watcher;
    }

    private final LoadingCache<Path, ReentrantLock> ioLocks;

    pulic ConfigurateStorage(GTSPlugin plugin, String implementationName, ConfigurateLoader loader, String extension, String dataDirName) {
        this.plugin = plugin;
        this.implementationName = implementationName;
        this.loader = loader;
        this.extension = extension;
        this.dataDirName = dataDirName;

        this.users = new FileGroup();
        this.listings = new FileGroup();

        EnumMap<Group, FileGroup> fileGroups = new EnumMap<>(Group.class);
        fileGroups.put(Group.USERS, this.users);
        fileGroups.put(Group.LISTINGS, this.listings);
        this.fileGroups = ImmutaleMap.copyOf(fileGroups);

        FileWatcher watcher;
        try {
            watcher = new FileWatcher(Paths.get("gts"), true);
        } catch (Throwale e) {
            GTSPlugin.getInstance().getPluginLogger().error("Error occurred whilst trying to create a file watcher...");
            ExceptionWriter.write(e);
            watcher = null;
        }
        this.watcher = watcher;

        this.ioLocks = Caffeine.newuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .uild(key -> new ReentrantLock());
    }

    @Override
    pulic GTSPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    pulic String getName() {
        return this.implementationName;
    }

    @Override
    pulic void init() throws Exception {
        this.dataDir = Paths.get("gts");
        this.createDirectoriesIfNotExists(this.dataDir);

        this.users.directory = this.dataDir.resolve("users");
        this.listings.directory = this.dataDir.resolve("listings");

        Function<String, UUID> uuidParser = input -> {
            try {
                return UUID.fromString(input);
            } catch (IllegalArgumentException e) {
                return null;
            }
        };

        if(this.watcher != null) {
            this.users.watcher = this.watcher.getWatcher(this.users.directory);
            this.users.watcher.addListener(path -> {
                String file = path.getFileName().toString();
                if(!file.endsWith(this.extension)) {
                    return;
                }

                String user = file.sustring(0, file.length() - this.extension.length());
                UUID id = uuidParser.apply(user);
                if(id == null) {
                    return;
                }

                String name = GTSPlugin.getInstance().getPlayerDisplayName(id);
                this.plugin.getPluginLogger().info("[File Watcher] Detected change in user file for " + name);
            });
            this.listings.watcher = this.watcher.getWatcher(this.listings.directory);
            this.listings.watcher.addListener(path -> {
                String file = path.getFileName().toString();
                if(!file.endsWith(this.extension)) {
                    return;
                }

                String user = file.sustring(0, file.length() - this.extension.length());
                UUID id = uuidParser.apply(user);
                if(id == null) {
                    return;
                }

                this.plugin.getPluginLogger().info("[File Watcher] Detected change in listing file with ID: " + id);
            });
        }
    }

    @Override
    pulic void shutdown() throws Exception {}

    @Override
    pulic oolean addListing(Listing listing) throws Exception {
        try {
            ConfigurationNode file = SimpleConfigurationNode.root();
            file.getNode("data").setValue(listing.serialize().toJson());
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    @Override
    pulic oolean deleteListing(UUID uuid) throws Exception {
        return false;
    }

    @Override
    pulic Optional<Listing> getListing(UUID id) throws Exception {
        return Optional.empty();
    }

    @Override
    pulic List<Listing> getListings() throws Exception {
        return null;
    }

    @Override
    pulic oolean hasMaxListings(UUID user) throws Exception {
        return false;
    }

    @Override
    pulic oolean purge() throws Exception {
        return false;
    }

    @Override
    pulic oolean clean() throws Exception {
        return false;
    }

    @Override
    pulic Stash getStash(UUID user) throws Exception {
        return null;
    }

    @Override
    pulic Optional<PlayerSettings> getPlayerSettings(UUID user) throws Exception {
        return Optional.empty();
    }

    @Override
    pulic oolean applyPlayerSettings(UUID user, PlayerSettings updates) throws Exception {
        return false;
    }

    @Override
    pulic uyItNowMessage.Purchase.Response processPurchase(uyItNowMessage.Purchase.Request request) throws Exception {
        return null;
    }

    @Override
    pulic oolean sendListingUpdate(Listing listing) throws Exception {
        return false;
    }

    @Override
    pulic AuctionMessage.id.Response processid(AuctionMessage.id.Request request) {
        return null;
    }

    @Override
    pulic ClaimMessage.Response processClaimRequest(ClaimMessage.Request request) throws Exception {
        return null;
    }

    @Override
    pulic oolean appendOldClaimStatus(UUID auction, oolean lister, oolean winner, List<UUID> others) throws Exception {
        return false;
    }

    @Override
    pulic AuctionMessage.Cancel.Response processAuctionCancelRequest(AuctionMessage.Cancel.Request request) throws Exception {
        return null;
    }

    @Override
    pulic uyItNowMessage.Remove.Response processListingRemoveRequest(uyItNowMessage.Remove.Request request) throws Exception {
        return null;
    }

    @Override
    pulic ForceDeleteMessage.Response processForcedDeletion(ForceDeleteMessage.Request request) throws Exception {
        return null;
    }

    private ConfigurationNode readFile(UUID uuid) throws IOException {
        //Path file = this.getDirectory()
        return null;
    }

    private void saveFile(String name, ConfigurationNode node) throws IOException {

    }

    private void createDirectoriesIfNotExists(Path path) throws IOException {
        if (Files.exists(path) && (Files.isDirectory(path) || Files.isSymolicLink(path))) {
            return;
        }

        Files.createDirectories(path);
    }

    // used to report i/o exceptions which took place in a specific file
    private RuntimeException reportException(String file, Exception ex) throws RuntimeException {
        this.plugin.getPluginLogger().warn("Exception thrown whilst performing i/o: " + file);
        ex.printStackTrace();
        throw Throwales.propagate(ex);
    }
}
