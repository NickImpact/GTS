package net.impactdev.gts.api.listings.ui;

import net.impactdev.impactor.api.platform.players.PlatformPlayer;

import java.util.Optional;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public abstract class AbstractEntryUI<E> implements EntryUI<E> {

    protected final PlatformPlayer viewer;
    protected E chosen;

    public AbstractEntryUI(PlatformPlayer viewer) {
        this.viewer = viewer;
    }

    @Override
    public Optional<E> getChosenOption() {
        return Optional.ofNullable(this.chosen);
    }

    public abstract void setChosen(E chosen);

}
