package me.nickimpact.gts.api.listings.ui;

import me.nickimpact.gts.api.listings.prices.Price;

import java.util.Optional;

public interface EntryUI<P, E, I> {

	Optional<E> getChosenOption();

	void open(P user);

	I generateWaitingIcon(boolean auction);

	I generateConfirmIcon();

	I createNoneChosenIcon();

	I createChosenIcon();

	I createPriceIcon();

	I createSelectionTypeIcon();

	I createTimeIcon();

	void style(boolean selected);

}
