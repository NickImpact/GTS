package me.nickimpact.gts.api.listings.ui;

import me.nickimpact.gts.api.listings.entries.Entry;

public interface EntrySelection<T extends Entry<?, ?>> {

    T createFromSelection();

}
