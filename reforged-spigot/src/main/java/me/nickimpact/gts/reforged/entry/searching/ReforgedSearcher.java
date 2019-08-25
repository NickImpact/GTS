package me.nickimpact.gts.reforged.entry.searching;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.plugin.PluginInstance;
import me.nickimpact.gts.api.searching.Searcher;
import me.nickimpact.gts.reforged.entry.ReforgedEntry;
import me.nickimpact.gts.spigot.SpigotListing;

import java.util.List;
import java.util.stream.Collectors;

public class ReforgedSearcher implements Searcher {

	@Override
	public List<Listing> parse(String input) {
		PokemonSpec spec = PokemonSpec.from(input.split(" "));
		return (List<Listing>) PluginInstance.getInstance().getAPIService().getListingManager().getListings().stream().filter(
				listing -> {
					SpigotListing sl = (SpigotListing) listing;
					if(sl.getEntry() instanceof ReforgedEntry) {
						Pokemon pokemon = ((ReforgedEntry) sl.getEntry()).getEntry();
						if(spec.matches(pokemon)) {
							return true;
						}
					}

					return false;
				}
		).collect(Collectors.toList());
	}

}
