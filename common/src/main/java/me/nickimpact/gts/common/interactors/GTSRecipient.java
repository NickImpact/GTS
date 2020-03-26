package me.nickimpact.gts.common.interactors;

import me.nickimpact.gts.api.listings.interactors.Recipient;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class GTSRecipient implements Recipient {

	@Override
	public @NonNull UUID getUUID() {
		return null;
	}

	@Override
	public @Nullable String getUsername() {
		return null;
	}

}
