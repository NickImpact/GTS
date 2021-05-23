package net.impactdev.gts.api.data.registry

import net.impactdev.gts.api.data.ResourceManager
import net.impactdev.gts.api.data.Storable
import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.listings.entries.Entry
import net.impactdev.gts.api.listings.entries.EntryManager
import net.impactdev.gts.api.listings.prices.Price
import net.impactdev.gts.api.listings.prices.PriceManager
import java.util.*

interface GTSComponentManager {
    // Listings
    fun <T : Listing?> registerListingResourceManager(type: Class<T>?, resource: ResourceManager<T>?)
    fun <T : Listing?> getListingResourceManager(type: Class<T>?): Optional<ResourceManager<T>?>?
    val allListingResourceManagers: Map<Class<out Listing?>?, ResourceManager<out Listing?>?>?

    // Entries
    fun <T : Entry<*, *>?> registerEntryManager(type: Class<T>?, manager: EntryManager<T, *>?)
    fun <T : Entry<*, *>?> getEntryManager(key: String?): Optional<EntryManager<T, *>?>?
    val allEntryManagers: Map<GTSKeyMarker?, EntryManager<out Entry<*, *>?, *>?>?

    // Prices
    fun <T : Price<*, *, *>?> registerPriceManager(type: Class<T>?, resource: PriceManager<T, *>?)
    fun <T : Price<*, *, *>?> getPriceManager(key: String?): Optional<PriceManager<T, *>?>?
    val allPriceManagers: Map<GTSKeyMarker?, PriceManager<out Price<*, *, *>?, *>?>?
    val deserializerRegistry: DeserializerRegistry?

    // Legacy
    @Deprecated("")
    fun <T : Entry<*, *>?> registerLegacyEntryDeserializer(key: String?, deserializer: Storable.Deserializer<T>?)

    @Deprecated("")
    fun <T : Entry<*, *>?> getLegacyEntryDeserializer(key: String?): Optional<Storable.Deserializer<T>?>?
}