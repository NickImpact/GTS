package me.nickimpact.gts.api.listings.ui;

import java.util.Optional;

public interface EntryUI<P, E, I> {

	Optional<E> getChosenOption();

	void open(P user);

	I generateWaitingIcon(boolean auction);

	I generateConfirmIcon();

	I createNoneChosenIcon();

	I createChosenIcon();

	void style(boolean selected);

}
