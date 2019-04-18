package me.nickimpact.gts.ui;

import com.nickimpact.impactor.spigot.ui.SpigotUI;
import me.nickimpact.gts.api.listings.Listing;
import org.bukkit.entity.Player;

public class ConfirmUI {

	private SpigotUI view;
	private Player viewer;
	private Listing focus;

	public ConfirmUI(Player viewer, Listing focus) {

	}

	public void open() {
		this.view.open(viewer);
	}
}
