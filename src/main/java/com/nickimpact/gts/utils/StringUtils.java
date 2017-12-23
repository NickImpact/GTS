package com.nickimpact.gts.utils;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class StringUtils {

    public static String capitalize(String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }
}
