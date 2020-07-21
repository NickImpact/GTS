package me.nickimpact.gts.api.listings.ui;

import java.util.Optional;

public interface EntryUI<T, E> {

	Optional<E> getChosenOption();

	void open(T user);

}
