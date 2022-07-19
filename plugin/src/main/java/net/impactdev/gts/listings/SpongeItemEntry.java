package net.impactdev.gts.listings;

import net.impactdev.gts.api.listings.entries.Entry;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.data.NBTMapper;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.data.NBTTranslator;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.sponge.listings.makeup.SpongeDisplay;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.services.text.MessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.nbt.CompoundNBT;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.entity.PrimaryPlayerInventory;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;

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
			CompoundNBT nbt = ((net.minecraft.item.ItemStack) (Object) snapshot.createStack()).serializeNBT();
			return NBTMapper.from(nbt);
		} catch (Exception e) {
			throw new RuntimeException("Failed to write JSON data for item snapshot", e);
		}
	};

	/** The actual item represented by the entry */
	private final ItemStackSnapshot item;

	/** Represents the slot the item should be polled from. This is ONLY for the initial take operation */
	@Nullable private transient final Integer slot;

	/** Represents how the items is displayed to the client */
	private transient Display<ItemStack> display;

	public SpongeItemEntry(ItemStackSnapshot item, @Nullable Integer slot) {
		this.item = item;
		this.slot = slot;
	}

	@Override
	public Class<? extends Entry<ItemStackSnapshot, ItemStack>> type() {
		return SpongeItemEntry.class;
	}

	@Override
	public ItemStackSnapshot getOrCreateElement() {
		return this.item;
	}

	@Override
	public TextComponent getName() {
		TextComponent.Builder builder = Component.text();
		TextComponent.Builder actual = Component.text();
		builder.append(this.getOrCreateElement().get(Keys.CUSTOM_NAME)
				.orElse(this.getOrCreateElement().type().asComponent()));

		if(this.item.get(Keys.DISPLAY_NAME).isPresent()) {
			builder.hoverEvent(HoverEvent.showText(this.getOrCreateElement().type().asComponent()));
		}

		return actual.append(builder.build()).build();
	}

	@Override
	public TextComponent getDescription() {
		TextComponent.Builder builder = Component.text();
		if(this.item.quantity() > 1) {
			builder.append(Component.text(this.item.quantity() + "x "));
		}

		return builder.append(this.getName()).build();
	}

	@Override
	public Display<ItemStack> getDisplay(UUID viewer) {
		if(this.display == null) {
			ItemStack.Builder designer = ItemStack.builder();
			designer.fromSnapshot(this.getOrCreateElement());

			if (this.getOrCreateElement().get(Keys.CUSTOM_NAME).isPresent()) {
				Component name = this.getOrCreateElement().get(Keys.CUSTOM_NAME).get();
				if (name.color() == null) {
					designer.add(Keys.CUSTOM_NAME, name.color(NamedTextColor.DARK_AQUA));
				}
			} else {
				designer.add(Keys.CUSTOM_NAME, this.getOrCreateElement().type().asComponent().color(NamedTextColor.DARK_AQUA));
			}

			this.display = new SpongeDisplay(designer.build());
		}

		return this.display;
	}

	@Override
	public boolean give(UUID receiver) {
		Optional<ServerPlayer> player = Sponge.server().player(receiver);
		if(player.isPresent()) {
			PrimaryPlayerInventory inventory = player.get().inventory().primary();

			Inventory transformed = inventory.hotbar().union(inventory.storage());

			if(transformed.canFit(this.getOrCreateElement().createStack())) {
				return transformed.offer(this.getOrCreateElement().createStack())
						.type()
						.equals(InventoryTransactionResult.Type.SUCCESS);
			}
		}

		return false;
	}

	@Override
	public boolean take(UUID depositor) {
		AtomicBoolean result = new AtomicBoolean(false);
		Optional<ServerPlayer> player = Sponge.server().player(depositor);
		player.ifPresent(pl -> {
			ItemStack rep = this.item.createStack();
			PrimaryPlayerInventory parent = pl.inventory().primary();
			Inventory transform = parent.hotbar().union(parent.storage());

			Slot slot = this.query(transform);
			if(slot.peek() != ItemStack.empty()) {
				if(!GTSPlugin.instance().configuration().main().get(ConfigKeys.ITEMS_ALLOW_ANVIL_NAMES)) {
					CompoundNBT nbt = NBTTranslator.getInstance().translate(slot.peek().toContainer());
					if (nbt.contains("UnsafeData")) {
						if (nbt.getCompound("UnsafeData").contains("GTS-Anvil")) {
							return;
						}
					}
				}

				slot.poll(rep.quantity());
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
		MessageService parser = Impactor.getInstance().getRegistry().get(MessageService.class);

		return parser.parse(
				Utilities.readMessageConfigOption(MsgConfigKeys.ITEM_DISCORD_DETAILS),
				PlaceholderSources.builder().append(ItemStackSnapshot.class, this::getOrCreateElement).build()
		).stream()
				.map(LegacyComponentSerializer.legacyAmpersand()::serialize)
				.map(out -> out.replace("&", ""))
				.collect(Collectors.toList());
	}

	@Override
	public int getVersion() {
		return 3;
	}

	@Override
	public JObject serialize() {
		return new JObject()
				.add("version", this.getVersion())
				.add("item", writer.apply(this.item));
	}

	private Slot query(Inventory inventory) {
		Iterable<Slot> slots = inventory.slots();
		for(Slot slot : slots) {
			boolean valid = slot.get(Keys.SLOT_INDEX)
					.filter(value -> value.equals(this.slot))
					.isPresent();
			if(valid) {
				return slot;
			}
		}

		return null;
	}
}
