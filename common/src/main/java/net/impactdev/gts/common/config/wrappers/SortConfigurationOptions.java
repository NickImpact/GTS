package net.impactdev.gts.common.config.wrappers;

public class SortConfigurationOptions {

	private String title;

	private String selectedColor;
	private String nonSelectedColor;

	private String qpMostRecent;
	private String qpEndingSoon;

	private String aHighest;
	private String aLowest;
	private String aEndingSoon;
	private String aMostBids;

	public SortConfigurationOptions(String title, String selectedColor, String nonSelectedColor, String qpMostRecent, String qpEndingSoon, String aHighest, String aLowest, String aEndingSoon, String aMostBids) {
		this.title = title;
		this.selectedColor = selectedColor;
		this.nonSelectedColor = nonSelectedColor;
		this.qpMostRecent = qpMostRecent;
		this.qpEndingSoon = qpEndingSoon;
		this.aHighest = aHighest;
		this.aLowest = aLowest;
		this.aEndingSoon = aEndingSoon;
		this.aMostBids = aMostBids;
	}

	public String getTitle() {
		return this.title;
	}

	public String getSelectedColor() {
		return this.selectedColor;
	}

	public String getNonSelectedColor() {
		return this.nonSelectedColor;
	}

	public String getQpMostRecent() {
		return this.qpMostRecent;
	}

	public String getQpEndingSoon() {
		return this.qpEndingSoon;
	}

	public String getAHighest() {
		return this.aHighest;
	}

	public String getALowest() {
		return this.aLowest;
	}

	public String getAEndingSoon() {
		return this.aEndingSoon;
	}

	public String getAMostBids() {
		return this.aMostBids;
	}
}
