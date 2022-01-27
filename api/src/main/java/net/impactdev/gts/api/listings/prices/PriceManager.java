package net.impactdev.gts.api.listings.prices;

import net.impactdev.gts.api.commands.CommandGenerator;
import net.impactdev.gts.api.data.ResourceManager;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.gts.api.util.TriConsumer;
import net.impactdev.impactor.api.gui.UI;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface PriceManager<T, P> extends ResourceManager<T> {

    TriConsumer<P, EntryUI<?, ?, ?>, BiConsumer<EntryUI<?, ?, ?>, Price<?, ?, ?>>> process();

    <U extends UI<?, ?, ?, ?>> Optional<PriceSelectorUI<U>> getSelector(P viewer, Price<?, ?, ?> price, Consumer<Object> callback);

    /**
     * Represents the executor that will handle processing of creating an entry from a command context.
     * This will be queried and attached as a child to the sell command at time of construction for the sell command.
     * To ensure readiness, this should be available before enable/initialization.
     *
     * @return The executor for the entry type when combined with /gts sell
     * @since 6.1.8
     */
    CommandGenerator.PriceGenerator<? extends Price<?, ?, ?>> getPriceCommandCreator();

    interface PriceSelectorUI<U extends UI<?, ?, ?, ?>> {

        U getDisplay();

        Consumer<Object> getCallback();

    }

}
