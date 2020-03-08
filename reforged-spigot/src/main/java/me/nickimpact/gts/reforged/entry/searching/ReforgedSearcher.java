package me.nickimpact.gts.reforged.entry.searching;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import me.nickimpact.gts.api.searching.Searcher;
import me.nickimpact.gts.reforged.entry.ReforgedEntry;
import me.nickimpact.gts.spigot.SpigotListing;

public class ReforgedSearcher implements Searcher {

	@Override
	@SuppressWarnings("all")
	public boolean parse(Listing listing, String input) {
		PokemonSpec spec = PokemonSpec.from(input.split(" "));
		SpigotListing sl = (SpigotListing) listing;
		if(sl.getEntry() instanceof ReforgedEntry) {
			Pokemon pokemon = ((ReforgedEntry) sl.getEntry()).getEntry();
			return spec.name != null && spec.matches(pokemon);
		}

		return false;

	}

}
