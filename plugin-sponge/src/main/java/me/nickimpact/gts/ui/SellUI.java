package me.nickimpact.gts.ui;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.utils.SpongeItemTypeUtil;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

public class SellUI {

	private SpongeUI ui;
	private Player viewer;

	public SellUI(Player viewer) {
		this.viewer = viewer;
		int height = 3;

		List<SpongeIcon> icons = this.fetchIcons();
		if(icons.size() > 7) {
			height = 5;
		}

		this.ui = SpongeUI.builder()
				.title(GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.UI_SELL_ITEMS_TITLE, null, null))
				.dimension(InventoryDimension.of(9, height))
				.build();
		this.ui.define(this.forgeDesign(icons, height));
	}

	public void open() {
		this.ui.open(this.viewer);
	}

	private SpongeLayout forgeDesign(List<SpongeIcon> icons, int height) {
		SpongeLayout.SpongeLayoutBuilder slb = SpongeLayout.builder();
		slb.dimension(9, height).border();

		int i = 0;
		for(int slot : Locations.getForSize(icons.size() % 7).locations) {
			if(i > icons.size()) {
				break;
			}

			slb.slot(icons.get(i++), slot + 9);
		}

		if(height > 3) {
			// TODO - Paging options
		}

		return slb.build();
	}

	private List<SpongeIcon> fetchIcons() {
		List<SpongeIcon> icons = Lists.newArrayList();

		EntryRegistry er = GTS.getInstance().getService().getEntryRegistry();
		for(EntryClassification classification : er.getClassifications()) {
			if(viewer.hasPermission("gts.command.sell." + classification.getPrimaryIdentifier().toLowerCase())) {
				SpongeIcon icon = new SpongeIcon(ItemStack.builder()
						.itemType(SpongeItemTypeUtil.getOrDefaultItemTypeFromID(classification.getItemRep()))
						.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, classification.getPrimaryIdentifier()))
						.build()
				);
				icon.addListener(clickable -> {
					this.ui.close(this.viewer);
					classification.getUi().createFor(viewer).getDisplay().open(viewer);
				});
				icons.add(icon);
			}
		}

		return icons;
	}

	private enum Locations {
		ZERO    (),
		ONE     (4),
		TWO     (3, 5),
		THREE   (2, 4, 6),
		FOUR    (1, 3, 5, 7),
		FIVE    (2, 3, 4, 5, 6),
		SIX     (1, 2, 3, 5, 6, 7),
		SEVEN   (1, 2, 3, 4, 5, 6, 7);

		private int[] locations;

		Locations(int... slots) {
			this.locations = slots;
		}

		public static Locations getForSize(int size) {
			switch(size) {
				case 0:
					return ZERO;
				case 1:
					return ONE;
				case 2:
					return TWO;
				case 3:
					return THREE;
				case 4:
					return FOUR;
				case 5:
					return FIVE;
				case 6:
					return SIX;
				case 7:
				default:
					return SEVEN;
			}
		}
	}
}
