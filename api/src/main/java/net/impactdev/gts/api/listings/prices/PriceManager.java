package net.impactdev.gts.api.listings.prices;

import net.impactdev.gts.api.data.ResourceManager;
import net.impactdev.gts.api.listings.ui.EntryUI;
import net.impactdev.gts.api.util.TriConsumer;

import java.util.function.BiConsumer;

public interface PriceManager<T, P> extends ResourceManager<T> {

    TriConsumer<P, EntryUI<?, ?, ?>, BiConsumer<EntryUI<?, ?, ?>, Price<?, ?>>> process();

}
