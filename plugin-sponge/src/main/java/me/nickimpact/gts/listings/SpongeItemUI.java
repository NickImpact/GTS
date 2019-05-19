package me.nickimpact.gts.listings;

import com.nickimpact.impactor.api.gui.UI;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

public class SpongeItemUI implements EntryUI<Player> {

	@Override
	public EntryUI createFor(Player player) {
		return null;
	}

	@Override
	public UI getDisplay() {
		return null;
	}
}
