package net.impactdev.gts.api.data;

public interface ResourceManager<T> {

    String getName();

    /**
     * Represents the item ID that'll be used to reference the associated Entry type. This is purely
     * for the creation of the listing entry,
     *
     * @return The Minecraft Item ID that represents the item that should be used for entry creation
     */
    String getItemID();

    /**
     * Fetches the deserializer responsible for deserializing the resource into its desired typing.
     *
     * @return
     */
    Storable.Deserializer<T> getDeserializer();

}
