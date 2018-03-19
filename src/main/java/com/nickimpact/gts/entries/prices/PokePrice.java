package com.nickimpact.gts.entries.prices;

import com.nickimpact.gts.api.json.Typing;
import com.nickimpact.gts.api.listings.pricing.Price;
import com.nickimpact.gts.trades.PokeRequest;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Typing("Pokemon")
public class PokePrice extends Price<PokeRequest> {

	public PokePrice(PokeRequest price) {
		super(price);
	}

	@Override
	public Text getText() {
		return null;
	}

	@Override
	public boolean canPay(User user) throws Exception {
		return false;
	}

	@Override
	public void pay(User user) {

	}

	@Override
	public BigDecimal calcTax(Player player) {
		return new BigDecimal(-1);
	}

	@Override
	public void reward(UUID uuid) {

	}

	@Override
	public void openCreateUI(Player player) {

	}
}
