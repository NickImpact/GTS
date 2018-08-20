package com.nickimpact.gts.utils;

import org.spongepowered.api.text.Text;

import java.util.List;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class StringUtils {

    public static String capitalize(String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    public static String listToString(List<Text> list) {
    	StringBuilder sb = new StringBuilder();
    	sb.append(list.get(0).toPlain());
    	for(int i = 1; i < list.size(); i++) {
    		sb.append("\n").append(list.get(i).toPlain());
	    }

    	return sb.toString();
    }
}
