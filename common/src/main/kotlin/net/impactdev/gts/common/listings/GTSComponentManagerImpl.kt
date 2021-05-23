package net.impactdev.gts.common.listings

import com.google.common.base.Preconditions
import com.google.common.collect.Maps
import net.impactdev.gts.api.data.ResourceManager
import net.impactdev.gts.api.data.Storable
import net.impactdev.gts.api.data.registry.DeserializerRegistry
import net.impactdev.gts.api.data.registry.GTSComponentManager
import net.impactdev.gts.api.data.registry.GTSKeyMarker
import net.impactdev.gts.api.listings.Listing
import net.impactdev.gts.api.listings.entries.Entry
import net.impactdev.gts.api.listings.entries.EntryManager
import net.impactdev.gts.api.listings.prices.Price
import net.impactdev.gts.api.listings.prices.PriceManager
import net.impactdev.gts.common.data.DeserializerRegistryImpl
import java.util.*
import java.util.function.Function
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.get
import kotlin.collections.set

class GTSComponentManagerImpl : GTSComponentManager {
    private val listings: MutableMap<Class<out Listing>?, ResourceManager<out Listing>?> = Maps.newHashMap()
    private val managers: MutableMap<GTSKeyMarker, EntryManager<out Entry<*, *>, *>> = LinkedHashMap()
    private val prices: MutableMap<GTSKeyMarker, PriceManager<out Price<*, *, *>, *>> = Maps.newHashMap()
    private val legacy: MutableMap<String, Storable.Deserializer<*>?> = Maps.newHashMap()
    override val deserializerRegistry: DeserializerRegistry = DeserializerRegistryImpl()
    override fun <T : Listing?> registerListingResourceManager(type: Class<T>?, deserializer: ResourceManager<T>?) {
        listings[type] = deserializer
    }

    override fun <T : Listing?> getListingResourceManager(type: Class<T>?): Optional<ResourceManager<T>?>? {
        return Optional.ofNullable(listings[type] as ResourceManager<T>?)
    }

    override val allListingResourceManagers: Map<Class<out Listing>?, ResourceManager<out Listing>?>
        get() = listings

    override fun <T : Entry<*, *>?> registerEntryManager(type: Class<T>?, manager: EntryManager<T, *>) {
        Preconditions.checkArgument(
            type!!.isAnnotationPresent(GTSKeyMarker::class.java),
            "An Entry type must be annotated with GTSKeyMarker"
        )
        managers[type.getAnnotation(GTSKeyMarker::class.java)] = manager
        manager.supplyDeserializers()
    }

    override fun <T : Entry<*, *>?> getEntryManager(key: String?): Optional<EntryManager<T, *>> {
        return managers.keys.stream()
            .filter { marker: GTSKeyMarker ->
                for (id in marker.value) {
                    if (id == key) {
                        return@filter true
                    }
                }
                false
            }
            .map { key: GTSKeyMarker? -> managers[key] }
            .findAny()
            .map(Function<EntryManager<Entry<*, *>, Any?>, EntryManager<T, *>?> { x: EntryManager<Entry<*, *>, Any?>? -> x as EntryManager<T, *>? })
    }

    override val allEntryManagers: Map<GTSKeyMarker, EntryManager<out Entry<*, *>, *>>
        get() = managers

    override fun <T : Price<*, *, *>?> registerPriceManager(type: Class<T>?, resource: PriceManager<T, *>) {
        Preconditions.checkArgument(
            type!!.isAnnotationPresent(GTSKeyMarker::class.java),
            "A Price type must be annotated with GTSKeyMarker"
        )
        prices[type.getAnnotation(GTSKeyMarker::class.java)] = resource
    }

    override fun <T : Price<*, *, *>?> getPriceManager(key: String?): Optional<PriceManager<T, *>> {
        return prices.keys.stream()
            .filter { marker: GTSKeyMarker ->
                for (id in marker.value) {
                    if (id == key) {
                        return@filter true
                    }
                }
                false
            }
            .map { key: GTSKeyMarker? -> prices[key] }
            .findAny()
            .map(Function<PriceManager<Price<*, *, *>, Any?>, PriceManager<T, *>?> { x: PriceManager<Price<*, *, *>, Any?>? -> x as PriceManager<T, *>? })
    }

    override val allPriceManagers: Map<GTSKeyMarker, PriceManager<out Price<*, *, *>, *>>
        get() = prices

    override fun <T : Entry<*, *>?> registerLegacyEntryDeserializer(
        key: String?,
        deserializer: Storable.Deserializer<T>?
    ) {
        legacy["legacy_$key"] = deserializer
    }

    override fun <T : Entry<*, *>?> getLegacyEntryDeserializer(key: String?): Optional<Storable.Deserializer<T>?>? {
        return Optional.ofNullable(legacy["legacy_$key"] as Storable.Deserializer<T>?)
    }
}