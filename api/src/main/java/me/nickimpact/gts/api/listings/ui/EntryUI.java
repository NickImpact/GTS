package me.nickimpact.gts.api.listings.ui;

import com.nickimpact.impactor.api.gui.UI;

public interface EntryUI<T> {

	EntryUI createFor(T user);

	UI getDisplay();

	void open(T user);

}
