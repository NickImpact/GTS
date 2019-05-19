package me.nickimpact.gts.reforged.entries;

import com.nickimpact.impactor.api.configuration.ConfigKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Getter
@AllArgsConstructor
public class KeyDetailHolder {
	private ConfigKey<List<String>> key;
	private Map<String, Function<CommandSource, Optional<Text>>> tokens;
}
