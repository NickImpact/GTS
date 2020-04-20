package me.nickimpact.gts.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.gui.InventoryDimensions;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import lombok.AllArgsConstructor;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.util.groupings.Tuple;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.manager.SpongeListingManager;
import me.nickimpact.gts.sponge.listings.SpongeListing;
import me.nickimpact.gts.sponge.ui.SpongeAsyncPage;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class SpongeMainMenu extends SpongeAsyncPage<SpongeListing> {

	private Collection<Predicate<SpongeListing>> conditions = Lists.newArrayList();
	private Class<? extends Entry> filter;

	private Task runner;

	public SpongeMainMenu(GTSPlugin plugin, Player viewer) {
		super(plugin, viewer, GTSService.getInstance().getRegistry().get(SpongeListingManager.class).fetchListings());
	}

	@Override
	protected Text getTitle() {
		return Text.of(TextColors.RED, "GTS", TextColors.GRAY, " \u00bb ", TextColors.DARK_AQUA, "Listings");
	}

	@Override
	protected Map<PageIconType, PageIcon<ItemType>> getPageIcons() {
		return Maps.newHashMap();
	}

	@Override
	protected InventoryDimensions getContentZone() {
		return new InventoryDimensions(7, 4);
	}

	@Override
	protected Tuple<Integer, Integer> getOffsets() {
		return new Tuple<>(0, 2);
	}

	@Override
	protected Tuple<Long, TimeUnit> getTimeout() {
		return new Tuple<>((long) 5, TimeUnit.SECONDS);
	}

	@Override
	protected SpongeLayout design() {
		SpongeLayout.SpongeLayoutBuilder slb = SpongeLayout.builder();
		slb.slots(SpongeIcon.BORDER, 1, 10, 19, 28, 37, 38, 39, 40, 41, 42, 42, 43, 44, 9, 36, 46, 48, 52);
		this.createBottomPanel(layout);
		this.createFilterOptions(layout);

		return slb.build();
	}

	@Override
	protected SpongeUI build(SpongeLayout layout) {
		return SpongeUI.builder()
				.title(this.title)
				.dimension(InventoryDimension.of(9, 6))
				.build()
				.define(this.layout);
	}

	@Override
	protected SpongeIcon getLoadingIcon() {
		return new SpongeIcon(ItemStack.builder()
				.itemType(ItemTypes.STAINED_GLASS_PANE)
				.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Fetching Listings..."))
				.add(Keys.DYE_COLOR, DyeColors.YELLOW)
				.build()
		);
	}

	@Override
	protected SpongeIcon getTimeoutIcon() {
		return new SpongeIcon(ItemStack.builder()
				.itemType(ItemTypes.STAINED_GLASS_PANE)
				.add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Fetch Timed Out"))
				.add(Keys.DYE_COLOR, DyeColors.RED)
				.add(Keys.ITEM_LORE, Lists.newArrayList(
						Text.of(TextColors.GRAY, "GTS failed to lookup the stored"),
						Text.of(TextColors.GRAY, "listings in a timely manner..."),
						Text.EMPTY,
						Text.of(TextColors.GRAY, "Please retry opening the menu!")
				))
				.build()
		);
	}

	private void createFilterOptions(SpongeLayout layout) {
//		int size = GTSService.getInstance().getEntryRegistry().getClassifications().size();
//		if(size > 6) {
//
//		} else {
//
//		}
	}

	private void createBottomPanel(SpongeLayout layout) {
		// TODO - This should really only feature the creation of the auction/quick purchase managers
		// Perhaps also add a close button
	}

	private List<SpongeListing> fetch() {
		List<SpongeListing> results = Lists.newArrayList();
		SpongeListingManager manager = GTSService.getInstance().getServiceManager().get(SpongeListingManager.class).get();
		if(!this.conditions.isEmpty()) {

		}

		return results;
	}

	@AllArgsConstructor
	private static class JustPlayer implements Predicate<SpongeListing> {

		private UUID viewer;

		@Override
		public boolean test(SpongeListing listing) {
			return listing.getLister().equals(this.viewer);
		}

	}

}
