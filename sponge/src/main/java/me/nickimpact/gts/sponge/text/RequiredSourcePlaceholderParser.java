package me.nickimpact.gts.sponge.text;

import io.github.nucleuspowered.nucleus.api.placeholder.Placeholder;
import io.github.nucleuspowered.nucleus.api.placeholder.PlaceholderParser;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.function.Function;


@RequiredArgsConstructor
public class RequiredSourcePlaceholderParser implements PlaceholderParser {

	private final Function<CommandSource, Text> output;

	@Override
	public Text parse(Placeholder.Standard placeholder) {
		if(placeholder.getAssociatedSource().isPresent()) {
			return output.apply(placeholder.getAssociatedSource().get());
		}
		return Text.EMPTY;
	}

}
