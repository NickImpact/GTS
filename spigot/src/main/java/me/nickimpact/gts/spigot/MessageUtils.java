package me.nickimpact.gts.spigot;

import me.nickimpact.gts.api.plugin.PluginInstance;
import org.bukkit.ChatColor;

import java.util.List;

public class MessageUtils {

	public static String parse(String input, boolean error) {
		if(error) {
			return ChatColor.YELLOW + "GTS " + ChatColor.GRAY + "(" + ChatColor.RED + "Error" + ChatColor.GRAY + ") " + ChatColor.translateAlternateColorCodes('&', input);
		} else {
			return ChatColor.YELLOW + "GTS " + ChatColor.GRAY + "\u00bb " + ChatColor.translateAlternateColorCodes('&', input);
		}
	}

	public static String[] asArray(List<String> input) {
		return input.toArray(new String[]{});
	}

	public static String asSingleWithNewlines(List<String> list) {
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
