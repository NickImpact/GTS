package net.impactdev.gts.common.utils.datetime;

import net.impactdev.gts.api.listings.Listing;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeFormatUtils {

	public static final DateTimeFormatter base = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss a z").withZone(ZoneId.systemDefault());

	public static String formatExpiration(Listing listing) {
		return listing.getExpiration().equals(LocalDateTime.MAX) ? "Infinite" : listing.getExpiration().format(base);
	}
}
