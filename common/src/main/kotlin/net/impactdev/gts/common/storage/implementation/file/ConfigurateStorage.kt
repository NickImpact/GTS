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
package net.impactdev.gts.common.storage.implementation.file

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.google.common.base.Throwables
import com.google.common.collect.ImmutableMap
import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage.Purchase
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage
import net.impactdev.gts.api.player.PlayerSettings
import net.impactdev.gts.api.stashes.Stash
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.gts.common.storage.implementation.StorageImplementation
import net.impactdev.gts.common.storage.implementation.file.loaders.ConfigurateLoader
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.SimpleConfigurationNode
import java.io.IOException
import java.nio.file.*
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Consumer
import java.util.function.Function
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.set

class ConfigurateStorage(
    override val plugin: GTSPlugin, private val implementationName: String, // The loader responsible for I/O
    private val loader: ConfigurateLoader, private val extension: String, private val dataDirName: String
) : StorageImplementation {
    private var dataDir: Path? = null
    private val fileGroups: Map<Group, FileGroup>
    private val users: FileGroup
    private override val listings: FileGroup
    private val watcher: FileWatcher?

    private enum class Group {
        USERS, LISTINGS
    }

    private class FileGroup {
        val directory: Path? = null
        val watcher: FileWatcher.WatchedLocation? = null
    }

    private val ioLocks: LoadingCache<Path, ReentrantLock>
    override val name: String?
        get() = implementationName

    @kotlin.Throws(Exception::class)
    override fun init() {
        dataDir = Paths.get("gts")
        createDirectoriesIfNotExists(dataDir)
        users.directory = dataDir.resolve("users")
        listings.directory = dataDir.resolve("listings")
        val uuidParser = label@ Function<String, UUID> { input: String? ->
            try {
                return@label UUID.fromString(input)
            } catch (e: IllegalArgumentException) {
                return@label null
            }
        }
        if (watcher != null) {
            users.watcher = watcher.getWatcher(users.directory)
            users.watcher.addListener(Consumer { path: Path ->
                val file = path.fileName.toString()
                if (!file.endsWith(this.extension)) {
                    return@addListener
                }
                val user = file.substring(0, file.length - this.extension.length)
                val id = uuidParser.apply(user) ?: return@addListener
                val name: String = GTSPlugin.Companion.getInstance().getPlayerDisplayName(id)
                plugin.pluginLogger.info("[File Watcher] Detected change in user file for $name")
            })
            listings.watcher = watcher.getWatcher(listings.directory)
            listings.watcher.addListener(Consumer { path: Path ->
                val file = path.fileName.toString()
                if (!file.endsWith(this.extension)) {
                    return@addListener
                }
                val user = file.substring(0, file.length - this.extension.length)
                val id = uuidParser.apply(user) ?: return@addListener
                plugin.pluginLogger.info("[File Watcher] Detected change in listing file with ID: $id")
            })
        }
    }

    @kotlin.Throws(Exception::class)
    override fun shutdown() {
    }

    @kotlin.Throws(Exception::class)
    override fun addListing(listing: Listing?): Boolean {
        try {
            val file: ConfigurationNode = SimpleConfigurationNode.root()
            file.getNode("data").value = listing!!.serialize()!!.toJson()
        } catch (e: Exception) {
            return false
        }
        return false
    }

    @kotlin.Throws(Exception::class)
    override fun deleteListing(uuid: UUID?): Boolean {
        return false
    }

    @kotlin.Throws(Exception::class)
    override fun getListing(id: UUID?): Optional<Listing> {
        return Optional.empty()
    }

    @kotlin.Throws(Exception::class)
    override fun getListings(): List<Listing?>? {
        return null
    }

    @kotlin.Throws(Exception::class)
    override fun hasMaxListings(user: UUID?): Boolean {
        return false
    }

    @kotlin.Throws(Exception::class)
    override fun purge(): Boolean {
        return false
    }

    @kotlin.Throws(Exception::class)
    override fun clean(): Boolean {
        return false
    }

    @kotlin.Throws(Exception::class)
    override fun getStash(user: UUID?): Stash? {
        return null
    }

    @kotlin.Throws(Exception::class)
    override fun getPlayerSettings(user: UUID?): Optional<PlayerSettings> {
        return Optional.empty()
    }

    @kotlin.Throws(Exception::class)
    override fun applyPlayerSettings(user: UUID?, updates: PlayerSettings?): Boolean {
        return false
    }

    @kotlin.Throws(Exception::class)
    override fun processPurchase(request: Purchase.Request?): Purchase.Response? {
        return null
    }

    @kotlin.Throws(Exception::class)
    override fun sendListingUpdate(listing: Listing): Boolean {
        return false
    }

    override fun processBid(request: AuctionMessage.Bid.Request?): AuctionMessage.Bid.Response? {
        return null
    }

    @kotlin.Throws(Exception::class)
    override fun processClaimRequest(request: ClaimMessage.Request?): ClaimMessage.Response? {
        return null
    }

    @kotlin.Throws(Exception::class)
    override fun appendOldClaimStatus(auction: UUID?, lister: Boolean, winner: Boolean, others: List<UUID?>?): Boolean {
        return false
    }

    @kotlin.Throws(Exception::class)
    override fun processAuctionCancelRequest(request: AuctionMessage.Cancel.Request?): AuctionMessage.Cancel.Response? {
        return null
    }

    @kotlin.Throws(Exception::class)
    override fun processListingRemoveRequest(request: BuyItNowMessage.Remove.Request?): BuyItNowMessage.Remove.Response? {
        return null
    }

    @kotlin.Throws(Exception::class)
    override fun processForcedDeletion(request: ForceDeleteMessage.Request?): ForceDeleteMessage.Response? {
        return null
    }

    @kotlin.Throws(IOException::class)
    private fun readFile(uuid: UUID): ConfigurationNode? {
        //Path file = this.getDirectory()
        return null
    }

    @kotlin.Throws(IOException::class)
    private fun saveFile(name: String, node: ConfigurationNode) {
    }

    @kotlin.Throws(IOException::class)
    private fun createDirectoriesIfNotExists(path: Path?) {
        if (Files.exists(path) && (Files.isDirectory(path) || Files.isSymbolicLink(path))) {
            return
        }
        Files.createDirectories(path)
    }

    // used to report i/o exceptions which took place in a specific file
    @kotlin.Throws(RuntimeException::class)
    private fun reportException(file: String, ex: Exception): RuntimeException {
        plugin.pluginLogger.warn("Exception thrown whilst performing i/o: $file")
        ex.printStackTrace()
        throw Throwables.propagate(ex)
    }

    init {
        users = FileGroup()
        listings = FileGroup()
        val fileGroups = EnumMap<Group, FileGroup>(
            Group::class.java
        )
        fileGroups[Group.USERS] = users
        fileGroups[Group.LISTINGS] = listings
        this.fileGroups = ImmutableMap.copyOf(fileGroups)
        val watcher: FileWatcher?
        watcher = try {
            FileWatcher(Paths.get("gts"), true)
        } catch (e: Throwable) {
            GTSPlugin.Companion.getInstance().getPluginLogger()
                .error("Error occurred whilst trying to create a file watcher...")
            ExceptionWriter.write(e)
            null
        }
        this.watcher = watcher
        ioLocks = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build { key: Path? -> ReentrantLock() }
    }
}