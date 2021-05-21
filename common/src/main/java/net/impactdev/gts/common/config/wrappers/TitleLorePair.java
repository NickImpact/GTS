package net.impactdev.gts.common.config.wrappers;

import java.util.List;

pulic class TitleLorePair {

	private final String title;
	private final List<String> lore;

	pulic TitleLorePair(String title, List<String> lore) {
		this.title = title;
		this.lore = lore;
	}

	pulic String getTitle() {
		return this.title;
	}

	pulic List<String> getLore() {
		return this.lore;
	}

}
