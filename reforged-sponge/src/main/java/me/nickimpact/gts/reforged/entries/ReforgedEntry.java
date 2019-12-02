package me.nickimpact.gts.reforged.entries;

import co.aikar.commands.CommandIssuer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.json.JsonTyping;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.EnumSpecialTexture;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.EnumBidoof;
import com.pixelmonmod.pixelmon.enums.forms.EnumGreninja;
import com.pixelmonmod.pixelmon.enums.forms.EnumNoForm;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import me.nickimpact.gts.api.enums.CommandResults;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.prices.Minable;
import me.nickimpact.gts.api.plugin.PluginInstance;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.reforged.ReforgedBridge;
import me.nickimpact.gts.reforged.config.PokemonConfigKeys;
import me.nickimpact.gts.reforged.config.PokemonMsgConfigKeys;
import me.nickimpact.gts.reforged.utils.Flags;
import me.nickimpact.gts.reforged.utils.GsonUtils;
import me.nickimpact.gts.sponge.*;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@JsonTyping("pokemon")
public class ReforgedEntry extends SpongeEntry<String, Pokemon> implements Minable<MoneyPrice> {

	private transient Pokemon pokemon;
	private transient boolean messaged;

	public ReforgedEntry() {}

	@Override
	public Entry setEntry(String backing) {
		this.element = backing;
		return this;
	}

	public ReforgedEntry(Pokemon element) {
		super(GsonUtils.serialize(element.writeToNBT(new NBTTagCompound())));
		this.pokemon = element;
	}

	@Override
	public Pokemon getEntry() {
		return pokemon != null ? pokemon : (pokemon = Pixelmon.pokemonFactory.create(GsonUtils.deserialize(this.element)));
	}

	@Override
	public String getSpecsTemplate() {
		if(this.getEntry().isEgg()) {
			return ReforgedBridge.getInstance().getMsgConfig().get(PokemonMsgConfigKeys.POKEMON_ENTRY_SPEC_TEMPLATE_EGG);
		}

		return ReforgedBridge.getInstance().getMsgConfig().get(PokemonMsgConfigKeys.POKEMON_ENTRY_SPEC_TEMPLATE);
	}

	@Override
	public String getName() {
		return this.getEntry().getSpecies().getLocalizedName();
	}

	@Override
	public List<String> getDetails() {
		Pokemon pokemon = this.getEntry();
		List<String> output = Lists.newArrayList();

		if(pokemon.getNickname() != null && !pokemon.getNickname().isEmpty()) {
			output.add("- Nickname: " + pokemon.getNickname());
		}

		output.add("- Level: " + pokemon.getLevel());

		if(pokemon.getFormEnum() != EnumNoForm.NoForm) {
			output.add("- Form: " + pokemon.getFormEnum().getLocalizedName());
		}

		output.add("- Traits:");
		if(pokemon.isShiny()) output.add("|  Shiny");
		if(pokemon.isEgg()) {
			output.add("|  Egg");
			output.add("|  - Steps Walked: " + this.formattedStepsRemainingOnEgg(pokemon));
		}
		if(pokemon.getPokerus() != null) output.add("|  Infected w/ Pok\u00e9rus");
		if(pokemon.getSpecialTexture() != null && pokemon.getSpecialTexture() != EnumSpecialTexture.None) output.add("|  Special Texture: " + pokemon.getSpecialTexture().name());
		if(pokemon.getCustomTexture() != null && !pokemon.getCustomTexture().isEmpty()) {
			StringBuilder sb = new StringBuilder();
			String[] split = pokemon.getCustomTexture().split("\\s+");

			boolean first = true;
			for(String word : split) {
				if(!first) {
					sb.append(" ");
				}
				sb.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
				first = false;
			}
			output.add("|  Custom Texture: " + sb.toString());
		}

		output.add("");

		output.add("- Ability: " + this.formattedAbility(pokemon));
		output.add("- Nature: " + pokemon.getNature().getLocalizedName());
		output.add("- Gender: " + pokemon.getGender().getLocalizedName());
		output.add("- Growth: " + pokemon.getGrowth().getLocalizedName());

		EVStore evs = pokemon.getEVs();
		output.add("- EVs: " + String.format(
				"%d/%d/%d/%d/%d/%d (%.2f%%)",
				evs.hp,
				evs.attack,
				evs.defence,
				evs.specialAttack,
				evs.specialDefence,
				evs.speed,
				this.calcEVPercent(evs)
		));

		IVStore ivs = pokemon.getIVs();
		output.add("- IVs: " + String.format(
				"%d/%d/%d/%d/%d/%d (%.2f%%)",
				ivs.hp,
				ivs.attack,
				ivs.defence,
				ivs.specialAttack,
				ivs.specialDefence,
				ivs.speed,
				this.calcIVPercent(ivs)
		));

		output.add("Moveset: " + this.moveset(pokemon));

		return output;
	}

	@Override
	public ItemStack baseItemStack(Player player, Listing listing) {
		ItemStack icon = this.getPicture(this.getEntry());
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);
		variables.put("pokemon", this.getEntry());

		icon.offer(Keys.DISPLAY_NAME, ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils().fetchAndParseMsg(player, ReforgedBridge.getInstance().getMsgConfig(), this.getEntry().isEgg() ? PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_TITLE_EGG : PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_TITLE, null, variables));

		List<String> template = Lists.newArrayList();
		template.addAll(ReforgedBridge.getInstance().getMsgConfig().get(PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_LORE));
		this.addLore(icon, template, player, variables);

		return icon;
	}

	private void addLore(ItemStack icon, List<String> template, Player player, Map<String, Object> variables) {
		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		for (EnumHidableDetail detail : EnumHidableDetail.values()) {
			if (detail.getCondition().test(this.getEntry())) {
				KeyDetailHolder holder = detail.getField().apply(this.getEntry());
				template.addAll(ReforgedBridge.getInstance().getMsgConfig().get(holder.getKey()));
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
		return true;
	}

	@Override
	public boolean giveEntry(User user) {
		PlayerPartyStorage storage = Pixelmon.storageManager.getParty(user.getUniqueId());
		if(!storage.add(this.getEntry())) {
			if(!messaged) {
				TextParsingUtils parser = ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils();
				user.getPlayer().ifPresent(player -> player.sendMessage(parser.fetchAndParseMsg(player, ReforgedBridge.getInstance().getMsgConfig(), PokemonMsgConfigKeys.STORAGE_FULL, null, null)));
				messaged = true;
			}

			return false;
		}
		return true;
	}

	@Override
	public boolean doTakeAway(Player player) {
		Config msgs = PluginInstance.getInstance().getMsgConfig();
		TextParsingUtils parser = ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils();

		PlayerPartyStorage storage = Pixelmon.storageManager.getParty(player.getUniqueId());
		if(BattleRegistry.getBattle(storage.getPlayer()) != null) {
			player.sendMessage(parser.fetchAndParseMsg(player, ReforgedBridge.getInstance().getMsgConfig(), PokemonMsgConfigKeys.ERROR_IN_BATTLE, null, null));
			return false;
		}

		if(Flags.UNTRADABLE.matches(this.getEntry())) {
			player.sendMessage(parser.fetchAndParseMsg(player, ReforgedBridge.getInstance().getMsgConfig(), PokemonMsgConfigKeys.ERROR_UNTRADABLE, null, null));
			return false;
		}

		List<EnumSpecies> blacklisted = ReforgedBridge.getInstance().getConfiguration()
				.get(PokemonConfigKeys.BLACKLISTED_POKEMON)
				.stream()
				.map(EnumSpecies::getFromNameAnyCase)
				.collect(Collectors.toList());
		if(blacklisted.contains(this.getEntry().getSpecies())) {
			player.sendMessage(parser.fetchAndParseMsg(player, msgs, MsgConfigKeys.ERROR_BLACKLISTED, null, null));
			return false;
		}

		storage.retrieveAll();
		storage.set(storage.getPosition(this.getEntry()), null);

		return true;
	}

	private ItemStack getPicture(Pokemon pokemon) {
		Calendar calendar = Calendar.getInstance();
		boolean aprilFools = false;
		if(calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) == 1) {
			aprilFools = true;
		}

		net.minecraft.item.ItemStack item = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
		NBTTagCompound nbt = new NBTTagCompound();
		String idValue = String.format("%03d", aprilFools ? 399 : pokemon.getBaseStats().nationalPokedexNumber);
		if (pokemon.isEgg()) {
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
		} else if (pokemon.isShiny()) {
			nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/shinypokemon/" + idValue + getSpriteExtraProperly(aprilFools ? EnumSpecies.Bidoof : pokemon.getSpecies(), aprilFools ? EnumBidoof.SIRDOOFUSIII : pokemon.getFormEnum(), pokemon.getGender(), pokemon.getSpecialTexture()));
		} else {
			nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/pokemon/" + idValue + getSpriteExtraProperly(aprilFools ? EnumSpecies.Bidoof : pokemon.getSpecies(), aprilFools ? EnumBidoof.SIRDOOFUSIII : pokemon.getFormEnum(), pokemon.getGender(), pokemon.getSpecialTexture()));
		}

		item.setTagCompound(nbt);
		return (ItemStack) (Object) item;
	}

	@Override
	public MoneyPrice calcMinPrice() {
		MoneyPrice price = new MoneyPrice(ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_BASE));
		Pokemon pokemon = this.getEntry();

		boolean isLegend = EnumSpecies.legendaries.contains(pokemon.getSpecies().getPokemonName());
		if (isLegend && pokemon.isShiny()) {
			price = new MoneyPrice(ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_LEGEND) + ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_SHINY) + price.getPrice());
		} else if (isLegend) {
			price = new MoneyPrice(ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_LEGEND) + price.getPrice());
		} else if (pokemon.isShiny()) {
			price = new MoneyPrice(ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_SHINY) + price.getPrice());

		}

		for (int iv : pokemon.getStats().ivs.getArray()) {
			if (iv >= ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_IVS_MINVAL)) {
				price = new MoneyPrice(ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_IVS_PRICE) + price.getPrice());
			}
		}

		if (pokemon.getAbilitySlot() == 2) {
			price = new MoneyPrice(ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.MIN_PRICING_POKEMON_HA) + price.getPrice());
		}

		double total = 0.0;
		for(Function<Pokemon, Double> function : ReforgedBridge.getInstance().getAPIService().getMinPriceOptionsForEntryType(ReforgedEntry.class)) {
			total += function.apply(pokemon);
		}
		price = new MoneyPrice(price.getPrice() + total);

		return price;
	}

	public enum LakeTrio {
		Mesprit(EnumSpecies.Mesprit),
		Azelf(EnumSpecies.Azelf),
		Uxie(EnumSpecies.Uxie);

		private EnumSpecies species;

		LakeTrio(EnumSpecies species) {
			this.species = species;
		}

		public static boolean isMember(EnumSpecies species) {
			for(LakeTrio guardian : values()) {
				if(guardian.species == species) {
					return true;
				}
			}

			return false;
		}
	}

	private double calcEVPercent(EVStore evs) {
		return (evs.hp + evs.attack + evs.defence + evs.specialAttack + evs.specialDefence + evs.speed) / 510.0 * 100;
	}

	private double calcIVPercent(IVStore ivs) {
		return (ivs.hp + ivs.attack + ivs.defence + ivs.specialAttack + ivs.specialDefence + ivs.speed) / 186.0 * 100;
	}

	private String formattedAbility(Pokemon pokemon) {
		boolean first = false;
		StringBuilder ability = new StringBuilder();
		String initial = pokemon.getAbilityName();
		for(int i = 0; i < initial.length(); i++) {
			char c = initial.charAt(i);
			if(c >= 'A' && c <= 'Z') {
				if (first) {
					ability.append(" ").append(c);
				} else {
					ability.append(c);
					first = true;
				}
			} else {
				ability.append(c);
			}
		}

		return ability.toString();
	}

	private String formattedStepsRemainingOnEgg(Pokemon pokemon) {
		int total = (pokemon.getBaseStats().eggCycles + 1) * PixelmonConfig.stepsPerEggCycle;
		int walked = pokemon.getEggSteps() + ((pokemon.getBaseStats().eggCycles - pokemon.getEggCycles()) * PixelmonConfig.stepsPerEggCycle);
		return String.format("%d/%d", walked, total);
	}

	private String moveset(Pokemon pokemon) {
		Moveset moves = pokemon.getMoveset();
		StringBuilder out = new StringBuilder();
		for(Attack attack : moves.attacks) {
			if(attack == null) continue;
			out.append(attack.getActualMove().getLocalizedName()).append(" - ");
		}

		return out.substring(0, out.length() - 3);
	}

	public static CommandResults execute(CommandIssuer src, List<String> args, boolean permanent) {
		CommandSource source = src.getIssuer();
		Config config = PluginInstance.getInstance().getMsgConfig();
		TextParsingUtils parser = ((SpongePlugin) PluginInstance.getInstance()).getTextParsingUtils();

		if(args.size() < 2) {
			source.sendMessage(parser.fetchAndParseMsg(source, config, MsgConfigKeys.INVALID_ARGS, null, null));
			return CommandResults.FAILED;
		}

		if(source instanceof Player) {
			int slot;
			double price;

			try {
				slot = Integer.parseInt(args.get(0));
				price = Double.parseDouble(args.get(1));
			} catch (Exception e) {
				source.sendMessage(parser.fetchAndParseMsg(source, config, MsgConfigKeys.INVALID_ARGS, null, null));
				return CommandResults.FAILED;
			}

			if(slot < 1 || slot > 6) {
				source.sendMessage(parser.fetchAndParseMsg(source, config, MsgConfigKeys.INVALID_ARGS, null, null));
				return CommandResults.FAILED;
			}

			if(price <= 0) {
				source.sendMessage(parser.fetchAndParseMsg(source, config, MsgConfigKeys.PRICE_NOT_POSITIVE, null, null));
				return CommandResults.FAILED;
			}

			if(price > PluginInstance.getInstance().getConfiguration().get(ConfigKeys.MAX_MONEY_PRICE)) {
				source.sendMessage(parser.fetchAndParseMsg(source, config, MsgConfigKeys.PRICE_MAX_INVALID, null, null));
				return CommandResults.FAILED;
			}

			if(!source.hasPermission("gts.command.sell.pokemon.base")) {
				source.sendMessage(parser.fetchAndParseMsg(source, config, MsgConfigKeys.NO_PERMISSION, null, null));
				return CommandResults.FAILED;
			}

			PlayerPartyStorage party = Pixelmon.storageManager.getParty(((Player) source).getUniqueId());
			Pokemon pokemon = party.get(slot - 1);
			if(pokemon == null) {
				source.sendMessage(parser.fetchAndParseMsg(source, ReforgedBridge.getInstance().getMsgConfig(), PokemonMsgConfigKeys.ERROR_EMPTY_SLOT, null, null));
				return CommandResults.FAILED;
			}

			if(!pokemon.isEgg() && party.getTeam().size() <= 1) {
				source.sendMessage(parser.fetchAndParseMsg(source, ReforgedBridge.getInstance().getMsgConfig(), PokemonMsgConfigKeys.ERROR_LAST_MEMBER, null, null));
				return CommandResults.FAILED;
			}

			if(ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.BLACKLISTED_POKEMON).stream().anyMatch(species -> species.toLowerCase().equals(pokemon.getSpecies().getPokemonName().toLowerCase()))) {
				if(!source.hasPermission("gts.command.sell.pokemon.bypass")) {
					source.sendMessage(parser.fetchAndParseMsg(source, config, MsgConfigKeys.BLACKLISTED, null, null));
					return CommandResults.FAILED;
				}
			}

			SpongeListing listing = SpongeListing.builder()
					.entry(new ReforgedEntry(pokemon))
					.id(UUID.randomUUID())
					.owner(((Player) source).getUniqueId())
					.price(price)
					.expiration(permanent ? LocalDateTime.MAX : LocalDateTime.now().plusSeconds(PluginInstance.getInstance().getConfiguration().get(ConfigKeys.LISTING_TIME)))
					.build();
			listing.publish(PluginInstance.getInstance(), ((Player) source).getUniqueId());

			return CommandResults.SUCCESSFUL;
		}

		source.sendMessage(parser.fetchAndParseMsg(source, config, MsgConfigKeys.NOT_PLAYER, null, null));
		return CommandResults.FAILED;
	}

	private static String getSpriteExtraProperly(EnumSpecies species, IEnumForm form, Gender gender, EnumSpecialTexture specialTexture) {
		if (species == EnumSpecies.Greninja && (form == EnumGreninja.BASE || form == EnumGreninja.BATTLE_BOND) && specialTexture.id > 0 && species.hasSpecialTexture()) {
			return "-special";
		}

		if(form != EnumNoForm.NoForm) {
			return species.getFormEnum(form.getForm()).getSpriteSuffix();
		}

		if(EnumSpecies.mfSprite.contains(species)) {
			return "-" + gender.name().toLowerCase();
		}

		if(specialTexture.id > 0 && species.hasSpecialTexture()) {
			return "-special";
		}

		return "";
	}
}
