package net.impactdev.gts.util;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MiniMessageColorTranslator {

    private static final char HEX = '#';
    private static final Map<String, TextColor> COLOR_ALIASES = new HashMap<>();

    static {
        COLOR_ALIASES.put("dark_grey", NamedTextColor.DARK_GRAY);
        COLOR_ALIASES.put("grey", NamedTextColor.GRAY);
    }

    public static TextColor resolve(final @NotNull String argument) {
        if(!has(argument)) {
            return null;
        }

        if(COLOR_ALIASES.containsKey(argument)) {
            return COLOR_ALIASES.get(argument);
        } else if(argument.charAt(0) == HEX) {
            return TextColor.fromHexString(argument);
        } else {
            return NamedTextColor.NAMES.value(argument);
        }
    }

    public static boolean has(final @NotNull String argument) {
        return TextColor.fromHexString(argument) != null
                || NamedTextColor.NAMES.value(argument) != null
                || COLOR_ALIASES.containsKey(argument);
    }

}
