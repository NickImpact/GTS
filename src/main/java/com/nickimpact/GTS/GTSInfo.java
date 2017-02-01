package com.nickimpact.GTS;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Created by Nick on 12/15/2016.
 */
public class GTSInfo {

    private GTSInfo() {}

    public static final String ID = "gts";

    public static final String NAME = "GTS";
    public static final String VERSION = "Sponge 1.2.4 [API 5]";
    public static final String DESCRIPTION = "A Sponge Representation of the Global Trading Station";

    public static final Text MESSAGE_PREFIX = Text.of(TextColors.GREEN, "[" + NAME + "]");
    public static final Text ERROR_MESSAGE_PREFIX = Text.of(TextColors.RED, "[" + NAME + "]");
}
