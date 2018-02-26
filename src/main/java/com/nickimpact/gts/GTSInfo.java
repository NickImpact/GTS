package com.nickimpact.gts;

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
    public static final String VERSION = "3.6.0-S7.1";
    public static final String DESCRIPTION = "A Sponge Representation of the Global Trading Station";

    public static final Text PREFIX = Text.of(TextColors.YELLOW, "GTS ", TextColors.GRAY, "\u00bb ", TextColors.DARK_AQUA);
    public static final Text ERROR = Text.of(
            TextColors.RED, "GTS ", TextColors.GRAY, "(", TextColors.RED, "Error", TextColors.GRAY, ") ",
            TextColors.DARK_RED
    );
    public static final Text DEBUG = Text.of(
            TextColors.YELLOW, "GTS ", TextColors.GRAY, "(", TextColors.RED, "Debug", TextColors.GRAY, ") ",
            TextColors.DARK_AQUA
    );
    public static final Text WARNING = Text.of(
            TextColors.YELLOW, "GTS ", TextColors.GRAY, "(", TextColors.RED, "Warning", TextColors.GRAY, ") ",
            TextColors.DARK_AQUA
    );


    public enum Dependencies {
        Nucleus("nucleus", "Nucleus-1.2.0-S7.0+"),
        Pixelmon("pixelmon", "6.x.x+");

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
                "    &e  _______ .___________.    _______.\n" +
                "    &e /  _____||           |   /       |\n" +
                "    &e|  |  __  `---|  |----`  |   (----`\n" +
                "    &e|  | |_ |     |  |        \\   \\    \n" +
                "    &e|  |__| |     |  |    .----)   |   \n" +
                "    &e \\______|     |__|    |_______/    \n ";

        for(String s : logo.split(Pattern.quote("\n")))
            GTS.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(TextSerializers.FORMATTING_CODE.deserialize(s))));

        GTS.getInstance().getConsole().ifPresent(console -> console.sendMessages(
                Text.of("    ", TextColors.YELLOW, "Rise of the Doof Edition"),
                Text.EMPTY,
                Text.of("    ", TextColors.GREEN, "Author:  ", TextColors.AQUA, "NickImpact"),
                Text.of("    ", TextColors.GREEN, "Version: ", TextColors.AQUA, VERSION),
                Text.EMPTY
        ));

        GTS.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.EMPTY));
    }

    static boolean dependencyCheck(){
    	boolean valid = true;

        for(Dependencies dependency : Dependencies.values()){
            if(!Sponge.getPluginManager().isLoaded(dependency.getDependency())){
                GTS.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(ERROR, Text.of(TextColors.DARK_RED, "==== Missing Dependency ===="))));
                GTS.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(ERROR, Text.of(TextColors.DARK_RED, "  Dependency: ", TextColors.RED, dependency.name()))));
                GTS.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(ERROR, Text.of(TextColors.DARK_RED, "  Version: ", TextColors.RED, dependency.getVersion()))));

                valid = false;
            }
        }
        return valid;
    }
}
