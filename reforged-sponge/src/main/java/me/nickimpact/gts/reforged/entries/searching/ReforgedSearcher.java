package me.nickimpact.gts.reforged.entries.searching;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import me.nickimpact.gts.api.searching.Searcher;
import me.nickimpact.gts.reforged.entries.ReforgedEntry;
import me.nickimpact.gts.sponge.SpongeListing;

public class ReforgedSearcher implements Searcher {

	@Override
	@SuppressWarnings("all")
	public boolean parse(Listing listing, String input) {
		PokemonSpec spec = PokemonSpec.from(input.split(" "));
		SpongeListing sl = (SpongeListing) listing;
		if(sl.getEntry() instanceof ReforgedEntry) {
			Pokemon pokemon = ((ReforgedEntry) sl.getEntry()).getEntry();
			return spec.matches(pokemon);
		}

		return false;

	}

}
