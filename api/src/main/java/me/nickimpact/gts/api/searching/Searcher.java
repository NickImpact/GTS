package me.nickimpact.gts.api.searching;

import me.nickimpact.gts.api.listings.Listing;

public interface Searcher {

	boolean parse(Listing listing, String input);

}
