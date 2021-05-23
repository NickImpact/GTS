package net.impactdev.gts.api.data

interface ResourceManager<T> {
    val name: String?

    /**
     * Represents the item ID that'll be used to reference the associated Entry type. This is purely
     * for the creation of the listing entry,
     *
     * @return The Minecraft Item ID that represents the item that should be used for entry creation
     */
    val itemID: String?

    /**
     *
     *
     * @return
     */
    val deserializer: Storable.Deserializer<T>?
}