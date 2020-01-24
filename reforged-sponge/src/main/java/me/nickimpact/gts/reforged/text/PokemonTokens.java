package me.nickimpact.gts.reforged.text;

import com.google.common.reflect.TypeToken;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import io.github.nucleuspowered.nucleus.api.placeholder.PlaceholderVariables;
import me.nickimpact.gts.api.plugin.PluginInstance;
import me.nickimpact.gts.reforged.entries.EnumPokemonFields;
import me.nickimpact.gts.sponge.SpongePlugin;
import me.nickimpact.gts.sponge.text.SpongeTokenService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class PokemonTokens {

	public PokemonTokens() {
		SpongeTokenService service = ((SpongePlugin) PluginInstance.getInstance()).getTokenService();
		PokemonKey key = new PokemonKey();
		service.register("pokemon", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.NAME)));
		service.register("nickname", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.NICKNAME)));
		service.register("ability", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.ABILITY)));
		service.register("gender", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.GENDER)));
		service.register("nature", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.NATURE)));
		service.register("growth", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.GROWTH)));
		service.register("level", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.LEVEL)));
		service.register("evs_percent", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.EV_PERCENT)));
		service.register("evs_total", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.EV_TOTAL)));
		service.register("evhp", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.EV_HP)));
		service.register("evatk", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.EV_ATK)));
		service.register("evdef", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.EV_DEF)));
		service.register("evspatk", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.EV_SPATK)));
		service.register("evspdef", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.EV_SPDEF)));
		service.register("evspeed", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.EV_SPEED)));
		service.register("ivs_percent", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.IV_PERCENT)));
		service.register("ivs_total", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.IV_TOTAL)));
		service.register("ivhp", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.IV_HP)));
		service.register("ivatk", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.IV_ATK)));
		service.register("ivdef", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.IV_DEF)));
		service.register("ivspatk", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.IV_SPATK)));
		service.register("ivspdef", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.IV_SPDEF)));
		service.register("ivspeed", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.IV_SPEED)));
		service.register("form", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.FORM)));
		service.register("shiny", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.SHINY)));
		service.register("texture", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.TEXTURE)));
		service.register("special_texture", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.SPECIAL_TEXTURE)));
		service.register("clones", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.CLONES)));
		service.register("clones_remaining", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.CLONES_REMAINING)));
		service.register("enchanted", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.ENCHANTED)));
		service.register("hidden_power", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.HIDDEN_POWER)));
		service.register("moves_1", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.MOVES_1)));
		service.register("moves_2", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.MOVES_2)));
		service.register("moves_3", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.MOVES_3)));
		service.register("moves_4", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.MOVES_4)));
		service.register("ivs_stat", placeholder -> service.getOrDefault(placeholder, key, pokemon -> Text.of("IV")));
		service.register("evs_stat", placeholder -> service.getOrDefault(placeholder, key, pokemon -> Text.of("EV")));
		service.register("pokerus", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.POKERUS)));
		service.register("pokerus_state", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.POKERUS_STATE)));
		service.register("unbreedable", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.UNBREEDABLE)));
		service.register("pokeball_name", placeholder -> service.getOrDefault(placeholder, key, pokemon -> getPokemonInfo(pokemon, EnumPokemonFields.POKE_BALL_NAME)));
	}

	public static class PokemonKey implements PlaceholderVariables.Key<Pokemon> {

		@Override
		public String key() {
			return "GTS Pokemon";
		}

		@Override
		public TypeToken<Pokemon> getValueClass() {
			return new TypeToken<Pokemon>(){};
		}

	}

	private Text getPokemonInfo(Pokemon pokemon, EnumPokemonFields field) {
		if (pokemon != null) {
			Object out = field.function.apply(pokemon);
			if(out instanceof String) {
				return Text.of(TextSerializers.FORMATTING_CODE.deserialize((String) field.function.apply(pokemon)));
			} else {
				return Text.of(field.function.apply(pokemon));
			}
		}
		return Text.EMPTY;
	}
}
