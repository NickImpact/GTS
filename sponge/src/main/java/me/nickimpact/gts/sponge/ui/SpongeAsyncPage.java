package me.nickimpact.gts.sponge.ui;

import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.common.ui.AsyncPage;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class SpongeAsyncPage<U> extends AsyncPage<Player, U, SpongeUI, SpongeIcon, SpongeLayout, Text, ItemType> {

	public SpongeAsyncPage(GTSPlugin plugin, Player viewer, CompletableFuture<List<U>> supplier) {
		super(plugin, viewer, supplier);
	}

}
