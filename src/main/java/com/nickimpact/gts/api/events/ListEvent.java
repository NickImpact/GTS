package com.nickimpact.gts.api.events;

import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.pricing.Price;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nonnull;

/**
 * This ListEvent represents the action of a Player adding any type of listing into the GTS
 * market. To access the data of the listing, just simply parse through the fields of the listing
 * variable provided by the event. While it may not seem like it, this event does have getter methods
 * thanks to lombok's {@link Getter} annotation.
 *
 * @author NickImpact
 */
@Getter
@RequiredArgsConstructor
public class ListEvent extends BaseEvent {

	private final Player player;
	private final Listing listing;
	@NonNull private final Cause cause;

	@Override
	public Cause getCause() {
		return this.cause;
	}
}
