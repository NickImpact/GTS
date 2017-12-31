package com.nickimpact.gts.api.listings.entries;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Getter
@RequiredArgsConstructor
public class EntryHolder {

	private final int id;
	private final UUID uuid;
	private final Entry entry;
}
