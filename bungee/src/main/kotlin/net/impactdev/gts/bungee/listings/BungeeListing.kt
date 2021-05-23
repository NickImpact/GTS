package net.impactdev.gts.bungee.listings

import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.listings.entries.Entry
import net.impactdev.gts.common.listings.JsonStoredEntry
import net.impactdev.impactor.api.json.factory.JObject
import java.time.LocalDateTime
import java.util.*

abstract class BungeeListing(
    override val iD: UUID?,
    override val lister: UUID?,
    private override val entry: JsonStoredEntry?,
    override val publishTime: LocalDateTime?,
    override val expiration: LocalDateTime?
) : Listing {

    constructor(id: UUID?, lister: UUID?, entry: JsonStoredEntry?, expiration: LocalDateTime?) : this(
        id,
        lister,
        entry,
        LocalDateTime.now(),
        expiration
    ) {
    }

    override fun getEntry(): Entry<*, *>? {
        return entry
    }

    override val version: Int
        get() = 1

    override fun serialize(): JObject? {
        val timings = JObject()
            .add("published", publishTime.toString())
            .add("expiration", expiration.toString())
        return JObject()
            .add("id", iD.toString())
            .add("lister", lister.toString())
            .add("version", version)
            .add("timings", timings)
            .add("entry", entry.getOrCreateElement())
    }
}