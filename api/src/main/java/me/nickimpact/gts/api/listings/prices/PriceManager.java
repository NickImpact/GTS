package me.nickimpact.gts.api.listings.prices;

import me.nickimpact.gts.api.data.ResourceManager;
import me.nickimpact.gts.api.listings.ui.EntryUI;
import me.nickimpact.gts.api.util.TriConsumer;

import java.util.function.BiConsumer;

public interface PriceManager<T, P> extends ResourceManager<T> {

    TriConsumer<P, EntryUI<?, ?, ?>, BiConsumer<EntryUI<?, ?, ?>, Price<?, ?>>> process();

}
