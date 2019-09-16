package me.nickimpact.gts.utils;

import me.nickimpact.gts.api.listings.Listing;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeFormatUtils {

	public static final DateTimeFormatter base = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss a z").withZone(ZoneId.systemDefault());

	public static String formatExpiration(Listing listing) {
		return listing.getExpiration() == null || listing.getExpiration().equals(LocalDateTime.MAX) ? "Infinite" : listing.getExpiration().format(base);
	}
}
