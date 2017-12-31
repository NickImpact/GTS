package com.nickimpact.gts.api.listings.data;

import com.nickimpact.gts.api.listings.pricing.Price;
import lombok.Getter;
import lombok.Setter;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;
import java.util.UUID;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Getter
@Setter
public class AuctionData {

	private int numIncrements;

	private Price increment;

	private UUID highBidder;

	private List<Player> listeners;
}
