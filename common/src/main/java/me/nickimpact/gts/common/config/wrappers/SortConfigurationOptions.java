package me.nickimpact.gts.common.config.wrappers;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
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

}
