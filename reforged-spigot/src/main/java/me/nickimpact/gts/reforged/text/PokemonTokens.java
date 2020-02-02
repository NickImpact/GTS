package me.nickimpact.gts.reforged.text;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.reforged.entry.EnumPokemonFields;
import me.nickimpact.gts.reforged.entry.ReforgedEntry;
import me.nickimpact.gts.spigot.tokens.TokenService;
import org.bukkit.ChatColor;

import java.util.Map;
import java.util.Optional;

public class PokemonTokens {

	public PokemonTokens(TokenService service) {
		service.register("pokemon", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.NAME)));
		service.register("nickname", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.NICKNAME)));
		service.register("ability", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.ABILITY)));
		service.register("gender", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.GENDER)));
		service.register("nature", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.NATURE)));
		service.register("growth", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.GROWTH)));
		service.register("level", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.LEVEL)));
		service.register("evs_percent", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.EV_PERCENT)));
		service.register("evs_total", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.EV_TOTAL)));
		service.register("evhp", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.EV_HP)));
		service.register("evatk", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.EV_ATK)));
		service.register("evdef", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.EV_DEF)));
		service.register("evspatk", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.EV_SPATK)));
		service.register("evspdef", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.EV_SPDEF)));
		service.register("evspeed", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.EV_SPEED)));
		service.register("ivs_percent", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.IV_PERCENT)));
		service.register("ivs_total", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.IV_TOTAL)));
		service.register("ivhp", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.IV_HP)));
		service.register("ivatk", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.IV_ATK)));
		service.register("ivdef", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.IV_DEF)));
		service.register("ivspatk", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.IV_SPATK)));
		service.register("ivspdef", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.IV_SPDEF)));
		service.register("ivspeed", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.IV_SPEED)));
		service.register("form", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.FORM_NAME)));
		service.register("shiny", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.SHINY)));
		service.register("shiny_state", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.SHINY_STATE)));
		service.register("texture", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.TEXTURE)));
		service.register("special_texture", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.SPECIAL_TEXTURE)));
		service.register("clones", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.CLONES)));
		service.register("clones_remaining", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.CLONES_REMAINING)));
		service.register("enchanted", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.ENCHANTED)));
		service.register("hidden_power", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.HIDDEN_POWER)));
		service.register("moves_1", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.MOVES_1)));
		service.register("moves_2", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.MOVES_2)));
		service.register("moves_3", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.MOVES_3)));
		service.register("moves_4", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.MOVES_4)));
		service.register("ivs_stat", (p, v, m) -> {
			if (getPokemonFromVariableIfExists(m) != null) {
				return Optional.of("IV");
			}

			return Optional.empty();
		});
		service.register("evs_stat", (p, v, m) -> {
			if (getPokemonFromVariableIfExists(m) != null) {
				return Optional.of("EV");
			}

			return Optional.empty();
		});
		service.register("pokerus", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.POKERUS)));
		service.register("pokerus_state", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.POKERUS_STATE)));
		service.register("unbreedable", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.UNBREEDABLE)));
		service.register("pokeball_name", (p, v, m) -> Optional.of(getPokemonInfo(getPokemonFromVariableIfExists(m), EnumPokemonFields.POKE_BALL_NAME)));
	}

	private static Pokemon getPokemonFromVariableIfExists(Map<String, Object> m) {
		Optional<Object> optPoke = m.values().stream().filter(val -> val instanceof Listing || val instanceof Pokemon).findAny();
		if (optPoke.isPresent()) {
			if(optPoke.get() instanceof Listing) {
				if(((Listing) optPoke.get()).getEntry() instanceof ReforgedEntry) {
					return (((ReforgedEntry) ((Listing) optPoke.get()).getEntry())).getEntry();
				}
			} else {
				return (Pokemon) optPoke.get();
			}
		}

		return null;
	}

	private static String getPokemonInfo(Pokemon pokemon, EnumPokemonFields field) {
		if (pokemon != null) {
			return ChatColor.translateAlternateColorCodes('&', field.function.apply(pokemon));
		}
		return "";
	}
}
