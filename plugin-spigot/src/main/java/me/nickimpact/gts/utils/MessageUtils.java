package me.nickimpact.gts.utils;

import me.nickimpact.gts.GTS;

import java.util.List;

public class MessageUtils {

	public static String parse(String input, boolean error) {
		if(error) {
			return GTS.getInstance().getTextParsingUtils().error(input);
		} else {
			return GTS.getInstance().getTextParsingUtils().normal(input);
		}
	}

	public static String[] asArray(List<String> input) {
		return GTS.getInstance().getTextParsingUtils().convertFromList(input);
	}

	public static String asSingleWithNewlines(List<String> list) {
		StringBuilder sb = new StringBuilder();
		if(list.size() > 0) {
			sb.append(list.get(0));
			for (int i = 1; i < list.size(); i++) {
				sb.append("\n").append(list.get(i));
			}
		}

		return sb.toString();
	}
}
