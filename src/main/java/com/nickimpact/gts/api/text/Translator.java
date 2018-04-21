package com.nickimpact.gts.api.text;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.Map;
import java.util.Optional;

@FunctionalInterface
public interface Translator {
	Optional<Text> get(CommandSource src, String token, Map<String, Object> variables);
}
