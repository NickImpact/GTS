package me.nickimpact.gts.manager;

import org.bukkit.ChatColor;

import java.util.List;

public class TextParsingUtils {

	public String[] convertFromList(List<String> original) {
		return original.toArray(new String[]{});
	}

	public String normal(String input) {
		return ChatColor.YELLOW + "GTS " + ChatColor.GRAY + "\u00bb " +
				ChatColor.translateAlternateColorCodes('&', input);
	}

	public String error(String input) {
		return ChatColor.YELLOW + "GTS " + ChatColor.GRAY + "(" + ChatColor.RED + "Error" + ChatColor.GRAY + ") " +
				ChatColor.translateAlternateColorCodes('&', input);
	}
}
