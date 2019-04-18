package me.nickimpact.gts.pixelmon.entries;

import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.nickimpact.gts.pixelmon.config.PokemonMsgConfigKeys;

import java.util.List;
import java.util.function.Predicate;

@Getter
@AllArgsConstructor
public enum EnumHidableDetail {

	TEXTURE(pokemon -> pokemon.getCustomTexture() != null && !pokemon.getCustomTexture().isEmpty(), PokemonMsgConfigKeys.PE_BASE_TEXTURE),
	UNBREEDABLE(pokemon -> new PokemonSpec("unbreedable").matches(pokemon), PokemonMsgConfigKeys.PE_BASE_UNBREEDABLE),
	POKERUS(pokemon -> pokemon.getPokerus() != null, PokemonMsgConfigKeys.PE_BASE_POKERUS),
	CLONES(pokemon -> pokemon.getSpecies().equals(EnumSpecies.Mew), PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_MEW_CLONES),
	ENCHANTED(pokemon -> ReforgedEntry.LakeTrio.isMember(pokemon.getSpecies()), PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_LAKE_TRIO),
	;

	private Predicate<Pokemon> condition;
	private ConfigKey<List<String>> field;
}
