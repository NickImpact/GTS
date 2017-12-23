package com.nickimpact.gts.api.commands;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public abstract class SpongeSubCommand extends SpongeCommand {

	/**
	 * Inherited by {@link SpongeCommand}, and set to do nothing to help
	 * ensure we don't register the same command twice, once as a subcommand,
	 * and once as it's own thing.
	 */
	@Override
	public void register() {
		// Command registry is dropped for sub-commands
	}
}
