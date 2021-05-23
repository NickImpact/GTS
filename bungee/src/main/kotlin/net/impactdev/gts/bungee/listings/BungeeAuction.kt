package net.impactdev.gts.bungee.listings

import com.google.common.base.Preconditions
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Maps
import com.google.common.collect.Multimap
import com.google.common.collect.TreeMultimap
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.impactdev.gts.api.listings.auctions.Auction
import net.impactdev.gts.api.listings.auctions.Auction.AuctionBuilder
import net.impactdev.gts.api.listings.entries.Entry
import net.impactdev.gts.common.listings.JsonStoredEntry
import net.impactdev.impactor.api.json.factory.JArray
import net.impactdev.impactor.api.json.factory.JObject
import net.impactdev.impactor.api.utilities.mappings.Tuple
import java.time.LocalDateTime
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors

class BungeeAuction(builder: BungeeAuctionBuilder) :
    BungeeListing(builder.id, builder.lister, builder.entry, builder.published, builder.expiration), Auction {
    /** The starting price of this auction  */
    override val startingPrice: Double

    /** The current price of this auction, representing the start or highest bid  */
    override var currentPrice: Double
        private set

    /** The base increment percentage required to place a bid  */
    override val increment: Float
    override val bids = TreeMultimap.create(
        Comparator.naturalOrder<UUID?>(),
        Collections.reverseOrder(Comparator.comparing(Auction.Bid::amount))
    )

    override fun getCurrentBid(uuid: UUID?): Optional<Auction.Bid?>? {
        return Optional.of(bids[uuid])
            .map { set: NavigableSet<Auction.Bid?> ->
                if (set.isEmpty()) {
                    return@map null
                }
                set
            }
            .map({ obj: NavigableSet<Auction.Bid?> -> obj.first()!! })
    }

    override val uniqueBiddersWithHighestBids: Map<UUID?, Auction.Bid?>
        get() {
            val unique: MutableMap<UUID?, Auction.Bid?> = Maps.newHashMap()
            for (uuid in ArrayList(bids.keys())) {
                unique[uuid] = bids[uuid].first()
            }
            return unique
        }
    override val highBid: Optional<Tuple<UUID, Auction.Bid>>
        get() = bids.entries().stream()
            .max(Comparator.comparing(Function { value: Map.Entry<UUID?, Auction.Bid> -> value.value.amount }))
            .map(Function { e: Map.Entry<UUID?, Auction.Bid?> -> Tuple(e.key, e.value) })
    override val nextBidRequirement: Double
        get() {
            val result: Double
            result = if (bids.size() == 0) {
                startingPrice
            } else {
                currentPrice * (1.0 + increment)
            }
            return result
        }

    override fun bid(user: UUID?, amount: Double): Boolean {
        if (bids.size() == 0 || amount >= highBid.get().second.amount * (1.0 + increment)) {
            bids.put(user, Auction.Bid(amount))
            currentPrice = amount
            return true
        }
        return false
    }

    override fun serialize(): JObject? {
        val json = super.serialize()
        val bids = JObject()
        for (id in this.bids.keys()) {
            val array = JArray()
            for (bid in this.bids[id].stream()
                .sorted(Collections.reverseOrder(Comparator.comparing(Auction.Bid::amount))).collect(
                Collectors.toList()
            )) {
                array.add(bid.serialize())
            }
            bids.add(id.toString(), array)
        }
        val pricing = JObject()
            .add("start", startingPrice)
            .add("current", currentPrice)
            .add("increment", increment)
        json!!.add(
            "auction", JObject()
                .add("bids", bids)
                .add("pricing", pricing)
        )
        json.add("type", "auction")
        return json
    }

    class BungeeAuctionBuilder : AuctionBuilder {
        var id = UUID.randomUUID()
        var lister: UUID? = null
        var entry: JsonStoredEntry? = null
        var published = LocalDateTime.now()
        var expiration: LocalDateTime? = null
        var start = 0.0
        var increment = 0f
        var current = 0.0
        var bids: Multimap<UUID?, Auction.Bid?>? = null
        override fun id(id: UUID?): AuctionBuilder? {
            this.id = id
            return this
        }

        override fun lister(lister: UUID?): AuctionBuilder? {
            this.lister = lister
            return this
        }

        override fun entry(entry: Entry<*, *>?): AuctionBuilder? {
            Preconditions.checkArgument(entry is JsonStoredEntry, "Mixing of invalid types!")
            this.entry = entry as JsonStoredEntry?
            return this
        }

        override fun published(published: LocalDateTime?): AuctionBuilder? {
            this.published = published
            return this
        }

        override fun expiration(expiration: LocalDateTime?): AuctionBuilder? {
            this.expiration = expiration
            return this
        }

        override fun start(amount: Double): AuctionBuilder? {
            start = amount
            return this
        }

        override fun increment(rate: Float): AuctionBuilder? {
            increment = rate
            return this
        }

        override fun current(current: Double): AuctionBuilder? {
            this.current = current
            return this
        }

        override fun bids(bids: Multimap<UUID?, Auction.Bid?>?): AuctionBuilder? {
            this.bids = bids
            return this
        }

        override fun from(input: Auction): AuctionBuilder? {
            Preconditions.checkArgument(input is BungeeAuction, "Mixing of invalid types!")
            id = input.iD
            lister = input.lister
            entry = input.entry as JsonStoredEntry
            published = input.publishTime
            expiration = input.expiration
            start = input.startingPrice
            increment = input.increment
            current = input.currentPrice
            return this
        }

        override fun build(): BungeeAuction? {
            return BungeeAuction(this)
        }
    }

    companion object {
        fun deserialize(`object`: JsonObject): BungeeAuction? {
            val builder = Auction.builder()
                .id(UUID.fromString(`object`["id"].asString))
                .lister(UUID.fromString(`object`["lister"].asString))
                .published(LocalDateTime.parse(`object`.getAsJsonObject("timings")["published"].asString))
                .expiration(LocalDateTime.parse(`object`.getAsJsonObject("timings")["expiration"].asString))
                .start(`object`.getAsJsonObject("auction").getAsJsonObject("pricing")["start"].asDouble)
                .current(`object`.getAsJsonObject("auction").getAsJsonObject("pricing")["current"].asDouble)
                .increment(
                    `object`.getAsJsonObject("auction").getAsJsonObject("pricing")["increment"].asFloat
                ) as BungeeAuctionBuilder?
            val element = `object`.getAsJsonObject("entry")
            builder!!.entry(JsonStoredEntry(element))
            val bids = `object`.getAsJsonObject("auction").getAsJsonObject("bids")
            val mapping: Multimap<UUID?, Auction.Bid?> = ArrayListMultimap.create()
            for ((key, value) in bids.entrySet()) {
                if (value.isJsonArray) {
                    value.asJsonArray.forEach(Consumer { e: JsonElement ->
                        val data = e.asJsonObject
                        val bid = Auction.Bid.builder()
                            .amount(data["amount"].asDouble)
                            .timestamp(LocalDateTime.parse(data["timestamp"].asString))
                            .build()
                        mapping.put(UUID.fromString(key), bid)
                    })
                }
            }
            builder.bids(mapping)
            return builder.build()
        }
    }

    init {
        startingPrice = builder.start
        currentPrice = Math.max(builder.current, startingPrice)
        increment = builder.increment
        if (builder.bids != null) {
            builder.bids!!.forEach { key: UUID?, value: Auction.Bid? -> bids.put(key, value) }
        }
    }
}