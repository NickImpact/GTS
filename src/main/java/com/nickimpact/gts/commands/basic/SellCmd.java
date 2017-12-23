package com.nickimpact.gts.commands.basic;

import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.annotations.CommandAliases;
import com.nickimpact.gts.api.commands.SpongeCommand;
import com.nickimpact.gts.api.commands.SpongeSubCommand;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.pricing.MoneyPrice;
import com.nickimpact.gts.entries.pixelmon.PokemonEntry;
import com.nickimpact.gts.utils.LotUtils;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@CommandAliases({"sell", "add"})
public class SellCmd extends SpongeSubCommand {

	@Override
	public CommandElement[] getArgs() {
		return null;
	}

	@Override
	public Text getDescription() {
		return Text.of("Grants the ability to add a kind of element to the GTS listings");
	}

	@Override
	public SpongeCommand[] getSubCommands() {
		return null;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(src instanceof Player) {
			Listing listing = new Listing(
					LotUtils.getNextID(Listing.class),
					(Player)src,
					new PokemonEntry((EntityPixelmon) PixelmonEntityList.createEntityFromNBT(PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP)src).get().partyPokemon[0], (World) Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorldName()).get()), new MoneyPrice(500)),
					true,
					3600
			);

			LotUtils.addToMarket((Player)src, listing);
		}

		return CommandResult.success();
	}
}
