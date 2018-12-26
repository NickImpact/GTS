package me.nickimpact.gts.api.exceptions;

import com.google.common.collect.Lists;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.GTSInfo;
import lombok.Getter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.*;
import java.util.List;

public class InvalidNBTException extends RuntimeException {

	@Getter
	private final String nbt;

	@Getter
	private final Player player;

	public InvalidNBTException(String nbt, Player player) {
		super("");
		this.nbt = nbt;
		this.player = player;
	}

	public void writeError() {
		List<Text> error = Lists.newArrayList(
				Text.of(GTSInfo.ERROR, TextColors.RED, "----------------------------"),
				Text.of(GTSInfo.ERROR, TextColors.RED, "|    Found Invalid NBT     |"),
				Text.of(GTSInfo.ERROR, TextColors.RED, "----------------------------")
		);
		error.add(Text.of(GTSInfo.ERROR, TextColors.RED, "\\              /"));
		error.add(Text.of(GTSInfo.ERROR, TextColors.RED, " \\            /"));
		error.add(Text.of(GTSInfo.ERROR, TextColors.RED, "  \\          /"));
		error.add(Text.of(GTSInfo.ERROR, TextColors.RED, "   \\        /"));
		error.add(Text.of(GTSInfo.ERROR, TextColors.RED, "    \\      /"));
		error.add(Text.of(GTSInfo.ERROR, TextColors.RED, "     \\    /"));
		error.add(Text.of(GTSInfo.ERROR, TextColors.RED, "      \\  /"));
		error.add(Text.of(GTSInfo.ERROR, TextColors.RED, "       \\/"));
		error.add(Text.of(GTSInfo.ERROR, TextColors.RED, "       /\\"));
		error.add(Text.of(GTSInfo.ERROR, TextColors.RED, "      /  \\"));
		error.add(Text.of(GTSInfo.ERROR, TextColors.RED, "     /    \\"));
		error.add(Text.of(GTSInfo.ERROR, TextColors.RED, "    /      \\"));
		error.add(Text.of(GTSInfo.ERROR, TextColors.RED, "   /        \\"));
		error.add(Text.of(GTSInfo.ERROR, TextColors.RED, "  /          \\"));
		error.add(Text.of(GTSInfo.ERROR, TextColors.RED, " /            \\"));
		error.add(Text.of(GTSInfo.ERROR, TextColors.RED, "/              \\"));

		error.addAll(Lists.newArrayList(
				Text.of(GTSInfo.ERROR, TextColors.RED, "----------------------------"),
				Text.of(GTSInfo.ERROR, TextColors.RED, "Invalid NBT was detected"),
				Text.of(GTSInfo.ERROR, TextColors.RED, "whilst trying to load a Pokemon."),
				Text.of(GTSInfo.ERROR, TextColors.RED, "A copy of this invalid NBT was"),
				Text.of(GTSInfo.ERROR, TextColors.RED, "written to a file named invalid.txt,"),
				Text.of(GTSInfo.ERROR, TextColors.RED, "located in the gts folder in your"),
				Text.of(GTSInfo.ERROR, TextColors.RED, "server's root directory."),
				Text.EMPTY,
				Text.of(GTSInfo.ERROR, TextColors.RED, "Please post this file as an"),
				Text.of(GTSInfo.ERROR, TextColors.RED, "attachment to the GTS issues"),
				Text.of(GTSInfo.ERROR, TextColors.RED, "page. This data will be used"),
				Text.of(GTSInfo.ERROR, TextColors.RED, "to help isolate and patch up"),
				Text.of(GTSInfo.ERROR, TextColors.RED, "the occurring issue.")
		));

		GTS.getInstance().getConsole().ifPresent(console -> console.sendMessages(error));

		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(new File("./gts/invalid.txt"), true)));
			writer.println("Owner of Pokemon: " + (this.player != null ? this.player.getName() : "Unknown"));
			writer.println(
					"------------------------------------------------------\n" +
					"                       NBT \n" +
					"------------------------------------------------------\n" +
					this.nbt +
					"------------------------------------------------------\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
