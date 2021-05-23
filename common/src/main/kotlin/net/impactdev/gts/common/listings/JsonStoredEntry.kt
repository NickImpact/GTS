package net.impactdev.gts.common.listings

import com.google.gson.JsonObject
import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.listings.entries.Entry
import net.impactdev.gts.api.listings.makeup.Display
import net.impactdev.impactor.api.json.factory.JObject
import net.kyori.adventure.text.TextComponent
import java.util.*

/**
 * A JsonStoredEntry represents the backing that Proxy servers will use in order to manage all listings from the
 * representative cache. As these proxy servers will have no way to determine what type of entry they are working
 * with, they must all end up in a similar manner. To achieve this, this class simply does 0 deserialization. In
 * other words, any and all JSON data representing a entry will remain serialized should the element ever be requested.
 *
 * NOTE: This class is only intended for references. As such, all other functionality other than data fetching
 * will be unsupported, and throw an error during any attempt to call these functions.
 */
class JsonStoredEntry(override val orCreateElement: JsonObject) : Entry<JsonObject?, Void?> {
    override val name: TextComponent
        get() {
            throw UnsupportedOperationException()
        }
    override val description: TextComponent
        get() {
            throw UnsupportedOperationException()
        }

    override fun getDisplay(viewer: UUID?, listing: Listing?): Display<Void>? {
        throw UnsupportedOperationException()
    }

    override fun give(receiver: UUID?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun take(depositor: UUID?): Boolean {
        throw UnsupportedOperationException()
    }

    override val thumbnailURL: Optional<String>
        get() {
            throw UnsupportedOperationException()
        }
    override val details: List<String>
        get() {
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