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
package net.impactdev.gts.common.storage.implementation.sql

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.TreeMultimap
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import net.impactdev.gts.api.GTSService.Companion.instance
import net.impactdev.gts.api.data.ResourceManager
import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.listings.auctions.Auction
import net.impactdev.gts.api.listings.buyitnow.BuyItNow
import net.impactdev.gts.api.listings.entries.Entry
import net.impactdev.gts.api.listings.prices.Price
import net.impactdev.gts.api.messaging.message.errors.ErrorCode
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage
import net.impactdev.gts.api.messaging.message.type.auctions.AuctionMessage
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage
import net.impactdev.gts.api.messaging.message.type.listings.BuyItNowMessage.Purchase
import net.impactdev.gts.api.messaging.message.type.listings.ClaimMessage
import net.impactdev.gts.api.player.NotificationSetting
import net.impactdev.gts.api.player.PlayerSettings
import net.impactdev.gts.api.player.PlayerSettings.Companion.create
import net.impactdev.gts.api.stashes.Stash
import net.impactdev.gts.api.util.PrettyPrinter
import net.impactdev.gts.api.util.TriState
import net.impactdev.gts.api.util.groupings.SimilarPair
import net.impactdev.gts.common.config.ConfigKeys
import net.impactdev.gts.common.messaging.messages.listings.ClaimMessageImpl.ClaimResponseImpl
import net.impactdev.gts.common.messaging.messages.listings.ClaimMessageImpl.ClaimResponseImpl.ClaimResponseBuilder
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionBidMessage
import net.impactdev.gts.common.messaging.messages.listings.auctions.impl.AuctionCancelMessage
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.purchase.BINPurchaseMessage
import net.impactdev.gts.common.messaging.messages.listings.buyitnow.removal.BINRemoveMessage
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.gts.common.storage.implementation.StorageImplementation
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter
import net.impactdev.impactor.api.json.factory.JObject
import net.impactdev.impactor.api.storage.sql.ConnectionFactory
import net.impactdev.impactor.api.utilities.mappings.Tuple
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.sql.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.function.*
import java.util.function.Function
import java.util.stream.Collectors
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.set

class SqlImplementation(
    override val plugin: GTSPlugin,
    val connectionFactory: ConnectionFactory,
    tablePrefix: String?
) : StorageImplementation {
    val statementProcessor: Function<String, String>
    override val name: String?
        get() = connectionFactory.implementationName

    @kotlin.Throws(Exception::class)
    override fun init() {
        connectionFactory.init()
        val schemaFileName = "assets/gts/schema/" + connectionFactory.implementationName.toLowerCase() + ".sql"
        plugin.getResourceStream(schemaFileName).use { `is` ->
            if (`is` == null) {
                throw Exception("Couldn't locate schema file for " + connectionFactory.implementationName)
            }
            BufferedReader(InputStreamReader(`is`, StandardCharsets.UTF_8)).use { reader ->
                connectionFactory.connection.use { connection ->
                    connection.createStatement().use { s ->
                        var sb = StringBuilder()
                        var line: String
                        while (reader.readLine().also { line = it } != null) {
                            if (line.startsWith("--") || line.startsWith("#")) continue
                            sb.append(line)

                            // check for end of declaration
                            if (line.endsWith(";")) {
                                sb.deleteCharAt(sb.length - 1)
                                val result = statementProcessor.apply(sb.toString().trim { it <= ' ' })
                                if (!result.isEmpty()) {
                                    if (result.startsWith("set mode")) {
                                        s.addBatch(result)
                                    } else {
                                        if (SchemaReaders.any(this, result)) {
                                            SchemaReaders.first(this, result, s)
                                        }
                                    }
                                }

                                // reset
                                sb = StringBuilder()
                            }
                        }
                        s.executeBatch()
                    }
                }
            }
        }
    }

    @kotlin.Throws(Exception::class)
    override fun shutdown() {
        connectionFactory.shutdown()
    }

    override val meta: Map<String?, String?>?
        get() = connectionFactory.meta

    @FunctionalInterface
    private interface SQLPrepared<T> {
        @kotlin.Throws(Exception::class)
        fun prepare(connection: Connection?, ps: PreparedStatement?): T
    }

    @kotlin.Throws(Exception::class)
    private fun <T> query(key: String, action: SQLPrepared<T>): T {
        connectionFactory.connection.use { connection ->
            connection.prepareStatement(statementProcessor.apply(key))
                .use { ps -> return action.prepare(connection, ps) }
        }
    }

    @FunctionalInterface
    private interface SQLResults<T> {
        @kotlin.Throws(Exception::class)
        fun results(rs: ResultSet?): T
    }

    @kotlin.Throws(Exception::class)
    private fun <T> results(ps: PreparedStatement, action: SQLResults<T>): T {
        ps.executeQuery().use { rs -> return action.results(rs) }
    }

    @kotlin.Throws(Exception::class)
    override fun addListing(listing: Listing?): Boolean {
        return query(ADD_LISTING, SQLPrepared<Boolean> { connection: Connection?, ps: PreparedStatement ->
            ps.setString(1, listing!!.iD.toString())
            ps.setString(2, listing.lister.toString())
            ps.setString(3, plugin.gson.toJson(listing.serialize()!!.toJson()))
            ps.executeUpdate()
            true
        })
    }

    @kotlin.Throws(Exception::class)
    override fun deleteListing(uuid: UUID?): Boolean {
        return query(DELETE_LISTING, SQLPrepared<Boolean> { connection: Connection?, ps: PreparedStatement ->
            ps.setString(1, uuid.toString())
            ps.executeUpdate() != 0
        })
    }

    @kotlin.Throws(Exception::class)
    override fun getListing(id: UUID?): Optional<Listing> {
        return query(
            GET_SPECIFIC_LISTING,
            SQLPrepared<Optional<Listing>> { connection: Connection?, ps: PreparedStatement ->
                ps.setString(1, id.toString())
                Optional.ofNullable(results<Any>(ps, SQLResults<Any> { results: ResultSet ->
                    if (results.next()) {
                        val json: JsonObject = GTSPlugin.Companion.getInstance().getGson()
                            .fromJson(results.getString("listing"), JsonObject::class.java)
                        if (!json.has("type")) {
                            throw JsonParseException("Invalid Listing: Missing type")
                        }
                        val type = json["type"].asString
                        if (type == "bin") {
                            return@results instance!!.gTSComponentManager
                                .getListingResourceManager(BuyItNow::class.java)
                                .get()
                                .deserializer
                                .deserialize(json)
                        } else {
                            return@results instance!!.gTSComponentManager
                                .getListingResourceManager(Auction::class.java)
                                .get()
                                .deserializer
                                .deserialize(json)
                        }
                    }
                    null
                }))
            })
    }

    @get:Throws(Exception::class)
    override val listings: List<Listing?>?
        get() {
            translateLegacy()
            return query<List<Listing?>>(
                SELECT_ALL_LISTINGS,
                SQLPrepared<List<Listing>> { connection: Connection?, ps: PreparedStatement ->
                    results<List<Listing?>>(ps, SQLResults<List<Listing>> { results: ResultSet ->
                        val entries: MutableList<Listing> = Lists.newArrayList()
                        var failed = 0
                        while (results.next()) {
                            try {
                                val json: JsonObject = GTSPlugin.Companion.getInstance().getGson()
                                    .fromJson(results.getString("listing"), JsonObject::class.java)
                                if (!json.has("type")) {
                                    throw JsonParseException("Invalid Listing: Missing type")
                                }
                                val type = json["type"].asString
                                if (type == "bin") {
                                    val bin = instance!!.gTSComponentManager
                                        .getListingResourceManager(BuyItNow::class.java)
                                        .get()
                                        .deserializer
                                        .deserialize(json)
                                    entries.add(bin)
                                } else {
                                    val auction = instance!!.gTSComponentManager
                                        .getListingResourceManager(Auction::class.java)
                                        .get()
                                        .deserializer
                                        .deserialize(json)
                                    entries.add(auction)
                                }
                            } catch (e: Exception) {
                                plugin.pluginLogger.error("Unable to read listing with ID: " + results.getString("id"))
                                ExceptionWriter.write(e)
                                ++failed
                            }
                        }
                        if (failed != 0) {
                            plugin.pluginLogger.error("Failed to read in &c$failed &7listings...")
                        }
                        entries
                    })
                })
        }

    @kotlin.Throws(Exception::class)
    override fun hasMaxListings(user: UUID?): Boolean {
        return query(GET_ALL_USER_LISTINGS, SQLPrepared<Boolean> { connection: Connection, ps: PreparedStatement ->
            ps.setString(1, user.toString())
            results<Boolean>(ps, SQLResults<Boolean> { results: ResultSet ->
                val possesses = AtomicInteger()
                while (results.next()) {
                    connection.prepareStatement(statementProcessor.apply(GET_AUCTION_CLAIM_STATUS)).use { query ->
                        query.setString(1, results.getString("id"))
                        results<Any>(query, SQLResults<Any> { r: ResultSet ->
                            if (r.next()) {
                                if (!r.getBoolean("lister")) {
                                    possesses.getAndIncrement()
                                }
                            } else {
                                possesses.getAndIncrement()
                            }
                            null
                        })
                    }
                }
                possesses.get() >= GTSPlugin.Companion.getInstance().getConfiguration()
                    .get(ConfigKeys.MAX_LISTINGS_PER_USER)
            })
        })
    }

    @kotlin.Throws(Exception::class)
    override fun purge(): Boolean {
        return false
    }

    @kotlin.Throws(Exception::class)
    override fun clean(): Boolean {
        if (tableExists(statementProcessor.apply("{prefix}listings_v3"))) {
            connectionFactory.connection.use { connection ->
                val statement = connection.createStatement()
                statement.executeUpdate(statementProcessor.apply("DROP TABLE {prefix}listings_v3"))
            }
            return true
        }
        return false
    }

    @kotlin.Throws(Exception::class)
    override fun getStash(user: UUID?): Stash? {
        val builder = Stash.builder()
        val listings = listings
        for (listing in listings!!) {
            if (listing!!.hasExpired() || listing is BuyItNow && listing.isPurchased) {
                if (listing is Auction) {
                    val auction = listing
                    if (auction.lister == user || auction.highBid!!.map { bid: Tuple<UUID?, Auction.Bid?>? -> bid!!.first == user }
                            .orElse(false)) {
                        val state = auction.lister == user
                        val claimed = query<SimilarPair<Boolean>>(
                            GET_AUCTION_CLAIM_STATUS,
                            SQLPrepared<SimilarPair<Boolean>> { connection: Connection?, ps: PreparedStatement ->
                                ps.setString(1, auction.iD.toString())
                                results<SimilarPair<Boolean?>>(
                                    ps,
                                    SQLResults<SimilarPair<Boolean>> { results: ResultSet ->
                                        if (results.next()) {
                                            return@results SimilarPair(
                                                results.getBoolean("lister"),
                                                results.getBoolean("winner")
                                            )
                                        } else {
                                            return@results SimilarPair(false, false)
                                        }
                                    })
                            })
                        if (state && !claimed.first) {
                            builder!!.append(auction, TriState.FALSE)
                        } else if (!state && !claimed.second) {
                            builder!!.append(auction, TriState.TRUE)
                        }
                    } else if (auction.bids.containsKey(user)) {
                        val result = query<Boolean>(
                            GET_AUCTION_CLAIM_STATUS,
                            SQLPrepared<Boolean> { connection: Connection?, ps: PreparedStatement ->
                                ps.setString(1, auction.iD.toString())
                                results<Boolean>(ps, SQLResults<Boolean> { results: ResultSet ->
                                    if (results.next()) {
                                        val others: List<UUID?> = GTSPlugin.Companion.getInstance().getGson().fromJson(
                                            results.getString("others"),
                                            object : TypeToken<List<UUID?>?>() {}.type
                                        )
                                        return@results Optional.ofNullable(others).orElse(Lists.newArrayList())
                                            .contains(user)
                                    }
                                    false
                                })
                            })
                        if (!result) {
                            builder!!.append(auction, TriState.UNDEFINED)
                        }
                    }
                } else {
                    val bin = listing as BuyItNow?
                    if (bin!!.lister == user && !bin.stashedForPurchaser()) {
                        builder!!.append(bin, TriState.FALSE)
                    } else if (bin.stashedForPurchaser()) {
                        if (bin.purchaser() == user) {
                            builder!!.append(bin, TriState.TRUE)
                        }
                    }
                }
            }
        }
        return builder!!.build()
    }

    @kotlin.Throws(Exception::class)
    override fun getPlayerSettings(user: UUID?): Optional<PlayerSettings> {
        return query(
            GET_PLAYER_SETTINGS,
            SQLPrepared<Optional<PlayerSettings>> { connection: Connection?, ps: PreparedStatement ->
                ps.setString(1, user.toString())
                results<Optional<PlayerSettings?>>(ps, SQLResults<Optional<PlayerSettings>> { results: ResultSet ->
                    if (results.next()) {
                        val settings = PlayerSettings.builder()
                            .set(NotificationSetting.Publish, results.getBoolean("pub_notif"))
                            .set(NotificationSetting.Sold, results.getBoolean("sell_notif"))
                            .set(NotificationSetting.Bid, results.getBoolean("bid_notif"))
                            .set(NotificationSetting.Outbid, results.getBoolean("outbid_notif"))
                            .build()
                        return@results Optional.of(settings!!)
                    }
                    applyPlayerSettings(user, create())
                    Optional.empty()
                })
            })
    }

    @kotlin.Throws(Exception::class)
    override fun applyPlayerSettings(user: UUID?, updates: PlayerSettings?): Boolean {
        return query(APPLY_PLAYER_SETTINGS, SQLPrepared<Boolean> { connection: Connection?, ps: PreparedStatement ->
            ps.setString(1, user.toString())
            ps.setBoolean(2, updates!!.publishListenState)
            ps.setBoolean(3, updates.soldListenState)
            ps.setBoolean(4, updates.bidListenState)
            ps.setBoolean(5, updates.outbidListenState)
            ps.executeUpdate()
            true // Ignore return value of executeUpdate() as this can possibly end up being 0
        })
    }

    @kotlin.Throws(Exception::class)
    override fun processPurchase(request: Purchase.Request?): Purchase.Response? {
        return query<BINPurchaseMessage.Response>(
            GET_SPECIFIC_LISTING,
            SQLPrepared<BINPurchaseMessage.Response> { connection: Connection?, ps: PreparedStatement ->
                ps.setString(1, request!!.listingID.toString())
                results<BINPurchaseMessage.Response>(ps, SQLResults<BINPurchaseMessage.Response> { results: ResultSet ->
                    var successful = results.next()
                    var seller = Listing.SERVER_ID
                    var listing: BuyItNow? = null
                    if (successful) {
                        val json: JsonObject = GTSPlugin.Companion.getInstance().getGson()
                            .fromJson(results.getString("listing"), JsonObject::class.java)
                        if (!json.has("type")) {
                            throw JsonParseException("Invalid Listing: Missing type")
                        }
                        val type = json["type"].asString
                        listing = if (type == "bin") {
                            instance!!.gTSComponentManager
                                .getListingResourceManager(BuyItNow::class.java)
                                .get()
                                .deserializer
                                .deserialize(json)
                        } else {
                            throw IllegalArgumentException("Can't purchase an Auction")
                        }
                        seller = listing.lister
                        if (successful) {
                            successful = !listing.isPurchased
                        }
                    }
                    if (successful) {
                        if (listing != null) {
                            listing.markPurchased()
                            sendListingUpdate(listing)
                        }
                    }
                    BINPurchaseMessage.Response(
                        GTSPlugin.Companion.getInstance().getMessagingService().generatePingID(),
                        request.iD,
                        request.listingID,
                        request.actor,
                        seller,
                        successful,
                        if (successful) null else ErrorCodes.ALREADY_PURCHASED
                    )
                })
            })
    }

    @kotlin.Throws(Exception::class)
    override fun sendListingUpdate(listing: Listing): Boolean {
        return query(UPDATE_LISTING, SQLPrepared<Boolean> { connection: Connection?, ps: PreparedStatement ->
            ps.setString(
                1, GTSPlugin.Companion.getInstance().getGson().toJson(
                    listing.serialize()!!.toJson()
                )
            )
            ps.setString(2, listing.iD.toString())
            ps.executeUpdate() != 0
        })
    }

    @kotlin.Throws(Exception::class)
    override fun processBid(request: AuctionMessage.Bid.Request?): AuctionMessage.Bid.Response? {
        return query<AuctionMessage.Bid.Response>(
            GET_SPECIFIC_LISTING,
            SQLPrepared<AuctionMessage.Bid.Response> { connection: Connection?, ps: PreparedStatement ->
                ps.setString(1, request!!.auctionID.toString())
                results<AuctionMessage.Bid.Response>(ps, SQLResults<AuctionMessage.Bid.Response> { results: ResultSet ->
                    val response: AuctionMessage.Bid.Response
                    val fatal = false
                    var successful = TriState.FALSE // FALSE = Missing Listing ID
                    var bids = TreeMultimap.create(
                        Comparator.naturalOrder<UUID?>(),
                        Collections.reverseOrder(Comparator.comparing(Auction.Bid::amount))
                    )
                    if (results.next()) {
                        val json: JsonObject = GTSPlugin.Companion.getInstance().getGson()
                            .fromJson(results.getString("listing"), JsonObject::class.java)
                        if (!json.has("type")) {
                            throw JsonParseException("Invalid Listing: Missing type")
                        }
                        val type = json["type"].asString
                        check(type == "auction") { "Trying to place bid on non-auction" }
                        val auction: Auction = instance!!.gTSComponentManager
                            .getListingResourceManager(Auction::class.java)
                            .map(ResourceManager::deserializer)
                            .get()
                            .deserialize(json)
                        successful =
                            if (auction.bid(request.actor, request.amountBid)) TriState.TRUE else TriState.UNDEFINED
                        bids = auction.bids
                        if (!sendListingUpdate(auction)) {
                            successful = TriState.FALSE
                        }
                    }
                    response = AuctionBidMessage.Response(
                        GTSPlugin.Companion.getInstance().getMessagingService().generatePingID(),
                        request.iD,
                        request.auctionID,
                        request.actor,
                        request.amountBid,
                        successful.asBoolean(),
                        UUID.fromString(results.getString("lister")),
                        bids,
                        if (successful === TriState.UNDEFINED) ErrorCodes.OUTBID else if (successful === TriState.FALSE) if (fatal) ErrorCodes.FATAL_ERROR else ErrorCodes.LISTING_MISSING else null
                    )
                    response
                })
            })
    }

    @kotlin.Throws(Exception::class)
    override fun processClaimRequest(request: ClaimMessage.Request?): ClaimMessage.Response? {
        val listing = getListing(request!!.listingID)
        val response: UUID = GTSPlugin.Companion.getInstance().getMessagingService().generatePingID()
        return if (!listing.isPresent) {
            ClaimResponseImpl.Companion.builder()
                .id(response)
                .request(request.iD)
                .listing(request.listingID)
                .actor(request.actor)
                .receiver(request.receiver!!.orElse(null))
                .error(ErrorCodes.LISTING_MISSING)
                .build()
        } else {
            val isLister = request.actor == listing.get().lister
            if (listing.map { l: Listing? -> l is Auction }.orElse(false)) {
                if (!listing.map { l: Listing? -> l as Auction? }.get().hasAnyBidsPlaced()) {
                    val builder: ClaimResponseBuilder = ClaimResponseImpl.Companion.builder()
                        .id(response)
                        .request(request.iD)
                        .listing(request.listingID)
                        .actor(request.actor)
                        .successful()
                        .auction()
                        .winner(false)
                        .lister(false)
                        .receiver(request.receiver!!.orElse(null))
                    if (deleteListing(request.listingID)) {
                        builder.successful()
                    }
                    return builder.build()
                }
                val auction = listing.map { l: Listing? -> l as Auction? }.get()
                val claimer = isLister || auction.highBid!!.get().first == request.actor
                query(
                    GET_AUCTION_CLAIM_STATUS,
                    SQLPrepared<ClaimResponseImpl> { connection: Connection, ps: PreparedStatement ->
                        ps.setString(1, request.listingID.toString())
                        results<ClaimResponseImpl>(ps, SQLResults<ClaimResponseImpl> { results: ResultSet ->
                            var result: Int
                            val lister: Boolean
                            val winner: Boolean
                            var others: MutableList<UUID?> = Lists.newArrayList()
                            if (results.next()) {
                                others = if (results.getString("others") == null) {
                                    Lists.newArrayList()
                                } else {
                                    GTSPlugin.Companion.getInstance().getGson().fromJson(
                                        results.getString("others"),
                                        object : TypeToken<List<UUID?>?>() {}.type
                                    )
                                }
                                val key =
                                    if (isLister) UPDATE_AUCTION_CLAIM_LISTER else if (claimer) UPDATE_AUCTION_CLAIM_WINNER else UPDATE_AUCTION_CLAIM_OTHER
                                connection.prepareStatement(statementProcessor.apply(key)).use { update ->
                                    if (claimer) {
                                        update.setBoolean(1, true)
                                    } else {
                                        others.add(request.actor)
                                        update.setString(1, GTSPlugin.Companion.getInstance().getGson().toJson(others))
                                    }
                                    update.setString(2, request.listingID.toString())
                                    result = update.executeUpdate()
                                }
                                lister = results.getBoolean("lister") || key == UPDATE_AUCTION_CLAIM_LISTER
                                winner = results.getBoolean("winner") || key == UPDATE_AUCTION_CLAIM_WINNER
                                val all = lister && winner && auction.bids.keySet().stream()
                                    .filter(Predicate<UUID> { bidder: UUID -> auction.highBid!!.get().first != bidder })
                                    .allMatch(Predicate<UUID> { o: UUID? -> others.contains(o) })
                                if (result > 0 && all) {
                                    connection.prepareStatement(statementProcessor.apply(DELETE_AUCTION_CLAIM_STATUS))
                                        .use { delete ->
                                            delete.setString(1, request.listingID.toString())
                                            result = delete.executeUpdate()
                                            deleteListing(listing.get().iD)
                                        }
                                }
                            } else {
                                if (!claimer) {
                                    others.add(request.actor)
                                }
                                connection.prepareStatement(statementProcessor.apply(ADD_AUCTION_CLAIM_STATUS))
                                    .use { append ->
                                        append.setString(1, request.listingID.toString())
                                        append.setBoolean(2, isLister && claimer)
                                        append.setBoolean(3, !isLister && claimer)
                                        append.setString(4, GTSPlugin.Companion.getInstance().getGson().toJson(others))
                                        result = append.executeUpdate()
                                    }
                                lister = isLister
                                winner = !isLister
                            }
                            val o = ImmutableList.copyOf(others)
                            val claimed: MutableMap<UUID, Boolean> = Maps.newHashMap()
                            auction.bids.keySet().stream()
                                .filter(Predicate<UUID> { bidder: UUID -> auction.highBid!!.get().first != bidder })
                                .forEach(Consumer<UUID> { bidder: UUID -> claimed[bidder] = o.contains(bidder) })
                            val builder: ClaimResponseBuilder = ClaimResponseImpl.Companion.builder()
                                .id(response)
                                .request(request.iD)
                                .listing(request.listingID)
                                .actor(request.actor)
                                .receiver(request.receiver!!.orElse(null))
                                .successful()
                                .auction()
                                .lister(lister)
                                .winner(winner)
                                .others(claimed)
                            if (result > 0) {
                                builder.successful()
                            } else {
                                builder.error(ErrorCodes.FATAL_ERROR)
                            }
                            builder.build()
                        })
                    })
            } else {
                val builder: ClaimResponseBuilder = ClaimResponseImpl.Companion.builder()
                    .id(response)
                    .request(request.iD)
                    .listing(request.listingID)
                    .actor(request.actor)
                    .receiver(request.receiver!!.orElse(null))
                if (deleteListing(request.listingID)) {
                    builder.successful()
                }
                builder.build()
            }
        }
    }

    @kotlin.Throws(Exception::class)
    override fun appendOldClaimStatus(auction: UUID?, lister: Boolean, winner: Boolean, others: List<UUID?>?): Boolean {
        return query(ADD_AUCTION_CLAIM_STATUS, SQLPrepared<Boolean> { connection: Connection?, ps: PreparedStatement ->
            ps.setString(1, auction.toString())
            ps.setBoolean(2, lister)
            ps.setBoolean(3, winner)
            ps.setString(4, GTSPlugin.Companion.getInstance().getGson().toJson(others))
            ps.executeUpdate() != 0
        })
    }

    @kotlin.Throws(Exception::class)
    override fun processAuctionCancelRequest(request: AuctionMessage.Cancel.Request?): AuctionMessage.Cancel.Response? {
        return query<AuctionCancelMessage.Response>(
            GET_SPECIFIC_LISTING,
            SQLPrepared<AuctionCancelMessage.Response> { connection: Connection?, ps: PreparedStatement ->
                ps.setString(1, request!!.auctionID.toString())
                val data = AtomicReference<Auction>()
                results<AuctionCancelMessage.Response>(
                    ps,
                    SQLResults<AuctionCancelMessage.Response> { results: ResultSet ->
                        var result = false
                        val bidders: MutableList<UUID> = Lists.newArrayList()
                        var error: ErrorCode? = null
                        if (results.next()) {
                            val json: JsonObject = GTSPlugin.Companion.getInstance().getGson()
                                .fromJson(results.getString("listing"), JsonObject::class.java)
                            if (!json.has("type")) {
                                throw JsonParseException("Invalid Listing: Missing type")
                            }
                            val type = json["type"].asString
                            check(type == "auction") { "Trying to place bid on non-auction" }
                            val auction: Auction = instance!!.gTSComponentManager
                                .getListingResourceManager(Auction::class.java)
                                .map(ResourceManager::deserializer)
                                .get()
                                .deserialize(json)
                            data.set(auction)
                            if (plugin.configuration.get(ConfigKeys.AUCTIONS_ALLOW_CANCEL_WITH_BIDS)) {
                                auction.bids.keySet().stream().distinct()
                                    .forEach(Consumer<UUID> { e: UUID -> bidders.add(e) })
                                result = deleteListing(auction.iD)
                            } else {
                                if (auction.bids.size() > 0) {
                                    error = ErrorCodes.BIDS_PLACED
                                } else {
                                    result = deleteListing(auction.iD)
                                }
                            }
                        }
                        AuctionCancelMessage.Response(
                            GTSPlugin.Companion.getInstance().getMessagingService().generatePingID(),
                            request.iD,
                            data.get(),
                            request.auctionID,
                            request.actor,
                            ImmutableList.copyOf(bidders),
                            result,
                            error
                        )
                    })
            })
    }

    @kotlin.Throws(Exception::class)
    override fun processListingRemoveRequest(request: BuyItNowMessage.Remove.Request?): BuyItNowMessage.Remove.Response? {
        val processor =
            BiFunction<Boolean, ErrorCode, BINRemoveMessage.Response> { success: Boolean, error: ErrorCode? ->
                BINRemoveMessage.Response(
                    GTSPlugin.Companion.getInstance().getMessagingService().generatePingID(),
                    request!!.iD,
                    request.listingID,
                    request.actor,
                    request.recipient!!.orElse(null),
                    request.shouldReturnListing(),
                    success,
                    if (success) null else error
                )
            }
        val listing = getListing(request!!.listingID)
        if (!listing.isPresent) {
            return processor.apply(false, ErrorCodes.LISTING_MISSING)
        }
        val result = listing.get()
        return if ((result as BuyItNow).isPurchased) {
            processor.apply(false, ErrorCodes.ALREADY_PURCHASED)
        } else processor.apply(deleteListing(request.listingID), ErrorCodes.FATAL_ERROR)
    }

    @kotlin.Throws(Exception::class)
    override fun processForcedDeletion(request: ForceDeleteMessage.Request?): ForceDeleteMessage.Response? {
        val listing = getListing(request!!.listingID)
        return if (listing.isPresent) {
            val successful = deleteListing(request.listingID)
            ForceDeleteMessage.Response.builder()
                .request(request.iD)
                .listing(request.listingID)
                .actor(request.actor)
                .data(listing.get())
                .give(request.shouldGive())
                .successful(successful)
                .error(if (successful) null else ErrorCodes.FATAL_ERROR)
                .build()
        } else {
            ForceDeleteMessage.Response.builder()
                .request(request.iD)
                .listing(request.listingID)
                .actor(request.actor)
                .successful(false)
                .give(request.shouldGive())
                .error(ErrorCodes.LISTING_MISSING)
                .build()
        }
    }

    @kotlin.Throws(SQLException::class)
    private fun tableExists(table: String): Boolean {
        connectionFactory.connection.use { connection ->
            connection.metaData.getTables(null, null, "%", null).use { rs ->
                while (rs.next()) {
                    if (rs.getString(3).equals(table, ignoreCase = true)) {
                        return true
                    }
                }
                return false
            }
        }
    }

    @Deprecated("")
    private var ran = false

    /**
     * This will read the old data from our database, and apply the necessary changes required to
     * update the data to our new system.
     *
     */
    @Deprecated("This is purely for legacy updating. This will be removed in 6.1")
    @kotlin.Throws(Exception::class)
    private fun translateLegacy() {
        if (!ran && tableExists(statementProcessor.apply("{prefix}listings_v3"))) {
            GTSPlugin.Companion.getInstance().getPluginLogger().info("&6Attempting to translate legacy data...")
            val successful = AtomicInteger()
            val parsed = AtomicInteger()
            val printer = PrettyPrinter(80)
            printer.add("Legacy Translation Effort").center()
            printer.table("ID", "Parsed", "Successful")
            ran = true
            query<Any>(
                statementProcessor.apply("SELECT * from {prefix}listings_v3"),
                SQLPrepared<Any> { connection: Connection, query: PreparedStatement ->
                    results<Any>(query, SQLResults<Any> { incoming: ResultSet ->
                        connection.autoCommit = false
                        try {
                            connection.prepareStatement(statementProcessor.apply(ADD_LISTING)).use { ps ->
                                while (incoming.next()) {
                                    val id = UUID.fromString(incoming.getString("id"))
                                    if (getListing(id).isPresent) {
                                        continue
                                    }
                                    val json: JsonObject = GTSPlugin.Companion.getInstance().getGson()
                                        .fromJson(incoming.getString("entry"), JsonObject::class.java)
                                    if (!json.has("element")) {
                                        continue
                                    }
                                    try {
                                        val lister = UUID.fromString(incoming.getString("owner"))
                                        val expiration = incoming.getTimestamp("expiration").toLocalDateTime()
                                        val price = instance!!.gTSComponentManager
                                            .getPriceManager<Price<*, *, *>>("currency")
                                            .orElseThrow(Supplier { IllegalStateException("No deserializer for currency available") })
                                            .deserializer
                                            .deserialize(JObject().add("value", incoming.getDouble("price")).toJson())
                                        val entry = instance!!.gTSComponentManager
                                            .getLegacyEntryDeserializer<Entry<*, *>>(json["type"].asString)
                                            .orElseThrow(Supplier { IllegalStateException("No deserializer for legacy entry type: " + json["type"].asString) })
                                            .deserialize(json)
                                        val bin = BuyItNow.builder()
                                            .id(id)
                                            .lister(lister)
                                            .expiration(expiration)
                                            .price(price)
                                            .entry(entry)
                                            .build()
                                        ps.setString(1, id.toString())
                                        ps.setString(2, lister.toString())
                                        ps.setString(
                                            3,
                                            GTSPlugin.Companion.getInstance().getGson()
                                                .toJson(bin!!.serialize()!!.toJson())
                                        )
                                        query<Any>(
                                            statementProcessor.apply("DELETE FROM {prefix}listings_v3 WHERE ID=?"),
                                            SQLPrepared<Any> { con: Connection?, p: PreparedStatement ->
                                                p.setString(1, id.toString())
                                                p.executeUpdate()
                                                null
                                            })
                                        ps.addBatch()
                                        successful.incrementAndGet()
                                    } catch (e: IllegalStateException) {
                                        GTSPlugin.Companion.getInstance().getPluginLogger()
                                            .error("Failed to read listing with ID: $id")
                                        GTSPlugin.Companion.getInstance().getPluginLogger().error("  * " + e.message)
                                    } catch (e: Exception) {
                                        GTSPlugin.Companion.getInstance().getPluginLogger()
                                            .error("Unexpectedly failed to read listing with ID: $id")
                                        ExceptionWriter.write(e)
                                    } finally {
                                        parsed.incrementAndGet()
                                        printer.tr(id, parsed.get(), successful.get())
                                    }
                                }
                                ps.executeBatch()
                                connection.commit()
                            }
                        } finally {
                            connection.autoCommit = true
                        }
                        null
                    })
                }
            )
            printer.log(GTSPlugin.Companion.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG)
            if (successful.get() == parsed.get()) {
                connectionFactory.connection.use { connection ->
                    val statement = connection.createStatement()
                    statement.executeUpdate(statementProcessor.apply("DROP TABLE {prefix}listings_v3"))
                }
                GTSPlugin.Companion.getInstance().getPluginLogger()
                    .info("Successfully converted " + successful.get() + " instances of legacy data!")
            } else {
                GTSPlugin.Companion.getInstance().getPluginLogger()
                    .warn("Some data failed to be converted, as such, we preserved the remaining data...")
                GTSPlugin.Companion.getInstance().getPluginLogger()
                    .warn("Check the logs above for further information!")
            }
        }
    }

    private enum class SchemaReaders(private val initial: SchemaPredicate, private val last: SchemaPredicate) {
        CREATE_TABLE(
            SchemaPredicate { impl: SqlImplementation?, `in`: String -> `in`.startsWith("CREATE TABLE") },
            SchemaPredicate { impl: SqlImplementation, `in`: String ->
                !impl.tableExists(
                    getTable(`in`)
                )
            }),
        ALTER_TABLE(
            SchemaPredicate { impl: SqlImplementation?, `in`: String -> `in`.startsWith("ALTER TABLE") },
            SchemaPredicate { impl: SqlImplementation, `in`: String ->
                impl.tableExists(
                    getTable(`in`)
                )
            }),
        ANY(
            SchemaPredicate { impl: SqlImplementation?, input: String? -> true },
            SchemaPredicate { impl: SqlImplementation?, input: String? -> true });

        companion object {
            fun any(impl: SqlImplementation?, `in`: String?): Boolean {
                return Arrays.stream(values()).map { sr: SchemaReaders ->
                    try {
                        return@map sr.initial.test(impl, `in`)
                    } catch (e: Exception) {
                        ExceptionWriter.write(e)
                        return@map false
                    }
                }.filter { x: Boolean? -> x!! }.findAny().orElse(false)
            }

            @kotlin.Throws(Exception::class)
            fun first(impl: SqlImplementation?, `in`: String?, statement: Statement) {
                for (reader in values()) {
                    if (reader != ANY) {
                        if (reader.initial.test(impl, `in`) && reader.last.test(impl, `in`)) {
                            statement.addBatch(`in`)
                            return
                        }
                    } else {
                        for (r in Arrays.stream(values()).filter { sr: SchemaReaders -> sr != ANY }
                            .collect(Collectors.toList())) {
                            if (r.initial.test(impl, `in`)) {
                                return
                            }
                        }
                        statement.addBatch(`in`)
                    }
                }
            }

            private fun getTable(`in`: String): String {
                val start = `in`.indexOf('`')
                return `in`.substring(start + 1, `in`.indexOf('`', start + 1))
            }
        }
    }

    private interface SchemaPredicate {
        @kotlin.Throws(Exception::class)
        fun test(impl: SqlImplementation?, input: String?): Boolean
    }

    companion object {
        private const val ADD_LISTING = "INSERT INTO `{prefix}listings` (id, lister, listing) VALUES (?, ?, ?)"
        private const val UPDATE_LISTING = "UPDATE `{prefix}listings` SET listing=? WHERE id=?"
        private const val SELECT_ALL_LISTINGS = "SELECT * FROM `{prefix}listings`"
        private const val GET_SPECIFIC_LISTING = "SELECT * FROM `{prefix}listings` WHERE id=?"
        private const val GET_ALL_USER_LISTINGS = "SELECT id FROM `{prefix}listings` WHERE lister=?"
        private const val DELETE_LISTING = "DELETE FROM `{prefix}listings` WHERE id=?"
        private const val ADD_AUCTION_CLAIM_STATUS =
            "INSERT INTO `{prefix}auction_claims` (auction, lister, winner, others) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE lister=VALUES(lister), winner=VALUES(winner), others=VALUES(others)"
        private const val GET_AUCTION_CLAIM_STATUS = "SELECT * FROM `{prefix}auction_claims` WHERE auction=?"
        private const val UPDATE_AUCTION_CLAIM_LISTER = "UPDATE `{prefix}auction_claims` SET lister=? WHERE auction=?"
        private const val UPDATE_AUCTION_CLAIM_WINNER = "UPDATE `{prefix}auction_claims` SET winner=? WHERE auction=?"
        private const val UPDATE_AUCTION_CLAIM_OTHER = "UPDATE `{prefix}auction_claims` SET others=? WHERE auction=?"
        private const val DELETE_AUCTION_CLAIM_STATUS = "DELETE FROM `{prefix}auction_claims` WHERE auction=?"
        private const val APPLY_PLAYER_SETTINGS =
            "INSERT INTO `{prefix}player_settings` (uuid, pub_notif, sell_notif, bid_notif, outbid_notif) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE pub_notif=VALUES(pub_notif), sell_notif=VALUES(sell_notif), bid_notif=VALUES(bid_notif), outbid_notif=VALUES(outbid_notif)"
        private const val GET_PLAYER_SETTINGS =
            "SELECT pub_notif, sell_notif, bid_notif, outbid_notif FROM `{prefix}player_settings` WHERE uuid=?"
    }

    init {
        statementProcessor = connectionFactory.statementProcessor.compose { s: String ->
            s.replace("{prefix}", tablePrefix).replace(
                "{database}", GTSPlugin.Companion.getInstance().getConfiguration().get(
                    ConfigKeys.STORAGE_CREDENTIALS
                ).getDatabase()
            )
        }
    }
}