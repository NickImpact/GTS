package me.nickimpact.gts.generations.entries;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.json.JsonTyping;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVsStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.enums.forms.EnumForms;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import me.nickimpact.gts.api.enums.CommandResults;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.prices.Minable;
import me.nickimpact.gts.api.plugin.PluginInstance;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.generations.GenerationsBridge;
import me.nickimpact.gts.generations.config.PokemonConfigKeys;
import me.nickimpact.gts.generations.config.PokemonMsgConfigKeys;
import me.nickimpact.gts.generations.utils.GsonUtils;
import me.nickimpact.gts.sponge.MoneyPrice;
import me.nickimpact.gts.sponge.SpongeEntry;
import me.nickimpact.gts.sponge.SpongeListing;
import me.nickimpact.gts.sponge.SpongePlugin;
import me.nickimpact.gts.sponge.TextParsingUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@JsonTyping("Pokemon")
public class PokemonEntry extends SpongeEntry<String, EntityPixelmon> implements Minable<MoneyPrice> {

	private transient EntityPixelmon pokemon;

	public PokemonEntry() {
		super();
	}

	@Override
	public Entry setEntry(String backing) {
		this.element = backing;
		return this;
	}

	@Override
	public EntityPixelmon getEntry() {
		return pokemon != null ? pokemon : (pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(GsonUtils.deserialize(this.element), (World) Sponge.getServer().getWorlds().iterator().next()));
	}

	public PokemonEntry(EntityPixelmon pokemon) {
		super(GsonUtils.serialize(pokemon.writeToNBT(new NBTTagCompound())));
		this.pokemon = pokemon;
	}

	@Override
	public String getSpecsTemplate() {
		if(this.getEntry().isEgg) {
			return GenerationsBridge.getInstance().getMsgConfig().get(PokemonMsgConfigKeys.POKEMON_ENTRY_SPEC_TEMPLATE_EGG);
		}
		return GenerationsBridge.getInstance().getMsgConfig().get(PokemonMsgConfigKeys.POKEMON_ENTRY_SPEC_TEMPLATE);
	}


	@Override
	public String getName() {
		return this.getEntry().getName();
	}

	@Override
	public List<String> getDetails() {
		EntityPixelmon pokemon = this.getEntry();
		List<String> output = Lists.newArrayList();

		if(pokemon.getNickname() != null && !pokemon.getNickname().isEmpty()) {
			output.add("- Nickname: " + pokemon.getNickname());
		}

		output.add("- Level: " + pokemon.getLvl().getLevel());

		if(pokemon.getFormEnum() != EnumForms.NoForm) {
			output.add("- Form: " + pokemon.getForm());
		}

		output.add("- Traits:");
		if(pokemon.getIsShiny()) output.add("|  Shiny");
		if(pokemon.isEgg) {
			output.add("|  Egg");
			output.add("|  - Steps Walked: " + this.formattedStepsRemainingOnEgg(pokemon));
		}
//		if(pokemon.getSpecialTexture() != null && pokemon.getSpecialTexture() != EnumSpecialTexture.None) output.add("|  Special Texture: " + pokemon.getSpecialTexture().name());
//		if(pokemon.getCustomTexture() != null && !pokemon.getCustomTexture().isEmpty()) {
//			StringBuilder sb = new StringBuilder();
//			String[] split = pokemon.getCustomTexture().split("\\s+");
//
//			boolean first = true;
//			for(String word : split) {
//				if(!first) {
//					sb.append(" ");
//				}
//				sb.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
//				first = false;
//			}
//			output.add("|  Custom Texture: " + sb.toString());
//		}

		output.add("");

		output.add("- Ability: " + pokemon.ability.getLocalizedName());
		output.add("- Nature: " + pokemon.getNature().getLocalizedName());
		output.add("- Gender: " + pokemon.getGender().name());
		output.add("- Growth: " + pokemon.getGrowth().getLocalizedName());

		EVsStore evs = pokemon.stats.EVs;
		output.add("- EVs: " + String.format(
				"%d/%d/%d/%d/%d/%d (%.2f%%)",
				evs.HP,
				evs.Attack,
				evs.Defence,
				evs.SpecialAttack,
				evs.SpecialDefence,
				evs.Speed,
				this.calcEVPercent(evs)
		));

		IVStore ivs = pokemon.stats.IVs;
		output.add("- IVs: " + String.format(
				"%d/%d/%d/%d/%d/%d (%.2f%%)",
				ivs.HP,
				ivs.Attack,
				ivs.Defence,
				ivs.SpAtt,
				ivs.SpDef,
				ivs.Speed,
				this.calcIVPercent(ivs)
		));

		output.add("Moveset: " + this.moveset(pokemon));

		return output;
	}

	@Override
	public ItemStack baseItemStack(Player player, Listing listing) {
		ItemStack icon = getPicture(this.getEntry());
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);
		variables.put("pokemon", this.getEntry());

		icon.offer(Keys.DISPLAY_NAME, ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils().fetchAndParseMsg(player, GenerationsBridge.getInstance().getMsgConfig(), this.getEntry().isEgg ? PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_TITLE_EGG : PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_TITLE, null, variables));

		List<String> template = Lists.newArrayList();
		template.addAll(GenerationsBridge.getInstance().getMsgConfig().get(PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_LORE));
		this.addLore(icon, template, player, listing, variables);

		return icon;
	}

	private void addLore(ItemStack icon, List<String> template, Player player, Listing listing, Map<String, Object> variables) {
		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		for (EnumHidableDetail detail : EnumHidableDetail.values()) {
			if (detail.getCondition().test(this.getEntry())) {
				KeyDetailHolder holder = detail.getField().apply(this.getEntry());
				template.addAll(GenerationsBridge.getInstance().getMsgConfig().get(holder.getKey()));
				if(holder.getTokens() != null) {
					tokens.putAll(holder.getTokens());
				}
			}
		}

		template.addAll(PluginInstance.getInstance().getMsgConfig().get(MsgConfigKeys.ENTRY_INFO));
		List<Text> translated = template.stream().map(str -> ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils().fetchAndParseMsg(player, str, tokens, variables)).collect(Collectors.toList());
		icon.offer(Keys.ITEM_LORE, translated);
	}

	@Override
	public boolean supportsOffline() {
		return false;
	}

	@Override
	public boolean giveEntry(User user) {
		Optional<PlayerStorage> optStorage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID(
				(MinecraftServer) Sponge.getServer(),
				user.getUniqueId()
		);

		if (!optStorage.isPresent())
			return false;

		optStorage.get().addToParty(this.getEntry());
		optStorage.get().sendUpdatedList();

		return true;
	}

	@Override
	public boolean doTakeAway(Player player) {
		Config msgs = PluginInstance.getInstance().getMsgConfig();
		TextParsingUtils parser = ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils();

		if(BattleRegistry.getBattle((EntityPlayer) player) != null) {
			player.sendMessage(parser.fetchAndParseMsg(player, GenerationsBridge.getInstance().getMsgConfig(), PokemonMsgConfigKeys.ERROR_IN_BATTLE, null, null));
			return false;
		}

//		if(UNTRADABLE.matches(this.getEntry())) {
//			player.sendMessage(Text.of(GTSInfo.ERROR, TextColors.GRAY, "This pokemon is marked as untradeable, and cannot be sold..."));
//			return false;
//		}

		List<EnumPokemon> blacklisted = GenerationsBridge.getInstance().getConfiguration()
				.get(PokemonConfigKeys.BLACKLISTED_POKEMON)
				.stream()
				.map(EnumPokemon::getFromNameAnyCase)
				.collect(Collectors.toList());
		if(blacklisted.contains(this.getEntry().getSpecies())) {
			player.sendMessage(parser.fetchAndParseMsg(player, msgs, MsgConfigKeys.ERROR_BLACKLISTED, null, null));
			return false;
		}

		PlayerStorage ps = PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP)player).orElse(null);
		if(ps == null)
			return false;

		ps.recallAllPokemon();
		ps.removeFromPartyPlayer(ps.getPosition(this.getEntry().getPokemonId()));
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
		return (ItemStack) (Object) item;
	}

	@Override
	public MoneyPrice calcMinPrice() {
		MoneyPrice price = new MoneyPrice(GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_BASE));
		EntityPixelmon pokemon = this.getEntry();

		boolean isLegend = EnumPokemon.legendaries.contains(pokemon.getName());
		if (isLegend && pokemon.getIsShiny()) {
			price = new MoneyPrice(GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_LEGEND) + GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_SHINY) + price.getPrice());
		} else if (isLegend) {
			price = new MoneyPrice(GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_LEGEND) + price.getPrice());
		} else if (pokemon.getIsShiny()) {
			price = new MoneyPrice(GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_SHINY) + price.getPrice());

		}

		for (int iv : pokemon.stats.IVs.getArray()) {
			if (iv >= GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_IVS_MINVAL)) {
				price = new MoneyPrice(GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_IVS_PRICE) + price.getPrice());
			}
		}

		if (pokemon.getAbilitySlot() == 2) {
			price = new MoneyPrice(GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_HA) + price.getPrice());
		}

		return price;
	}

	public static CommandResults execute(CommandSource src, List<String> args, boolean permanent) {
		Config config = PluginInstance.getInstance().getMsgConfig();
		TextParsingUtils parser = ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils();

		if(args.size() < 2) {
			return CommandResults.FAILED;
		}

		if(src instanceof Player) {

			int slot;
			double price;

			try {
				slot = Integer.parseInt(args.get(0));
				price = Double.parseDouble(args.get(1));
			} catch (Exception e) {
				src.sendMessage(parser.fetchAndParseMsg(src, config, MsgConfigKeys.INVALID_ARGS, null, null));
				return CommandResults.FAILED;
			}

			if (slot < 1 || slot > 6) {
				src.sendMessage(parser.fetchAndParseMsg(src, config, MsgConfigKeys.INVALID_ARGS, null, null));
				return CommandResults.FAILED;
			}

			if (price <= 0) {
				src.sendMessage(parser.fetchAndParseMsg(src, config, MsgConfigKeys.PRICE_NOT_POSITIVE, null, null));
				return CommandResults.FAILED;
			}

			if(price > PluginInstance.getInstance().getConfiguration().get(ConfigKeys.MAX_MONEY_PRICE)) {
				src.sendMessage(parser.fetchAndParseMsg(src, config, MsgConfigKeys.PRICE_MAX_INVALID, null, null));
				return CommandResults.FAILED;
			}

			if(!src.hasPermission("gts.command.sell.pokemon.base")) {
				src.sendMessage(parser.fetchAndParseMsg(src, config, MsgConfigKeys.NO_PERMISSION, null, null));
				return CommandResults.FAILED;
			}

			PlayerStorage party = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID(((Player) src).getUniqueId()).get();
			EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(party.partyPokemon[slot - 1], (World) ((Player) src).getWorld());
			if(pokemon == null) {
				src.sendMessage(parser.fetchAndParseMsg(src, GenerationsBridge.getInstance().getMsgConfig(), PokemonMsgConfigKeys.ERROR_EMPTY_SLOT, null, null));
				return CommandResults.FAILED;
			}

			if(!pokemon.isEgg && party.getTeam().size() <= 1) {
				src.sendMessage(parser.fetchAndParseMsg(src, GenerationsBridge.getInstance().getMsgConfig(), PokemonMsgConfigKeys.ERROR_LAST_MEMBER, null, null));
				return CommandResults.FAILED;
			}

			if(GenerationsBridge.getInstance().getConfig().get(PokemonConfigKeys.BLACKLISTED_POKEMON).stream().anyMatch(species -> species.toLowerCase().equals(pokemon.getLocalizedName().toLowerCase()))) {
				if(!src.hasPermission("gts.command.sell.pokemon.bypass")) {
					src.sendMessage(parser.fetchAndParseMsg(src, config, MsgConfigKeys.BLACKLISTED, null, null));
					return CommandResults.FAILED;
				}
			}

			SpongeListing listing = SpongeListing.builder()
					.entry(new PokemonEntry(pokemon))
					.id(UUID.randomUUID())
					.owner(((Player) src).getUniqueId())
					.price(price)
					.expiration(permanent ? LocalDateTime.MAX : LocalDateTime.now().plusSeconds(PluginInstance.getInstance().getConfiguration().get(ConfigKeys.LISTING_TIME)))
					.build();
			listing.publish(PluginInstance.getInstance(), ((Player) src).getUniqueId());

			return CommandResults.SUCCESSFUL;
		}

		return CommandResults.SUCCESSFUL;
	}

	public enum LakeTrio {
		Mesprit(EnumPokemon.Mesprit),
		Azelf(EnumPokemon.Azelf),
		Uxie(EnumPokemon.Uxie);

		private EnumPokemon species;

		LakeTrio(EnumPokemon species) {
			this.species = species;
		}

		public static boolean isMember(EnumPokemon species) {
			for(LakeTrio guardian : values()) {
				if(guardian.species == species) {
					return true;
				}
			}

			return false;
		}
	}

	private double calcEVPercent(EVsStore evs) {
		return (evs.HP + evs.Attack + evs.Defence + evs.SpecialAttack + evs.SpecialDefence + evs.Speed) / 510.0 * 100;
	}

	private double calcIVPercent(IVStore ivs) {
		return (ivs.HP + ivs.Attack + ivs.Defence + ivs.SpAtt + ivs.SpDef + ivs.Speed) / 186.0 * 100;
	}

	private String formattedStepsRemainingOnEgg(EntityPixelmon pokemon) {
		int total = (pokemon.baseStats.eggCycles + 1) * PixelmonConfig.stepsPerEggCycle;
		int walked = pokemon.writeToNBT(new NBTTagCompound()).getInteger("steps") + ((pokemon.baseStats.eggCycles - pokemon.eggCycles) * PixelmonConfig.stepsPerEggCycle);
		return String.format("%d/%d", walked, total);
	}

	private String moveset(EntityPixelmon pokemon) {
		Moveset moves = pokemon.getMoveset();
		StringBuilder out = new StringBuilder();
		for(Attack attack : moves.attacks) {
			if(attack == null) continue;
			out.append(attack.baseAttack.getLocalizedName()).append(" - ");
		}

		return out.substring(0, out.length() - 3);
	}
}
