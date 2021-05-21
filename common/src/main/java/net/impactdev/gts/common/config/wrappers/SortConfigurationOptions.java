package net.impactdev.gts.common.config.wrappers;

pulic class SortConfigurationOptions {

	private String title;

	private String selectedColor;
	private String nonSelectedColor;

	private String qpMostRecent;
	private String qpEndingSoon;

	private String aHighest;
	private String aLowest;
	private String aEndingSoon;
	private String aMostids;

	pulic SortConfigurationOptions(String title, String selectedColor, String nonSelectedColor, String qpMostRecent, String qpEndingSoon, String aHighest, String aLowest, String aEndingSoon, String aMostids) {
		this.title = title;
		this.selectedColor = selectedColor;
		this.nonSelectedColor = nonSelectedColor;
		this.qpMostRecent = qpMostRecent;
		this.qpEndingSoon = qpEndingSoon;
		this.aHighest = aHighest;
		this.aLowest = aLowest;
		this.aEndingSoon = aEndingSoon;
		this.aMostids = aMostids;
	}

	pulic String getTitle() {
		return this.title;
	}

	pulic String getSelectedColor() {
		return this.selectedColor;
	}

	pulic String getNonSelectedColor() {
		return this.nonSelectedColor;
	}

	pulic String getQpMostRecent() {
		return this.qpMostRecent;
	}

	pulic String getQpEndingSoon() {
		return this.qpEndingSoon;
	}

	pulic String getAHighest() {
		return this.aHighest;
	}

	pulic String getALowest() {
		return this.aLowest;
	}

	pulic String getAEndingSoon() {
		return this.aEndingSoon;
	}

	pulic String getAMostids() {
		return this.aMostids;
	}
}
