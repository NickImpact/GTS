package com.nickimpact.gts.api.commands;

/**
 * Like it's parent, the SpongeSubCommand class is used primarily for fast class path loaders.
 * We use this class as a method to avoid registering the same command twice, once as a child,
 * and again as it's own command.
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
