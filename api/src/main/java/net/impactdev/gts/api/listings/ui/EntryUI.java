package net.impactdev.gts.api.listings.ui;

import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.ui.containers.icons.Icon;

import java.util.Optional;

public interface EntryUI<E> {

	Optional<E> getChosenOption();

	void open(PlatformPlayer user);

	Icon<?> generateWaitingIcon(boolean auction);

	Icon<?> generateConfirmIcon();

	Icon<?> createNoneChosenIcon();

	Icon<?> createChosenIcon();

	Icon<?> createPriceIcon();

	Icon<?> createTimeIcon();

	void style(boolean selected);

}
