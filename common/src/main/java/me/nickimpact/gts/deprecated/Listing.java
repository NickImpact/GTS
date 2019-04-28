package me.nickimpact.gts.deprecated;

import lombok.Getter;
import java.util.Date;
import java.util.UUID;

@Getter
@Deprecated
public class Listing {
	/** The Unique ID of the element */
	private UUID uuid;

	/** The uuid of the element owner */
	private UUID ownerUUID;

	private Entry entry;

	/** When the lot will expire, if the above is true */
	private Date expiration;
}
