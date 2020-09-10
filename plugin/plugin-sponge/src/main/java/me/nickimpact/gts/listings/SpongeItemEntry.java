package me.nickimpact.gts.listings;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.Impactor;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.json.factory.JObject;
import com.nickimpact.impactor.api.services.text.MessageService;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.data.registry.GTSKeyMarker;
import me.nickimpact.gts.api.listings.auctions.Auction;
import me.nickimpact.gts.api.listings.buyitnow.BuyItNow;
import me.nickimpact.gts.api.listings.makeup.Display;
import me.nickimpact.gts.common.config.MsgConfigKeys;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.sponge.listings.makeup.SpongeDisplay;
import me.nickimpact.gts.sponge.listings.makeup.SpongeEntry;
import me.nickimpact.gts.util.DataViewJsonManager;
import me.nickimpact.gts.util.Utilities;
import net.kyori.text.TextComponent;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.InventoryTransformation;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

@GTSKeyMarker("item")
public class SpongeItemEntry extends SpongeEntry<ItemStackSnapshot> {

	private static final Function<ItemStackSnapshot, JObject> writer = snapshot -> {
		try {
			JObject result = new JObject();
			DataContainer container = snapshot.toContainer();
			DataViewJsonManager.writeDataViewToJSON(result, container);
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Failed to write JSON data for item snapshot", e);
		}
	};

	private final ItemStackSnapshot item;

	public SpongeItemEntry(ItemStackSnapshot item) {
		this.item = item;
	}

	@Override
	public ItemStackSnapshot getOrCreateElement() {
		return this.item;
	}

	@Override
	public TextComponent getName() {
		return LegacyComponentSerializer.legacy().deserialize(
				this.getOrCreateElement().get(Keys.DISPLAY_NAME)
						.map(TextSerializers.FORMATTING_CODE::serialize)
						.orElse(this.getOrCreateElement().getTranslation().get())
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Display<ItemStack> getDisplay(UUID viewer, Listing listing) {
		final Config lang = GTSPlugin.getInstance().getMsgConfig();
		final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

		ItemStack.Builder designer = ItemStack.builder();
		designer.fromSnapshot(this.getOrCreateElement());

		List<Text> lore = Lists.newArrayList();
		if(this.getOrCreateElement().get(Keys.ITEM_LORE).isPresent()) {
			lore.addAll(this.getOrCreateElement().get(Keys.ITEM_LORE).get());
			lore.addAll(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_LISTING_DETAIL_SEPARATOR)));
		}

		if(listing instanceof Auction) {
			Auction auction = (Auction) listing;

			List<String> input = lang.get(MsgConfigKeys.UI_AUCTION_DETAILS);
			List<Supplier<Object>> sources = Lists.newArrayList(() -> auction);
			lore.addAll(service.parse(input, sources));
		} else if(listing instanceof BuyItNow) {
			BuyItNow bin = (BuyItNow) listing;

			List<String> input = lang.get(MsgConfigKeys.UI_BIN_DETAILS);
			List<Supplier<Object>> sources = Lists.newArrayList(() -> bin);
			lore.addAll(service.parse(input, sources));
		}

		designer.add(Keys.ITEM_LORE, lore);

		return new SpongeDisplay(designer.build());
	}

	@Override
	public boolean give(UUID receiver) {
		Optional<Player> player = Sponge.getServer().getPlayer(receiver);
		if(player.isPresent()) {
			MainPlayerInventory inventory = player.get().getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class));
			if(inventory.size() == inventory.capacity()) {
				return false;
			}

			player.get().getInventory()
					.transform(InventoryTransformation.of(
							QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class),
							QueryOperationTypes.INVENTORY_TYPE.of(GridInventory.class))
					)
					.offer(this.getOrCreateElement().createStack());
		}

		return false;
	}

	@Override
	public boolean take(UUID depositor) {
		AtomicBoolean result = new AtomicBoolean(false);
		Optional<Player> player = Sponge.getServer().getPlayer(depositor);
		player.ifPresent(pl -> {
			ItemStack rep = this.item.createStack();
			Slot slot = pl.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class))
					.query(QueryOperationTypes.ITEM_STACK_EXACT.of(rep))
					.first();
			if(slot.peek().isPresent()) {
				slot.poll();
				result.set(true);
			}
		});

		return result.get();
	}

	@Override
	public Optional<String> getThumbnailURL() {
		return Optional.empty();
	}

	@Override
	public List<String> getDetails() {
		return Lists.newArrayList();
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public JObject serialize() {
		return new JObject()
				.add("version", this.getVersion())
				.add("item", writer.apply(this.item));
	}

}
