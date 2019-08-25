package me.nickimpact.gts.listings;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.spigot.ui.SpigotIcon;
import com.nickimpact.impactor.spigot.ui.SpigotLayout;
import com.nickimpact.impactor.spigot.ui.SpigotUI;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.config.ConfigKeys;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;
import java.util.stream.Collectors;

public class SpigotItemUI implements EntryUI<Player> {

	private Player player;
	private SpigotUI display;
	private ItemStack selection;

	private int size = 1;

	private double price;

	public SpigotItemUI() {}

	private SpigotItemUI(Player player) {
		this.player = player;
		this.display = createUI();
	}

	@Override
	public EntryUI createFor(Player player) {
		return new SpigotItemUI(player);
	}

	@Override
	public SpigotUI getDisplay() {
		return this.display;
	}

	private SpigotUI createUI() {
		SpigotUI ui = SpigotUI.builder()
				.size(54)
				.title("&cGTS &7(&3Items&7)")
				.build();
		ui.attachListener((player, event) -> {
			if(event.getClickedInventory() != null) {
				int clicked = event.getRawSlot();
				if (clicked >= 54) {
					Optional<ItemStack> item = Optional.ofNullable(event.getCurrentItem());
					item.ifPresent(i -> {
						if(i.getType().equals(Material.AIR) || i.equals(this.selection)) {
							return;
						}

						this.selection = i;
						if(this.size > i.getAmount()) {
							this.size = i.getAmount();
						}

						this.display.setSlot(13, new SpigotIcon(i));
						//this.update();
					});
				}
			}
		});

		return ui.define(this.forgeLayout());
	}

	private SpigotLayout forgeLayout() {
		Config config = GTS.getInstance().getConfiguration();

		SpigotLayout.SpigotLayoutBuilder slb = SpigotLayout.builder().dimension(9, 6);
		slb.rows(SpigotIcon.BORDER, 0, 2);
		slb.slots(SpigotIcon.BORDER, 9, 17, 34, 43, 52);

		ItemStack marker = new ItemStack(Material.BARRIER);
		ItemMeta m = marker.getItemMeta();
		m.setDisplayName(ChatColor.GRAY + "Select an Item...");
		marker.setItemMeta(m);

		slb.slot(new SpigotIcon(marker), 13);

		ItemStack money_inc = new ItemStack(Material.INK_SACK, 1, (short) 10);
		ItemMeta miMeta = money_inc.getItemMeta();
		miMeta.setDisplayName(ChatColor.GREEN + "Increase Price Requested");
		miMeta.setLore(Lists.newArrayList(
			"&7Left Click: &e+" + config.get(ConfigKeys.PRICING_LEFTCLICK_BASE),
			"&7Right Click: &e+" + config.get(ConfigKeys.PRICING_RIGHTCLICK_BASE),
			"&7Shift+Left Click: &e+" + config.get(ConfigKeys.PRICING_LEFTCLICK_SHIFT),
			"&7Shift+Right Click: &e+" + config.get(ConfigKeys.PRICING_RIGHTCLICK_SHIFT)
		).stream().map(l -> ChatColor.translateAlternateColorCodes('&', l)).collect(Collectors.toList()));
		money_inc.setItemMeta(miMeta);
		SpigotIcon miIcon = new SpigotIcon(money_inc);
		miIcon.addListener(clickable -> {
			if(clickable.getEvent().isShiftClick()) {
				if(clickable.getEvent().isRightClick()) {
					this.price = Math.min(config.get(ConfigKeys.MAX_MONEY_PRICE), this.price + config.get(ConfigKeys.PRICING_RIGHTCLICK_SHIFT));
				} else {
					this.price = Math.min(config.get(ConfigKeys.MAX_MONEY_PRICE), this.price + config.get(ConfigKeys.PRICING_LEFTCLICK_SHIFT));
				}
			} else {
				if(clickable.getEvent().isRightClick()) {
					this.price = Math.min(config.get(ConfigKeys.MAX_MONEY_PRICE), this.price + config.get(ConfigKeys.PRICING_RIGHTCLICK_BASE));
				} else {
					this.price = Math.min(config.get(ConfigKeys.MAX_MONEY_PRICE), this.price + config.get(ConfigKeys.PRICING_LEFTCLICK_BASE));
				}
			}
		});

		return slb.build();
	}
}
