package me.nickimpact.gts;

import com.nickimpact.impactor.api.plugins.PluginInfo;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.regex.Pattern;

/**
 * Created by Nick on 12/15/2016.
 */
public class GTSInfo implements PluginInfo {

	public static final String ID = "gts";
	public static final String NAME = "GTS";
	public static final String VERSION = "3.11.3-S7.1";
	public static final String DESCRIPTION = "A Sponge Representation of the Global Trading Station";

	public static final Text PREFIX = Text.of(TextColors.YELLOW, "GTS ", TextColors.GRAY, "\u00bb ");
	public static final Text ERROR = Text.of(TextColors.RED, "GTS ", TextColors.GRAY, "(", TextColors.RED, "Error", TextColors.GRAY, ") ");
	public static final Text DEBUG = Text.of(TextColors.YELLOW, "GTS ", TextColors.GRAY, "(", TextColors.RED, "Debug", TextColors.GRAY, ") ");
	public static final Text WARNING = Text.of(TextColors.YELLOW, "GTS ", TextColors.GRAY, "(", TextColors.RED, "Warning", TextColors.GRAY, ") ");

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	static void displayBanner(){
		String logo =
				"\n" +
						"       _________________\n" +
						"      / ____/_  __/ ___/       &aGTS &bV" + VERSION + "\n" +
						"     / / __  / /  \\__ \\      &8Running on &e" + Sponge.getGame().getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getName() + " V" + Sponge.getGame().getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getVersion().get() + "\n" +
						"    / /_/ / / /  ___/ / \n     &aAuthor: &bNickImpact" +
						"    \\____/ /_/  /____/  \n" +
						"\n";

		for(String s : logo.split(Pattern.quote("\n"))) {
			GTS.getInstance().getConsole().ifPresent(console -> console.sendMessages(Text.of(TextColors.DARK_AQUA, TextSerializers.FORMATTING_CODE.deserialize(s))));
		}
	}
}
