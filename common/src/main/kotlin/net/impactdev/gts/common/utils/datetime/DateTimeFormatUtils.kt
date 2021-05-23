package net.impactdev.gts.common.utils.datetime

import net.impactdev.gts.api.listings.Listing
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateTimeFormatUtils {
    val base = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss a z").withZone(ZoneId.systemDefault())
    fun formatExpiration(listing: Listing): String {
        return if (listing.expiration == LocalDateTime.MAX) "Infinite" else listing.expiration!!.format(base)
    }
}