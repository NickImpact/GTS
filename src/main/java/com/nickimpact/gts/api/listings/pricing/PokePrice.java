package com.nickimpact.gts.api.listings.pricing;

import com.nickimpact.gts.api.json.Typing;
import com.nickimpact.gts.trades.PokeRequest;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;

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
	public boolean canPay(Player player) throws Exception {
		return false;
	}

	@Override
	public void pay(Player payer) {

	}

	@Override
	public BigDecimal calcTax(Player player) {
		return new BigDecimal(-1);
	}

	@Override
	public void reward(Player recipient) {

	}

	@Override
	public void openCreateUI(Player player) {

	}
}
