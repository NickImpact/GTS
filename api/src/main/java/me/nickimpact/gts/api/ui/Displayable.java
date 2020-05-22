package me.nickimpact.gts.api.ui;

public interface Displayable<P> {

	P getViewer();

	void open();

	void close();
}
