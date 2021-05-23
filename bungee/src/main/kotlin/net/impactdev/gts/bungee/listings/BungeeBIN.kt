package net.impactdev.gts.bungee.listings

import com.google.common.base.Preconditions
import com.google.gson.JsonObject
import net.impactdev.gts.api.listings.buyitnow.BuyItNow
import net.impactdev.gts.api.listings.buyitnow.BuyItNow.BuyItNowBuilder
import net.impactdev.gts.api.listings.entries.Entry
import net.impactdev.gts.api.listings.prices.Price
import net.impactdev.gts.common.listings.JsonStoredEntry
import net.impactdev.gts.common.listings.JsonStoredPrice
import net.impactdev.impactor.api.json.factory.JObject
import java.time.LocalDateTime
import java.util.*

class BungeeBIN(builder: BungeeBINBuilder) :
    BungeeListing(builder.id, builder.lister, builder.entry, builder.expiration), BuyItNow {
    private override val price: JsonStoredPrice?
    override var isPurchased: Boolean
        private set
    private val purchaser: UUID?
    private val stashed: Boolean
    override fun getPrice(): Price<*, *, *>? {
        return price
    }

    override fun purchaser(): UUID? {
        return purchaser
    }

    override fun stashedForPurchaser(): Boolean {
        return stashed
    }

    override fun markPurchased() {
        isPurchased = true
    }

    override fun serialize(): JObject? {
        val json = super.serialize()
        json.add("price", price.getPrice())
        json!!.add("type", "bin")
        json.add("stashed", stashed)
        if (stashed) {
            json.add("purchaser", purchaser.toString())
        }
        return json
    }

    class BungeeBINBuilder : BuyItNowBuilder {
        var id = UUID.randomUUID()
        var lister: UUID? = null
        var entry: JsonStoredEntry? = null
        var price: JsonStoredPrice? = null
        var purchased = false
        var expiration: LocalDateTime? = null
        var purchaser: UUID? = null
        var stashed = false
        override fun id(id: UUID?): BuyItNowBuilder? {
            this.id = id
            return this
        }

        override fun lister(lister: UUID?): BuyItNowBuilder? {
            this.lister = lister
            return this
        }

        override fun entry(entry: Entry<*, *>?): BuyItNowBuilder? {
            Preconditions.checkArgument(entry is JsonStoredEntry, "Mixing of incompatible platform types")
            this.entry = entry as JsonStoredEntry?
            return this
        }

        override fun price(price: Price<*, *, *>?): BuyItNowBuilder? {
            Preconditions.checkArgument(price is JsonStoredPrice, "Mixing of incompatible platform types")
            this.price = price as JsonStoredPrice?
            return this
        }

        override fun purchased(): BuyItNowBuilder? {
            purchased = true
            return this
        }

        override fun purchaser(purchaser: UUID?): BuyItNowBuilder? {
            this.purchaser = purchaser
            return this
        }

        override fun stashedForPurchaser(): BuyItNowBuilder? {
            stashed = true
            return this
        }

        override fun expiration(expiration: LocalDateTime?): BuyItNowBuilder? {
            this.expiration = expiration
            return this
        }

        override fun from(input: BuyItNow): BuyItNowBuilder? {
            return id(input.iD)
                .lister(input.lister)
                .entry(input.entry)
                .price(input.price)
                .expiration(input.expiration)
        }

        override fun build(): BungeeBIN? {
            return BungeeBIN(this)
        }
    }

    companion object {
        fun deserialize(json: JsonObject): BungeeBIN? {
            val builder = BuyItNow.builder()
                .id(UUID.fromString(json["id"].asString))
                .lister(UUID.fromString(json["lister"].asString))
                .expiration(LocalDateTime.parse(json.getAsJsonObject("timings")["expiration"].asString)) as BungeeBINBuilder?
            val element = json.getAsJsonObject("entry")
            builder!!.entry(JsonStoredEntry(element))
            val price = json.getAsJsonObject("price")
            builder.price(JsonStoredPrice(price))
            if (price.has("purchased") && price["purchased"].asBoolean) {
                builder.purchased()
            }
            if (json["stashed"].asBoolean) {
                builder.stashedForPurchaser()
                builder.purchaser(UUID.fromString(json["purchaser"].asString))
            }
            return builder.build()
        }
    }

    init {
        price = builder.price
        isPurchased = builder.purchased
        stashed = builder.stashed
        purchaser = builder.purchaser
    }
}