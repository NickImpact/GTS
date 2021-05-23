package net.impactdev.gts.common.listings

import com.google.gson.JsonObject
import net.impactdev.gts.api.listings.makeup.Display
import net.impactdev.gts.api.listings.prices.Price
import net.impactdev.impactor.api.json.factory.JObject
import net.kyori.adventure.text.TextComponent
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class JsonStoredPrice(override val price: JsonObject) : Price<JsonObject?, Void?, Void?> {
    override val text: TextComponent
        get() {
            throw UnsupportedOperationException()
        }
    override val display: Display<Void>
        get() {
            throw UnsupportedOperationException()
        }

    override fun canPay(payer: UUID?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun pay(payer: UUID?, source: Any?, marker: AtomicBoolean) {
        throw UnsupportedOperationException()
    }

    override fun reward(recipient: UUID?): Boolean {
        throw UnsupportedOperationException()
    }

    override val sourceType: Class<Void>
        get() = Void::class.java

    override fun calculateFee(listingType: Boolean): Long {
        throw UnsupportedOperationException()
    }

    override val version: Int
        get() {
            throw UnsupportedOperationException()
        }

    override fun serialize(): JObject? {
        throw UnsupportedOperationException()
    }
}