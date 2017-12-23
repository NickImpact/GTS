package com.nickimpact.gts.ui.updater;

import java.util.Observable;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class GuiUpdater extends Observable {

	public void sendUpdate() {
		this.setChanged();
		this.notifyObservers();
	}
}
