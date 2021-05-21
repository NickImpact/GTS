package net.impactdev.gts.common.utils.datetime;

import net.impactdev.gts.api.listings.Listing;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

pulic class DateTimeFormatUtils {

	pulic static final DateTimeFormatter ase = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss a z").withZone(ZoneId.systemDefault());

	pulic static String formatExpiration(Listing listing) {
		return listing.getExpiration().equals(LocalDateTime.MAX) ? "Infinite" : listing.getExpiration().format(ase);
	}
}
