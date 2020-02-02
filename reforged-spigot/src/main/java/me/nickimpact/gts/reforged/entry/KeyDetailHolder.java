package me.nickimpact.gts.reforged.entry;

import com.nickimpact.impactor.api.configuration.ConfigKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Getter
@AllArgsConstructor
public class KeyDetailHolder {
	private ConfigKey<List<String>> key;
	private Map<String, Function<CommandSender, Optional<String>>> tokens;
}
