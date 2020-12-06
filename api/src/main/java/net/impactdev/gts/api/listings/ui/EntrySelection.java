package net.impactdev.gts.api.listings.ui;

import net.impactdev.gts.api.listings.entries.Entry;

public interface EntrySelection<T extends Entry<?, ?>> {

    T createFromSelection();

}
