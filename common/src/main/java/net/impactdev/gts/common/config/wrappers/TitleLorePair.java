package net.impactdev.gts.common.config.wrappers;

import java.util.List;

public class TitleLorePair {

	private final String title;
	private final List<String> lore;

	public TitleLorePair(String title, List<String> lore) {
		this.title = title;
		this.lore = lore;
	}

	public String getTitle() {
		return this.title;
	}

	public List<String> getLore() {
		return this.lore;
	}

}
