package me.nickimpact.gts.reforged.entry;

import com.nickimpact.impactor.api.configuration.Config;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.HiddenPower;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.reforged.ReforgedBridge;
import me.nickimpact.gts.reforged.config.ReforgedKeys;
import me.nickimpact.gts.reforged.config.ReforgedMsgConfigKeys;
import me.nickimpact.gts.spigot.SpigotGTSPlugin;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.ChatColor;

import java.text.DecimalFormat;
import java.util.function.Function;

public enum EnumPokemonFields {

	NAME(pokemon -> {
		if(pokemon.getSpecies().getPokemonName().equals("Yamper")) {
			return "Corgi";
		}
		return pokemon.getSpecies().getLocalizedName();
	}),
	ABILITY(pokemon -> {
		boolean ha = pokemon.getAbilitySlot() == 2;
		if(ha) {
			return pokemon.getAbility().getLocalizedName() + " &7(&6HA&7)";
		} else {
			return pokemon.getAbility().getLocalizedName();
		}
	}),
	NATURE(pokemon -> pokemon.getNature().getLocalizedName()),
	NATURE_INCREASED(pokemon -> "+" + toRep(pokemon.getNature().increasedStat)),
	NATURE_DECREASED(pokemon -> "-" + toRep(pokemon.getNature().decreasedStat)),
	GENDER(pokemon -> {
		switch(pokemon.getGender()) {
			case Male:
				return "&b" + Gender.Male.getLocalizedName();
			case Female:
				return "&d" + Gender.Female.getLocalizedName();
			default:
				return "&f" + Gender.None.getLocalizedName();
		}
	}),
	SHINY(pokemon -> {
		if(!pokemon.isShiny())
			return "";

		return "&7(&6" + ((SpigotGTSPlugin) PluginInstance.getInstance()).getTokenService().process(ReforgedBridge.getInstance().getMsgConfig(), ReforgedMsgConfigKeys.SHINY_TRANSLATION, null, null, null) + "&7)";
	}),

	GROWTH(pokemon -> pokemon.getGrowth().getLocalizedName()),
	LEVEL(pokemon -> {
		if(pokemon.isEgg()) {
			return "" + 1;
		}

		return "" + pokemon.getLevel();
	}),
	FORM_NAME(pokemon -> {
		String form = pokemon.getFormEnum().getFormSuffix();
		if(form.startsWith("-")) {
			return form.substring(1);
		} else {
			return form;
		}
	}),
	CLONES(pokemon -> {
		if(pokemon.getSpecies().equals(EnumSpecies.Mew)) {
			NBTTagCompound nbt = new NBTTagCompound();
			pokemon.writeToNBT(nbt);
			return "" + nbt.getShort(NbtKeys.STATS_NUM_CLONED);
		}
		return "" + 0;
	}),
	CLONES_REMAINING(pokemon -> {
		if(pokemon.getSpecies().equals(EnumSpecies.Mew)) {
			NBTTagCompound nbt = new NBTTagCompound();
			pokemon.writeToNBT(nbt);
			return "" + (3 - nbt.getShort(NbtKeys.STATS_NUM_CLONED));
		}
		return "" + 0;
	}),
	ENCHANTED(pokemon -> {
		switch (pokemon.getSpecies()) {
			case Mesprit:
			case Azelf:
			case Uxie:
				NBTTagCompound nbt = new NBTTagCompound();
				pokemon.writeToNBT(nbt);
				return "" + nbt.getShort(NbtKeys.STATS_NUM_ENCHANTED);
			default:
				return "" + 0;
		}
	}),
	EV_PERCENT(pokemon -> new DecimalFormat("#0.##").format(totalEVs(pokemon.getStats().evs) / 510.0 * 100) + "%"),
	IV_PERCENT(pokemon -> new DecimalFormat("#0.##").format(totalIVs(pokemon.getStats().ivs) / 186.0 * 100) + "%"),
	EV_TOTAL(pokemon -> "" + (int)totalEVs(pokemon.getStats().evs)),
	IV_TOTAL(pokemon -> "" + (int)totalIVs(pokemon.getStats().ivs)),
	NICKNAME(pokemon -> ChatColor.translateAlternateColorCodes('\u00A7', pokemon.getNickname() != null  && !pokemon.getNickname().isEmpty() ? pokemon.getNickname() : "N/A")),
	EV_HP(pokemon -> "" + pokemon.getStats().evs.hp),
	EV_ATK(pokemon -> "" + pokemon.getStats().evs.attack),
	EV_DEF(pokemon -> "" + pokemon.getStats().evs.defence),
	EV_SPATK(pokemon -> "" + pokemon.getStats().evs.specialAttack),
	EV_SPDEF(pokemon -> "" + pokemon.getStats().evs.specialDefence),
	EV_SPEED(pokemon -> "" + pokemon.getStats().evs.speed),
	IV_HP(pokemon -> (pokemon.getStats().ivs.isHyperTrained(StatsType.HP) ? "&6" : "&e") + getIV(pokemon.getStats().ivs, StatsType.HP)),
	IV_ATK(pokemon -> (pokemon.getStats().ivs.isHyperTrained(StatsType.Attack) ? "&6" : "&e") + getIV(pokemon.getStats().ivs, StatsType.Attack)),
	IV_DEF(pokemon -> (pokemon.getStats().ivs.isHyperTrained(StatsType.Defence) ? "&6" : "&e") + getIV(pokemon.getStats().ivs, StatsType.Defence)),
	IV_SPATK(pokemon -> (pokemon.getStats().ivs.isHyperTrained(StatsType.SpecialAttack) ? "&6" : "&e") + getIV(pokemon.getStats().ivs, StatsType.SpecialAttack)),
	IV_SPDEF(pokemon -> (pokemon.getStats().ivs.isHyperTrained(StatsType.SpecialDefence) ? "&6" : "&e") + getIV(pokemon.getStats().ivs, StatsType.SpecialDefence)),
	IV_SPEED(pokemon -> (pokemon.getStats().ivs.isHyperTrained(StatsType.Speed) ? "&6" : "&e") + getIV(pokemon.getStats().ivs, StatsType.Speed)),
	TEXTURE(pokemon -> {
		NBTTagCompound nbt = new NBTTagCompound();
		pokemon.writeToNBT(nbt);

		String texture = nbt.getString(NbtKeys.CUSTOM_TEXTURE);
		if(!texture.isEmpty()) {
			Config config = ReforgedBridge.getInstance().getConfiguration();
			if(config.get(ReforgedKeys.TEXTUREFLAG_CAPITALIZE)) {
				StringBuilder sb = new StringBuilder();
				String[] split = texture.split("\\s+");

				boolean first = true;
				for(String word : split) {
					if(!first) {
						sb.append(" ");
					}
					sb.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
					first = false;
				}

				texture = sb.toString();
			}

			if(config.get(ReforgedKeys.TEXTUREFLAG_TRIM_TRAILING_NUMS)) {
				texture = texture.replaceAll("\\d*$", "");
			}

			return texture;
		}

		return "";
	}),
	SPECIAL_TEXTURE(pokemon -> {
		return pokemon.getSpecialTexture().name();
	}),
	HIDDEN_POWER(pokemon -> HiddenPower.getHiddenPowerType(pokemon.getStats().ivs).getLocalizedName()),
	MOVES_1(pokemon -> pokemon.getMoveset().attacks[0].getActualMove().getLocalizedName()),
	MOVES_2(pokemon -> pokemon.getMoveset().attacks[1].getActualMove().getLocalizedName()),
	MOVES_3(pokemon -> pokemon.getMoveset().attacks[2].getActualMove().getLocalizedName()),
	MOVES_4(pokemon -> pokemon.getMoveset().attacks[3].getActualMove().getLocalizedName()),
	SHINY_STATE(pokemon -> ((SpigotGTSPlugin) PluginInstance.getInstance()).getTokenService().process(pokemon.isShiny() ? MsgConfigKeys.TRANSLATIONS_YES : MsgConfigKeys.TRANSLATIONS_NO, null, null, null)),
	POKERUS_STATE(pokemon -> ((SpigotGTSPlugin) PluginInstance.getInstance()).getTokenService().process(pokemon.isShiny() ? MsgConfigKeys.TRANSLATIONS_YES : MsgConfigKeys.TRANSLATIONS_NO, null, null, null)),
	POKERUS(pokemon ->  pokemon.getPokerus() != null ? ((SpigotGTSPlugin) PluginInstance.getInstance()).getTokenService().process(ReforgedBridge.getInstance().getMsgConfig(), ReforgedMsgConfigKeys.POKERUS_TRANSLATION, null, null, null) : null),
	UNBREEDABLE(pokemon -> {
		PokemonSpec unbreedable = new PokemonSpec("unbreedable");
		if(unbreedable.matches(pokemon)){
			return ((SpigotGTSPlugin) PluginInstance.getInstance()).getTokenService().process(ReforgedBridge.getInstance().getMsgConfig(), ReforgedMsgConfigKeys.BREEDABLE_TRANSLATION, null, null, null);
		}else{
			return ((SpigotGTSPlugin) PluginInstance.getInstance()).getTokenService().process(ReforgedBridge.getInstance().getMsgConfig(), ReforgedMsgConfigKeys.UNBREEDABLE_TRANSLATION, null, null, null);
		}
	}),
	POKE_BALL_NAME(pokemon ->{
		return pokemon.getCaughtBall().name();
	});


	public final Function<Pokemon, String> function;

	private EnumPokemonFields(Function<Pokemon, String> function) {
		this.function = function;
	}

	public static int getIV(IVStore ivs, StatsType type) {
		return ivs.isHyperTrained(type) ? 31 : ivs.get(type);
	}

	private static double totalEVs(EVStore evs) {
		return evs.hp + evs.attack + evs.defence + evs.specialAttack + evs.specialDefence + evs.speed;
	}

	private static double totalIVs(IVStore ivs) {
		return getIV(ivs, StatsType.HP) + getIV(ivs, StatsType.Attack) + getIV(ivs, StatsType.Defence) + getIV(ivs, StatsType.SpecialAttack) + getIV(ivs, StatsType.SpecialDefence) + getIV(ivs, StatsType.Speed);
	}

	private static String toRep(StatsType stat) {
		switch(stat) {
			case HP:
				return "HP";
			case Attack:
				return "Atk";
			case Defence:
				return "Def";
			case SpecialAttack:
				return "SpAtk";
			case SpecialDefence:
				return "SpDef";
			case Speed:
				return "Speed";
			default:
				return "???";
		}
	}

}
