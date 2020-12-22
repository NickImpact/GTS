package net.impactdev.gts.api.listings.entries;

import net.impactdev.gts.api.data.ResourceManager;
import net.impactdev.gts.api.listings.ui.EntryUI;

import java.util.function.Supplier;

public interface EntryManager<T, P> extends ResourceManager<T> {

    Class<?> getBlacklistType();

    /**
     * The UI that a user will use to create a listing specific to the particular Entry type. These
     * allow for customization of the selling menu, but it is advised you keep the components of the UI
     * similar so a player is not easily confused.
     *
     * @return The UI responsible for creating a new listing based on the type managed by this Entry Manager
     */
    Supplier<EntryUI<?, ?, ?>> getSellingUI(P player);

    /**
     *
     */
    void supplyDeserializers();
}
