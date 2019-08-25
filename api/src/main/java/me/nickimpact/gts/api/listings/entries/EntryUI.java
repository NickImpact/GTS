package me.nickimpact.gts.api.listings.entries;

import com.nickimpact.impactor.api.gui.UI;

public interface EntryUI<T> {

	EntryUI createFor(T player);

	UI getDisplay();

}
