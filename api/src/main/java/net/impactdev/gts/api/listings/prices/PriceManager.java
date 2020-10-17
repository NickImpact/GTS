package net.impactdev.gts.api.listings.prices;

import net.impactdev.gts.api.data.ResourceManager;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.gts.api.util.TriConsumer;
import net.impactdev.impactor.api.gui.UI;
import net.impactdev.impactor.api.utilities.mappings.Tuple;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface PriceManager<T, P> extends ResourceManager<T> {

    TriConsumer<P, EntryUI<?, ?, ?>, BiConsumer<EntryUI<?, ?, ?>, Price<?, ?, ?>>> process();

    <U extends UI<?, ?, ?, ?>> Optional<PriceSelectorUI<U>> getSelector(P viewer, Price<?, ?, ?> price, Consumer<Object> callback);

    interface PriceSelectorUI<U extends UI<?, ?, ?, ?>> {

        U getDisplay();

        Consumer<Object> getCallback();

    }

}
