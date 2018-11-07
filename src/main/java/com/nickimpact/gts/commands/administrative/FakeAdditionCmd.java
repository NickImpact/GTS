package com.nickimpact.gts.commands.administrative;

import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.entries.pixelmon.PokemonEntry;
import com.nickimpact.gts.entries.prices.MoneyPrice;
import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.commands.SpongeSubCommand;
import com.nickimpact.impactor.api.commands.annotations.Aliases;
import com.nickimpact.impactor.api.commands.annotations.Permission;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.enums.items.EnumPokeballs;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.sql.Date;
import java.time.Instant;
import java.util.UUID;

@Aliases({"fakeaddition", "fake", "fakeadd", "fa"})
@Permission(admin = true)
public class FakeAdditionCmd extends SpongeSubCommand {

	private static final Text POKEMON = Text.of("pokemon");

	public FakeAdditionCmd(SpongePlugin plugin) {
		super(plugin);
	}

	@Override
	public CommandElement[] getArgs() {
		return new CommandElement[] {
				GenericArguments.string(POKEMON)
		};
	}

	@Override
	public Text getDescription() {
		return Text.of();
	}

	@Override
	public Text getUsage() {
		return Text.of("/gts admin fake (pokemon)");
	}

	@Override
	public SpongeCommand[] getSubCommands() {
		return new SpongeCommand[0];
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		String pokemon = args.<String>getOne(POKEMON).get();
		if (EnumPokemon.getFromNameAnyCase(pokemon) == null) {
			throw new CommandException(Text.of(pokemon, " does not exist in the pool of pokemon..."));
		}

		Listing.builder()
				.entry(new PokemonEntry((EntityPixelmon) PixelmonEntityList.createEntityByName(pokemon, (net.minecraft.world.World) Sponge.getServer().getWorld("world").get()), new MoneyPrice(500)))
				.player(UUID.randomUUID(), "Totally Legit User")
				.expiration(Date.from(Instant.now().plusSeconds(3600)))
				.build();

		src.sendMessages(Text.of(GTSInfo.PREFIX, "Fake listing added!"));

		return CommandResult.success();
	}
}
