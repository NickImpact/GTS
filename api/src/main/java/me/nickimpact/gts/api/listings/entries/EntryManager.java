package me.nickimpact.gts.api.listings.entries;

import me.nickimpact.gts.api.data.Storable;
import me.nickimpact.gts.api.listings.ui.EntryUI;

import java.util.function.Supplier;

public interface EntryManager<T, P> extends Storable<T> {

    /**
     * Represents the item ID that'll be used to reference the associated Entry type. This is purely
     * for the creation of the listing entry,
     *
     * @return The Minecraft Item ID that represents the item that should be used for entry creation
     */
    String getItemID();

    /**
     * The UI that a user will use to create a listing specific to the particular Entry type. These
     * allow for customization of the selling menu, but it is advised you keep the components of the UI
     * similar so a player is not easily confused.
     *
     * @return The UI responsible for creating a new listing based on the type managed by this Entry Manager
     */
    Supplier<EntryUI<?, ?, ?>> getSellingUI(P player);

}
