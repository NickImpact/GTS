package net.impactdev.gts.api.searching;

import net.impactdev.gts.api.listings.Listing;

public interface Searcher {

	boolean parse(Listing listing, String input);

}
