package net.impactdev.gts.common.data

import net.impactdev.gts.api.data.ResourceManager
import net.impactdev.gts.api.data.Storable

class ResourceManagerImpl<T>(
    override val name: String,
    override val itemID: String,
    override val deserializer: Storable.Deserializer<T>
) : ResourceManager<T>