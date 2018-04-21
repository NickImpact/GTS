package com.nickimpact.gts.api.listings.data;

import com.google.common.collect.Lists;
import com.nickimpact.gts.api.listings.pricing.Auctionable;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.entries.prices.MoneyPrice;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

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

	private final MoneyPrice increment;

	private UUID highBidder;

	private String hbNameString;

	@Deprecated
	private transient Text hbName;

	private transient List<Player> listeners = Lists.newArrayList();

	private boolean ownerReceived;

	public AuctionData(MoneyPrice increment) {
		this.increment = increment;
	}
}
