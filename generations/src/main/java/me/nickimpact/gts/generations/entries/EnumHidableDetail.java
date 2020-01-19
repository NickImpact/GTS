package me.nickimpact.gts.generations.entries;

import com.google.common.collect.Maps;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.nickimpact.gts.generations.config.PokemonMsgConfigKeys;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@Getter
@AllArgsConstructor
public enum EnumHidableDetail {

	TEXTURE(pokemon -> !pokemon.getSpecialTexture().isEmpty(), pokemon -> new KeyDetailHolder(PokemonMsgConfigKeys.PE_BASE_TEXTURE, null)),
	//UNBREEDABLE(pokemon -> new PokemonSpec("unbreedable").matches(pokemon), pokemon -> new KeyDetailHolder(PokemonMsgConfigKeys.PE_BASE_UNBREEDABLE, null)),
	//POKERUS(pokemon -> pokemon.getPokerus() != null, pokemon -> new KeyDetailHolder(PokemonMsgConfigKeys.PE_BASE_POKERUS, null)),
	CLONES(pokemon -> pokemon.getSpecies().equals(EnumPokemon.Mew), pokemon -> new KeyDetailHolder(PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_MEW_CLONES, null)),
	ENCHANTED(pokemon -> PokemonEntry.LakeTrio.isMember(pokemon.getSpecies()), pokemon -> new KeyDetailHolder(PokemonMsgConfigKeys.POKEMON_ENTRY_BASE_LAKE_TRIO, null)),
	EGG(pokemon -> pokemon.isEgg, egg -> {
		int total = (egg.baseStats.eggCycles + 1) * PixelmonConfig.stepsPerEggCycle;
		int walked = egg.serializeNBT().getInteger("steps") + ((egg.baseStats.eggCycles - egg.eggCycles) * PixelmonConfig.stepsPerEggCycle);
		String out = String.format("%d/%d", walked, total);
		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("gts_egg_steps_walked", src -> Optional.of(Text.of(out)));
		return new KeyDetailHolder(PokemonMsgConfigKeys.PE_BASE_EGGSTEPS, tokens);
	}),
	MINT(pokemon -> pokemon.serializeNBT().hasKey("PseudoNature"), pokemon -> {
		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("gts_mint", src -> Optional.of(Text.of(EnumNature.getNatureFromIndex(pokemon.serializeNBT().getShort("PseudoNature")).getLocalizedName())));
		return new KeyDetailHolder(PokemonMsgConfigKeys.PE_BASE_MINT, tokens);
	})
	;

	private Predicate<EntityPixelmon> condition;
	private Function<EntityPixelmon, KeyDetailHolder> field;

}
