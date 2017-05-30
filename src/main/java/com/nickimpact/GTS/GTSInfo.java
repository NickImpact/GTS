package com.nickimpact.GTS;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.regex.Pattern;

/**
 * Created by Nick on 12/15/2016.
 */
public class GTSInfo {

    private GTSInfo() {}

    public static final String ID = "gts";

    public static final String NAME = "GTS";
    public static final String VERSION = "2.3.0-SNAPSHOT";
    public static final String DESCRIPTION = "A Sponge Representation of the Global Trading Station";

    public static final Text PREFIX = Text.of(TextColors.YELLOW, "GTS ", TextColors.GRAY, "\u00bb ", TextColors.DARK_AQUA);
    public static final Text ERROR_PREFIX = Text.of(
            TextColors.RED, "GTS ", TextColors.GRAY, "(", TextColors.RED, "Error", TextColors.GRAY, ") ",
            TextColors.DARK_RED
    );
    public static final Text DEBUG_PREFIX = Text.of(
            TextColors.YELLOW, "GTS ", TextColors.GRAY, "(", TextColors.RED, "Debug", TextColors.GRAY, ") ",
            TextColors.DARK_AQUA
    );

    public enum Dependencies {
        Pixelmon("pixelmon", "5.0.2+");

        private String dependency;
        private String version;

        private Dependencies(String dependency, String version){
            this.dependency = dependency;
            this.version = version;
        }


        public String getDependency() {
            return dependency;
        }

        public String getVersion() {
            return version;
        }
    }

    static void startup(){
        String logo = "\n" +
                "      &e  _______ .___________.    _______.\n" +
                "      &e /  _____||           |   /       |\n" +
                "      &e|  |  __  `---|  |----`  |   (----`\n" +
                "      &e|  | |_ |     |  |        \\   \\    \n" +
                "      &e|  |__| |     |  |    .----)   |   \n" +
                "      &e \\______|     |__|    |_______/    \n ";

        for(String s : logo.split(Pattern.quote("\n")))
            GTS.getInstance().getConsole().sendMessage(Text.of(TextSerializers.FORMATTING_CODE.deserialize(s)));

        GTS.getInstance().getConsole().sendMessage(Text.of(
                "    ", TextColors.GREEN, "Author:  ", TextColors.AQUA, "NickImpact"
        ));

        GTS.getInstance().getConsole().sendMessage(Text.of(
                "    ", TextColors.GREEN, "Version: ",
                TextColors.AQUA, VERSION
        ));

        GTS.getInstance().getConsole().sendMessage(Text.EMPTY);
    }

    static boolean dependencyCheck(){
        for(Dependencies dependency : Dependencies.values()){
            if(!Sponge.getPluginManager().isLoaded(dependency.getDependency())){
                GTS.getInstance().getConsole().sendMessage(Text.of(ERROR_PREFIX, Text.of(TextColors.DARK_RED, "==== Missing Dependency ====")));
                GTS.getInstance().getConsole().sendMessage(Text.of(ERROR_PREFIX, Text.of(TextColors.DARK_RED, "  Dependency: ", TextColors.RED, dependency.getDependency())));
                GTS.getInstance().getConsole().sendMessage(Text.of(ERROR_PREFIX, Text.of(TextColors.DARK_RED, "  Version Needed: ", TextColors.RED, dependency.getVersion())));

                return false;
            }
        }
        return true;
    }
}
