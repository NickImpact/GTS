package me.nickimpact.gts.utils;

import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.stream.Collectors;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class StringUtils {

    public static String capitalize(String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    public static String textListToString(List<Text> list) {
    	return stringListToString(list.stream().map(Text::toPlain).collect(Collectors.toList()));
    }

    public static String stringListToString(List<String> list) {
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
