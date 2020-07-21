package me.nickimpact.gts.ui;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.gui.InventoryDimensions;
import com.nickimpact.impactor.spigot.ui.SpigotIcon;
import com.nickimpact.impactor.spigot.ui.SpigotLayout;
import com.nickimpact.impactor.spigot.ui.SpigotUI;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.wrappers.EntryClassification;
import me.nickimpact.gts.api.wrappers.EntryRegistry;
import me.nickimpact.gts.config.MsgConfigKeys;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SpigotSellUI {

	private SpigotUI ui;
	private Player viewer;

	public SpigotSellUI(Player viewer) {
		this.viewer = viewer;
		int height = 3;

		List<SpigotIcon> icons = this.fetchIcons();
		if(icons.size() > 7) {
			height = 5;
		}

		this.ui = SpigotUI.builder()
				.title(GTS.getInstance().getTokenService().process(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.UI_SELL_ITEMS_TITLE), viewer, null, null))
				.size(45)
				.build();
		this.ui.define(this.forgeDesign(icons, height));
	}

	public void open() {
		this.ui.open(this.viewer);
	}

	private SpigotLayout forgeDesign(List<SpigotIcon> icons, int height) {
		SpigotLayout.SpigotLayoutBuilder slb = SpigotLayout.builder();
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

	private List<SpigotIcon> fetchIcons() {
		List<SpigotIcon> icons = Lists.newArrayList();

		EntryRegistry er = GTS.getInstance().getAPIService().getEntryRegistry();
		for(EntryClassification classification : er.getClassifications()) {
			ItemStack display = new ItemStack(Material.matchMaterial(classification.getItemRep()));
			ItemMeta meta = display.getItemMeta();
			meta.setDisplayName(ChatColor.YELLOW + classification.getPrimaryIdentifier());
			display.setItemMeta(meta);

			SpigotIcon icon = new SpigotIcon(display);
			icon.addListener(clickable -> {
				this.ui.close(this.viewer);
				classification.getUi().createFor(viewer).getDisplay().open(viewer);
			});
			icons.add(icon);
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
