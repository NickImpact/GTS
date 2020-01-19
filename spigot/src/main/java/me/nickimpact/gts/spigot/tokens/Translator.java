package me.nickimpact.gts.spigot.tokens;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;

@FunctionalInterface
public interface Translator {
	Optional<String> get(CommandSender source, String token, Map<String, Object> variables);
}
