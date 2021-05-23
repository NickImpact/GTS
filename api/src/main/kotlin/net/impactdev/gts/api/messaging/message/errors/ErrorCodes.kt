package net.impactdev.gts.api.messaging.message.errors

import com.google.common.collect.ImmutableList
import java.lang.reflect.Modifier
import java.util.*

object ErrorCodes {
    // Generic
    @kotlin.jvm.JvmField
    val LISTING_MISSING = create("UNKNOWN_LISTING", "Listing could not be found")
    @kotlin.jvm.JvmField
    val REQUEST_TIMED_OUT = create("TIMEOUT", "Failed to receive a response within 5 seconds")
    @kotlin.jvm.JvmField
    val THIRD_PARTY_CANCELLED = create("OUTSIDE_CANCEL", "An outside source cancelled your request")
    val LISTING_EXPIRED = create("EXPIRED", "The listing has expired")
    @kotlin.jvm.JvmField
    val FAILED_TO_GIVE = create("UNABLE_TO_GIVE", "The item could not be rewarded successfully")

    // BIN
    @kotlin.jvm.JvmField
    val ALREADY_PURCHASED = create("PURCHASED", "Listing already purchased")

    // Auctions
    @kotlin.jvm.JvmField
    val OUTBID = create("OUTBID", "Another user has already placed a larger bid")
    @kotlin.jvm.JvmField
    val BIDS_PLACED = create("BIDS_PRESENT", "At least one bid has already been placed on your auction")

    // Fatal
    @kotlin.jvm.JvmField
    val FATAL_ERROR = create("FATAL", "A fatal error occurred...")
    @kotlin.jvm.JvmField
    val UNKNOWN = create("UNKNOWN", "Literally no idea what happened")

    // Safe Mode Reasons
    @kotlin.jvm.JvmField
    val ECONOMY = create("LACKING_ECONOMY", "You are missing an economy plugin!")
    private val KEYS: List<ErrorCode>? = null
    @kotlin.jvm.JvmStatic
    operator fun get(ordinal: Int): ErrorCode {
        return KEYS!![ordinal]
    }

    private fun create(key: String, description: String): ErrorCode {
        return ErrorCodeBackend(key, description)
    }

    class ErrorCodeBackend internal constructor(override val key: String, override val description: String) :
        ErrorCode {
        private var ordinal = -1
        override fun ordinal(): Int {
            return ordinal
        }

        fun setOrdinal(ordinal: Int) {
            this.ordinal = ordinal
        }
    }

    init {
        val codes: List<ErrorCode> = LinkedList()
        val values = ErrorCodes::class.java.fields
        val i = 0
        for (f in net.impactdev.gts.api.messaging.message.errors.values) {
            // ignore non-static fields
            if (!Modifier.isStatic(net.impactdev.gts.api.messaging.message.errors.f.getModifiers())) {
                continue
            }

            // ignore fields that aren't error codes
            if (!ErrorCode::class.java.isAssignableFrom(net.impactdev.gts.api.messaging.message.errors.f.getType())) {
                continue
            }
            try {
                // get the key instance
                val key = net.impactdev.gts.api.messaging.message.errors.f.get(null) as ErrorCodeBackend
                // set the ordinal value of the key.
                net.impactdev.gts.api.messaging.message.errors.key.ordinal =
                    net.impactdev.gts.api.messaging.message.errors.i++
                // add the key to the return map
                net.impactdev.gts.api.messaging.message.errors.codes.add(net.impactdev.gts.api.messaging.message.errors.key)
            } catch (e: Exception) {
                throw RuntimeException(
                    "Exception processing field: " + net.impactdev.gts.api.messaging.message.errors.f,
                    e
                )
            }
        }
        KEYS = ImmutableList.copyOf<ErrorCode>(net.impactdev.gts.api.messaging.message.errors.codes)
    }
}