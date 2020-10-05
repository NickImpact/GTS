package net.impactdev.gts.api.listings.ui;

import java.util.Optional;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public abstract class AbstractEntryUI<P, E, I> implements EntryUI<P, E, I> {

    protected final P viewer;
    protected E chosen;

    public AbstractEntryUI(P viewer) {
        this.viewer = viewer;
    }

    @Override
    public Optional<E> getChosenOption() {
        return Optional.ofNullable(this.chosen);
    }

}
