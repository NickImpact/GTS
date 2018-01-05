package com.nickimpact.gts.entries.pixelmon;

import com.google.common.collect.Lists;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.commands.SpongeCommand;
import com.nickimpact.gts.api.commands.SpongeSubCommand;
import com.nickimpact.gts.api.commands.annotations.CommandAliases;
import com.nickimpact.gts.api.json.Typing;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.entries.Entry;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.api.utils.MessageUtils;
import com.nickimpact.gts.configuration.ConfigKeys;
import com.nickimpact.gts.entries.prices.MoneyPrice;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerComputerStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.List;
import java.util.Optional;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Typing("Pokemon")
public class PokemonEntry extends Entry<Pokemon> {

	public PokemonEntry() {
		super();
	}

	public PokemonEntry(EntityPixelmon pokemon, Price price) {
		this(new Pokemon(pokemon), price);
	}

	public PokemonEntry(Pokemon pokemon, Price price) {
		super(pokemon, price);
	}

	@Override
	public SpongeSubCommand commandSpec() {
		return new PokemonSub();
	}

	@Override
	public String getSpecsTemplate() {
		return "{{ability}} {{ivs_percent}} IV {{shiny:s}}&a{{pokemon}}";
	}

	@Override
	public String getName() {
		return this.getElement().getPokemon().getName();
	}

	@Override
	public ItemStack baseItemStack(Player player) {
		return getPicture(this.getElement().getPokemon());
	}

	@Override
	public String baseTitleTemplate() {
		return "&e{{pokemon}} {{shiny:s}}&7| &bLvl {{level}}";
	}

	@Override
	public List<String> baseLoreTemplate() {
		List<String> template = Lists.newArrayList(
				"&7Listing ID: &e{{id}}",
				"&7Seller: &e{{seller}}",
				"",
				"&7Ability: &e{{ability}}",
				"&7Gender: &e{{gender}}",
				"&7Nature: &e{{nature}}",
				"&7Size: &e{{growth}}"
		);

		if(this.getElement().getPokemon().getSpecies().equals(EnumPokemon.Mew)) {
			template.add("&7Clones: &e{{clones}}");
		}

		template.addAll(Lists.newArrayList(
				"",
				"&7Price: &e{{price}}",
				"&7Time Left: &e{{time_left}}"
		));

		return template;
	}

	@Override
	public ItemStack confirmItemStack(Player player) {
		return getPicture(this.getElement().getPokemon());
	}

	@Override
	public String confirmTitleTemplate() {
		return "&ePurchase {{pokemon}}?";
	}

	@Override
	public List<String> confirmLoreTemplate() {
		return Lists.newArrayList(
				"&7Here's some additional info:",
				"&7EVs: &e{{evs_total}}&7/&e510 &7(&a{{evs_percent}}&7)",
				"&7IVs: &e{{ivs_total}}&7/&e186 &7(&a{{ivs_percent}}&7)",
				"",
				"&7Move Set:",
				"  &7 - &e{{moves_1}}",
				"  &7 - &e{{moves_2}}",
				"  &7 - &e{{moves_3}}",
				"  &7 - &e{{moves_4}}"
		);
	}

	@Override
	public boolean giveEntry(User user) {
		if(Sponge.getServer().getPlayer(user.getUniqueId()).isPresent()) {
			Optional<PlayerStorage> optStorage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID(
					(MinecraftServer) Sponge.getServer(),
					user.getUniqueId()
			);

			if (!optStorage.isPresent())
				return false;

			optStorage.get().addToParty(this.getElement().getPokemon());
			optStorage.get().sendUpdatedList();
		} else {
			Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer)Sponge.getServer(), user.getUniqueId());
			PlayerComputerStorage computerStorage = PixelmonStorage.computerManager.getPlayerStorageOffline((MinecraftServer)Sponge.getServer(), user.getUniqueId());

			if(storage.isPresent()) {
				if(storage.get().count() == 6) {
					computerStorage.addToComputer(this.getElement().getPokemon().writeToNBT(new NBTTagCompound()));
					PixelmonStorage.computerManager.savePlayer(computerStorage);
				} else {
					storage.get().addToParty(this.getElement().getPokemon());
					PixelmonStorage.pokeBallManager.savePlayer((MinecraftServer)Sponge.getServer(), storage.get());
				}
			} else {
				computerStorage.addToComputer(this.getElement().getPokemon().writeToNBT(new NBTTagCompound()));
				PixelmonStorage.computerManager.savePlayer(computerStorage);
			}
		}

		return true;
	}

	@Override
	public boolean doTakeAway(Player player) {
		if(GTS.getInstance().getConfig().get(ConfigKeys.BLACKLISTED_POKEMON).stream().anyMatch(name -> name.equalsIgnoreCase(this.getElement().getPokemon().getName()))){
			return false;
		}

		PlayerStorage ps = PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP)player).orElse(null);
		if(ps == null)
			return false;

		ps.recallAllPokemon();
		ps.removeFromPartyPlayer(ps.getPosition(this.getElement().getPokemon().getPokemonId()));
		ps.sendUpdatedList();

		return true;
	}

	private static ItemStack getPicture(EntityPixelmon pokemon) {
		net.minecraft.item.ItemStack item = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
		NBTTagCompound nbt = new NBTTagCompound();
		String idValue = String.format("%03d", pokemon.baseStats.nationalPokedexNumber);
		if (pokemon.isEgg) {
			switch (pokemon.getSpecies()) {
				case Manaphy:
				case Togepi:
					nbt.setString(NbtKeys.SPRITE_NAME,
					              String.format("pixelmon:sprites/eggs/%s1", pokemon.getSpecies().name.toLowerCase()));
					break;
				default:
					nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/egg1");
					break;
			}
		} else if (pokemon.getIsShiny()) {
			nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/shinypokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon.getSpecies().name, pokemon.getForm()));
		} else {
			nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/pokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon.getSpecies().name, pokemon.getForm()));
		}

		item.setTagCompound(nbt);
		return ItemStackUtil.fromNative(item);
	}

	@CommandAliases({"pokemon", "poke"})
	public class PokemonSub extends SpongeSubCommand {

		private final Text argPos = Text.of("pos");
		private final Text argPrice = Text.of("price");

		@Override
		public CommandElement[] getArgs() {
			return new CommandElement[]{
					GenericArguments.integer(argPos),
					GenericArguments.integer(argPrice)
			};
		}

		@Override
		public Text getDescription() {
			return Text.of("Handles pokemon entries for the GTS");
		}

		@Override
		public Text getUsage() {
			return Text.of("/gts sell pokemon <party slot> <price>");
		}

		@Override
		public SpongeCommand[] getSubCommands() {
			return new SpongeCommand[0];
		}

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if(src instanceof Player) {
				Player player = (Player)src;
				int pos = args.<Integer>getOne(argPos).get() - 1;
				int price = args.<Integer>getOne(argPrice).get();
				if(price <= 0) {
					throw new CommandException(Text.of("Price must be a positive integer!"));
				}

				Optional<PlayerStorage> optStorage = PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP)player);
				if(optStorage.isPresent()) {
					PlayerStorage storage = optStorage.get();
					NBTTagCompound nbt = storage.getNBT(storage.getIDFromPosition(pos));
					if(nbt != null) {
						EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(
								nbt,
								(World) Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorldName()).get()
						);
						if (storage.countTeam() == 1 && !pokemon.isEgg)
							throw new CommandException(Text.of("You can't sell your last non-egg party member!"));

						Listing.builder()
								.player(player)
								.entry(new PokemonEntry(pokemon, new MoneyPrice(price)))
								.doesExpire()
								.expiration(GTS.getInstance().getConfig().get(ConfigKeys.LISTING_TIME))
								.build();

						return CommandResult.success();
					}

					throw new CommandException(Text.of("Unable to find a pokemon in the specified slot..."));
				}

				MessageUtils.genAndSendErrorMessage(
						"Pixelmon Storage Access Error",
						"Unable to locate storage for " + player.getName(),
						"Their UUID: " + player.getUniqueId()
				);
				throw new CommandException(Text.of("Unable to find your party data, this error has been reported"));
			}

			throw new CommandException(Text.of("Only players may use this command..."));
		}
	}
}
