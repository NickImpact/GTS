package me.nickimpact.gts.reforged.entries;

import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.nickimpact.gts.reforged.config.PokemonMsgConfigKeys;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@Getter
@AllArgsConstructor
public enum EnumHidableDetail {

	TEXTURE(pokemon -> pokemon.getCustomTexture() != null && !pokemon.getCustomTexture().isEmpty(), pokemon -> new KeyDetailHolder(PokemonMsgConfigKeys.PE_BASE_TEXTURE, null)),
	UNBREEDABLE(pokemon -> new PokemonSpec("unbreedable").matches(pokemon), pokemon -> new KeyDetailHolder(PokemonMsgConfigKeys.PE_BASE_UNBREEDABLE, null)),
	POKERUS(pokemon -> pokemon.getPokerus() != null, pokemon -> new KeyDetailHolder(PokemonMsgConfigKeys.PE_BASE_POKERUS, null)),
	CLONES(pokemon -> pokemon.getSpecies().equals(EnumSpecies.Mew), pokemon -> new KeyDetailHolder(PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_MEW_CLONES, null)),
	ENCHANTED(pokemon -> ReforgedEntry.LakeTrio.isMember(pokemon.getSpecies()), pokemon -> new KeyDetailHolder(PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_LAKE_TRIO, null)),
	EGG(Pokemon::isEgg, egg -> {
		int total = (egg.getBaseStats().eggCycles + 1) * PixelmonConfig.stepsPerEggCycle;
		int walked = egg.getEggSteps() + ((egg.getBaseStats().eggCycles - egg.getEggCycles()) * PixelmonConfig.stepsPerEggCycle);
		String out = String.format("%d/%d", walked, total);
		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("gts_egg_steps_walked", src -> Optional.of(Text.of(out)));
		return new KeyDetailHolder(PokemonMsgConfigKeys.PE_BASE_EGGSTEPS, tokens);
	}),
	;

	private Predicate<Pokemon> condition;
	private Function<Pokemon, KeyDetailHolder> field;
}
