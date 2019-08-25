package me.nickimpact.gts.api.searching;

import me.nickimpact.gts.api.listings.Listing;

import java.util.List;

public interface Searcher {

	List<Listing> parse(String input);

}
