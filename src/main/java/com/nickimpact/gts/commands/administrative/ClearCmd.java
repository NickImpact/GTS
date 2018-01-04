package com.nickimpact.gts.commands.administrative;

import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.commands.annotations.AdminCmd;
import com.nickimpact.gts.api.commands.annotations.CommandAliases;
import com.nickimpact.gts.api.commands.SpongeCommand;
import com.nickimpact.gts.api.commands.SpongeSubCommand;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.entries.EntryHolder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@AdminCmd
@CommandAliases({"clear"})
public class ClearCmd extends SpongeSubCommand {

	@Override
	public CommandElement[] getArgs() {
		return new CommandElement[0];
	}

	@Override
	public Text getDescription() {
		return Text.of("Clears all current listings");
	}

	@Override
	public Text getUsage() {
		return Text.of("/gts admin clear");
	}

	@Override
	public SpongeCommand[] getSubCommands() {
		return new SpongeCommand[0];
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		for(Listing listing : GTS.getInstance().getListingsCache()) {
			if(!listing.getEntry().supportsOffline()) {
				if(!Sponge.getServer().getPlayer(listing.getOwnerUUID()).isPresent()) {
					GTS.getInstance().getStorage().addHeldElement(new EntryHolder(listing.getID(), listing.getOwnerUUID(), listing.getEntry()));
				} else {
					listing.getEntry().giveEntry(Sponge.getServer().getPlayer(listing.getOwnerUUID()).get());
				}
			} else {
				listing.getEntry().giveEntry(GTS.getInstance().getUserStorageService().get(listing.getOwnerUUID()).get());
			}
		}

		GTS.getInstance().getListingsCache().clear();
		GTS.getInstance().getStorage().purge(false);
		return CommandResult.success();
	}
}
