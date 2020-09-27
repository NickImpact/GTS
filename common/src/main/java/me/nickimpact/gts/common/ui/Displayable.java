package me.nickimpact.gts.common.ui;

public interface Displayable<U, P> {

    U getView();

    void open(P player);

}
