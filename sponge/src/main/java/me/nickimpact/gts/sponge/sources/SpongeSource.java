package me.nickimpact.gts.sponge.sources;

import lombok.RequiredArgsConstructor;
import me.nickimpact.gts.api.user.Source;
import org.spongepowered.api.command.CommandSource;

@RequiredArgsConstructor
public class SpongeSource implements Source<CommandSource> {

	private final CommandSource delegate;

	@Override
	public CommandSource getSource() {
		return this.delegate;
	}
}
