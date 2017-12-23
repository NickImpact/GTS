package com.nickimpact.gts.ui;

import com.nickimpact.gts.api.gui.InventoryBase;
import com.nickimpact.gts.api.listings.Listing;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class ConfirmUI extends InventoryBase {

	/** The listing that the player might wish to purchase */
	private Listing target;

	/** The lingering search condition that can be restored if the user cancels purchase */
	private Predicate<Listing> search;

	public ConfirmUI(Player player, Listing listing, @Nullable Predicate<Listing> search) {
		super(player, 3, Text.of(TextColors.RED, "GTS ", TextColors.GRAY, "\u00BB ", TextColors.DARK_AQUA, "Confirmation"));
		this.target = listing;
	}
}
