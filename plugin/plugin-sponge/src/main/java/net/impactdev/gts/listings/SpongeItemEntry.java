package net.impactdev.gts.listings;

import com.google.common.collect.Lists;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.listings.makeup.SpongeDisplay;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.gts.util.DataViewJsonManager;
import net.impactdev.gts.sponge.utils.Utilities;
import net.kyori.text.TextComponent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.InventoryTransformation;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
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
		TextComponent.Builder builder = TextComponent.builder();
		TextComponent.Builder actual = TextComponent.builder();
		builder.append(LegacyComponentSerializer.legacy().deserialize(
				this.getOrCreateElement().get(Keys.DISPLAY_NAME)
						.map(TextSerializers.LEGACY_FORMATTING_CODE::serialize)
						.orElse(this.getOrCreateElement().getTranslation().get())
		));

		if(this.item.get(Keys.DISPLAY_NAME).isPresent()) {
			builder.hoverEvent(HoverEvent.showText(
					TextComponent.builder()
							.append(this.getOrCreateElement().getTranslation().get())
							.build()
			));
		}

		return actual.append(builder.build()).build();
	}

	@Override
	public TextComponent getDescription() {
		TextComponent.Builder builder = TextComponent.builder();
		if(this.item.getQuantity() > 1) {
			builder.append(this.item.getQuantity() + "x ");
		}

		return builder.append(this.getName()).build();
	}

	@Override
	public Display<ItemStack> getDisplay(UUID viewer, Listing listing) {
		ItemStack.Builder designer = ItemStack.builder();
		designer.fromSnapshot(this.getOrCreateElement());

		if(this.getOrCreateElement().get(Keys.DISPLAY_NAME).isPresent()) {
			Text name = this.getOrCreateElement().get(Keys.DISPLAY_NAME).get();
			if(name.getColor() == TextColors.NONE) {
				designer.add(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, name));
			}
		} else {
			designer.add(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, this.getOrCreateElement().getTranslation().get()));
		}

		return new SpongeDisplay(designer.build());
	}

	@Override
	public boolean give(UUID receiver) {
		Optional<Player> player = Sponge.getServer().getPlayer(receiver);
		if(player.isPresent()) {
			MainPlayerInventory inventory = player.get().getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class));
			return inventory.transform(InventoryTransformation.of(
					QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class),
					QueryOperationTypes.INVENTORY_TYPE.of(GridInventory.class))
				)
				.offer(this.getOrCreateElement().createStack())
				.getType()
				.equals(InventoryTransactionResult.Type.SUCCESS);
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
		return Optional.of("https://static.wikia.nocookie.net/minecraft_gamepedia/images/e/e7/Diamond_Pickaxe_JE3_BE3.png/revision/latest/scale-to-width-down/150?cb=20200226193952");
	}

	@Override
	public List<String> getDetails() {
		return Lists.newArrayList(
				"Testing",
				"123"
		);
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
