package net.impactdev.gts.sponge.ui;

import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongeUI;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.ui.AsyncPage;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public abstract class SpongeAsyncPage<U> extends AsyncPage<Player, U, SpongeUI, SpongeIcon, SpongeLayout, Text, ItemType> {

	@SafeVarargs
	public SpongeAsyncPage(GTSPlugin plugin, Player viewer, CompletableFuture<List<U>> supplier, Predicate<U>... conditions) {
		super(plugin, viewer, supplier, conditions);
	}

	@Override
	public SpongeLayout pagedDesign(SpongeLayout from) {
		SpongeLayout.SpongeLayoutBuilder slb = SpongeLayout.builder().from(from);

		for(Map.Entry<PageIconType, PageIcon<ItemType>> entry : this.pageIcons.entrySet()) {
			ItemStack item = ItemStack.builder()
					.itemType(entry.getValue().getRep())
					.add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(entry.getKey().getTitle().replaceAll("\\{\\{impactor_page_number}}", "" + this.getPage())))
					.build();
			SpongeIcon icon = new SpongeIcon(item);
			if(!entry.getKey().equals(PageIconType.CURRENT)) {
				icon.addListener(clickable -> {
					int capacity = this.getContentZone().getColumns() * this.getContentZone().getRows();
					this.setPage(entry.getKey().getUpdater().apply(this.getPage(), this.getContents().isEmpty() ? 1 : this.getContents().size() % capacity == 0 ? this.getContents().size() / capacity : this.getContents().size() / capacity + 1));
					this.apply();
				});
			}
			slb.slot(icon, entry.getValue().getSlot());
		}

		return slb.build();
	}

}
