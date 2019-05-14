package me.nickimpact.gts.reforged.entry;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.LakeTrioStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.MewStats;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;
import java.util.function.Predicate;

@Getter
@AllArgsConstructor
public enum EnumHidableDetail {

	TEXTURE(pokemon -> pokemon.getCustomTexture() != null && !pokemon.getCustomTexture().isEmpty(), poke -> {
		StringBuilder sb = new StringBuilder();
		String[] split = poke.getCustomTexture().split("\\s+");

		boolean first = true;
		for(String word : split) {
			if(!first) {
				sb.append(" ");
			}
			sb.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
			first = false;
		}

		return "&7Texture: &e" + sb.toString();
	}),
	UNBREEDABLE(pokemon -> new PokemonSpec("unbreedable").matches(pokemon), poke -> "&cUnbreedable"),
	POKERUS(pokemon -> pokemon.getPokerus() != null, poke -> "&dInflicted with Pok\u00e9rus"),
	CLONES(pokemon -> pokemon.getSpecies().equals(EnumSpecies.Mew), mew -> "&7Clones Left: &e" + (3 - ((MewStats) mew.getExtraStats()).numCloned)),
	ENCHANTED(pokemon -> ReforgedEntry.LakeTrio.isMember(pokemon.getSpecies()), lakey -> "&7Enchants Left: &e" + (3 - ((LakeTrioStats) lakey.getExtraStats()).numEnchanted)),
	;

	private Predicate<Pokemon> condition;
	private Function<Pokemon, String> field;

}
