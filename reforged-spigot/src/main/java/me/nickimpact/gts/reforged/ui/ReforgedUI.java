package me.nickimpact.gts.reforged.ui;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.spigot.ui.SpigotIcon;
import com.nickimpact.impactor.spigot.ui.SpigotLayout;
import com.nickimpact.impactor.spigot.ui.SpigotUI;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.reforged.utils.SpriteItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class ReforgedUI implements EntryUI<Player> {

	private Player viewer;
	private SpigotUI ui;

	private Pokemon selection;

	public ReforgedUI() {}

	private ReforgedUI(Player player) {
		this.viewer = player;
		this.ui = this.createDisplay();
	}

	@Override
	public ReforgedUI createFor(Player player) {
		return new ReforgedUI(player);
	}

	@Override
	public SpigotUI getDisplay() {
		return this.ui;
	}

	private SpigotUI createDisplay() {
		return SpigotUI.builder().title("&cGTS &7(&3Pokemon&7)").size(54).build().define(this.formatDisplay());
	}

	private SpigotLayout formatDisplay() {
		SpigotLayout.SpigotLayoutBuilder slb = SpigotLayout.builder();
		slb.rows(SpigotIcon.BORDER, 0, 2);
		slb.column(SpigotIcon.BORDER, 7);
		slb.slots(SpigotIcon.BORDER, 9);

		int index = 0;
		for(Pokemon pokemon : Pixelmon.storageManager.getParty(this.viewer.getUniqueId()).getAll()) {
			if(pokemon == null) {
				index++;
				continue;
			}

			ItemStack item = SpriteItemUtil.createPicture(pokemon);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', String.format(
					!pokemon.isEgg() ? "&3%s &7| &aLvl %d" : "&3%s Egg",
					pokemon.getSpecies().getLocalizedName(),
					pokemon.getLevel()))
			);

			List<String> details = SpriteItemUtil.getDetails(pokemon);
			meta.setLore(details.subList(0, details.size() - 1).stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
			item.setItemMeta(meta);
			SpigotIcon icon = new SpigotIcon(item);
			icon.addListener(clickable -> {

			});
			slb.slot(icon, 10 + index++);
		}

		return slb.build();
	}
}
