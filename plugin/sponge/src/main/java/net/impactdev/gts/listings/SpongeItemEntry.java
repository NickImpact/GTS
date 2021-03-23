package net.impactdev.gts.listings;

import com.google.common.collect.Lists;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.data.NBTMapper;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.listings.data.NBTTranslator;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.sponge.listings.makeup.SpongeDisplay;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.impactor.api.services.text.MessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.nbt.NBTTagCompound;
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
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.rmi.CORBA.Util;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@GTSKeyMarker("items")
public class SpongeItemEntry extends SpongeEntry<ItemStackSnapshot> {

	private static final Function<ItemStackSnapshot, JObject> writer = snapshot -> {
		try {
			DataContainer container = snapshot.toContainer();
			return new NBTMapper().from(NBTTranslator.getInstance().translateData(container));
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
		TextComponent.Builder builder = Component.text();
		TextComponent.Builder actual = Component.text();
		builder.append(LegacyComponentSerializer.legacyAmpersand().deserialize(
				this.getOrCreateElement().get(Keys.DISPLAY_NAME)
						.map(TextSerializers.LEGACY_FORMATTING_CODE::serialize)
						.orElse(this.getOrCreateElement().getTranslation().get())
		));

		if(this.item.get(Keys.DISPLAY_NAME).isPresent()) {
			builder.hoverEvent(HoverEvent.showText(
					Component.text()
							.append(Component.text(this.getOrCreateElement().getTranslation().get()))
							.build()
			));
		}

		return actual.append(builder.build()).build();
	}

	@Override
	public TextComponent getDescription() {
		TextComponent.Builder builder = Component.text();
		if(this.item.getQuantity() > 1) {
			builder.append(Component.text(this.item.getQuantity() + "x "));
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
				if(!GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.ITEMS_ALLOW_ANVIL_NAMES)) {
					NBTTagCompound nbt = NBTTranslator.getInstance().translate(slot.peek().get().toContainer());
					if (nbt.hasKey("tag")) {
						if (nbt.getCompoundTag("tag").hasKey("GTS-Anvil")) {
							return;
						}
					}
				}

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
		MessageService<Text> parser = Impactor.getInstance().getRegistry().get(MessageService.class);

		return parser.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ITEM_DISCORD_DETAILS), Lists.newArrayList(this::getOrCreateElement))
				.stream()
				.map(Text::toPlain)
				.collect(Collectors.toList());
	}

	@Override
	public int getVersion() {
		return 2;
	}

	@Override
	public JObject serialize() {
		return new JObject()
				.add("version", this.getVersion())
				.add("item", writer.apply(this.item));
	}

}
