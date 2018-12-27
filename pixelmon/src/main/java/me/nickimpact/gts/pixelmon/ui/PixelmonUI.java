package me.nickimpact.gts.pixelmon.ui;

import com.nickimpact.impactor.gui.v2.Icon;
import com.nickimpact.impactor.gui.v2.Layout;
import com.nickimpact.impactor.gui.v2.UI;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class PixelmonUI extends EntryUI {

	private UI display;

	public PixelmonUI() {}

	private PixelmonUI(Player player) {
		super(player);
		this.display = this.createUI(player);
	}

	@Override
	public PixelmonUI createFor(Player player) {
		return new PixelmonUI(player);
	}

	@Override
	public UI getDisplay() {
		return this.display;
	}

	@Override
	protected UI createUI(Player player) {
		UI display = UI.builder()
				.title(Text.of(TextColors.RED, "GTS ", TextColors.GRAY, "(", TextColors.DARK_AQUA, "Pixelmon", TextColors.GRAY, ")"))
				.dimension(InventoryDimension.of(9, 6))
				.build(GTS.getInstance());
		return display.define(this.forgeLayout(player));
	}

	@Override
	protected Layout forgeLayout(Player player) {
		Layout.Builder lb = Layout.builder();
		lb.row(Icon.BORDER, 0).row(Icon.BORDER, 2);
		lb.column(Icon.BORDER, 0).slots(Icon.BORDER, 16, 34, 43, 52);


		return null;
	}
}
