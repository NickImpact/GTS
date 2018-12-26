package me.nickimpact.gts.commands.administrative;

import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.Listing;
import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.commands.SpongeSubCommand;
import com.nickimpact.impactor.api.commands.annotations.Aliases;
import com.nickimpact.impactor.api.commands.annotations.Permission;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
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
@Aliases({"clear"})
@Permission(admin = true)
public class ClearCmd extends SpongeSubCommand {

	public ClearCmd(SpongePlugin plugin) {
		super(plugin);
	}

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
	public CommandResult execute(CommandSource src, CommandContext args) {
		for(Listing listing : GTS.getInstance().getListingsCache()) {
			if(!listing.getEntry().supportsOffline()) {
				if(!Sponge.getServer().getPlayer(listing.getOwnerUUID()).isPresent()) {
//					GTS.getInstance().getStorage().addHeldElement(new EntryHolder(listing.getUuid(), listing.getOwnerUUID(), listing.getEntry()));
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
