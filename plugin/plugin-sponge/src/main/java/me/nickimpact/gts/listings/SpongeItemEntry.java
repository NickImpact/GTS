package me.nickimpact.gts.listings;

import com.nickimpact.impactor.api.json.factory.JObject;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.data.registry.GTSKeyMarker;
import me.nickimpact.gts.api.listings.makeup.Display;
import me.nickimpact.gts.sponge.listings.makeup.SpongeEntry;
import me.nickimpact.gts.util.DataViewJsonManager;
import net.kyori.text.TextComponent;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

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
	public Display<ItemStack> getDisplay(UUID viewer, Listing listing) {
		return null;
	}

//	@Override
//	public SpongeDisplay getDisplay(UUID source, SpongeListing listing) {
//		final Config messages = GTSPlugin.getInstance().getMsgConfig();
//		final SpongeMessageService service = (SpongeMessageService) GTSService.getInstance().getServiceManager().get(MessageService.class).get();
//		ItemStack representation = ItemStack.builder()
//				.fromSnapshot(this.item)
//				.build();
//
//		List<String> lore = Lists.newArrayList();
//		if(this.getOrCreateElement().get(Keys.DISPLAY_NAME).isPresent()) {
//			Pattern pattern = Pattern.compile("[&][a-fk-or0-9]");
//			Matcher matcher = pattern.matcher(TextSerializers.FORMATTING_CODE.serialize(this.getOrCreateElement().get(Keys.DISPLAY_NAME).get()));
//			if(!matcher.find()) {
//				representation.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, this.getOrCreateElement().getTranslation().get(source.getSource().getLocale())));
//				lore.add(GTSPlugin.getInstance().getMsgConfig().get(MsgConfigKeys.UI_LISTINGS_ITEMS_ANVIL_RENAME_PREPEND) + this.getOrCreateElement().get(Keys.DISPLAY_NAME).get().toPlain());
//				lore.add("");
//			} else {
//				representation.offer(Keys.DISPLAY_NAME, service.getForSource(messages.get(MsgConfigKeys.ITEM_ENTRY_BASE_TITLE), source.getSource(),null, variables));
//			}
//		} else {
//			representation.offer(Keys.DISPLAY_NAME, service.getForSource(messages.get(MsgConfigKeys.ITEM_ENTRY_BASE_TITLE), source.getSource(),null, variables));
//		}
//
//		this.getOrCreateElement().get(Keys.ITEM_LORE).ifPresent(l -> {
//			if(l.size() > 0) {
//				lore.add(messages.get(MsgConfigKeys.UI_LISTINGS_ITEMS_LORE_DESCRIPTOR));
//				lore.addAll(l.stream().map(TextSerializers.FORMATTING_CODE::serialize).collect(Collectors.toList()));
//				lore.add("");
//			}
//		});
//		lore.addAll(GTSPlugin.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_BASE_LORE));
//		lore.addAll(messages.get(MsgConfigKeys.ENTRY_INFO));
//
//		representation.offer(Keys.ITEM_LORE, service.getTextListForSource(lore, source.getSource(), null, variables));
//
//		return new SpongeDisplay(representation);
//	}

	@Override
	public boolean give(UUID receiver) {
		Optional<Player> player = Sponge.getServer().getPlayer(receiver);
		if(player.isPresent()) {
			if(player.get().getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class)).size() == 36) {

			}
		}

		return false;
	}

	@Override
	public boolean take(UUID depositor) {
		Optional<Player> player = Sponge.getServer().getPlayer(depositor);
		player.ifPresent(pl -> {
			ItemStack rep = this.item.createStack();
			Slot slot = pl.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class))
					.query(QueryOperationTypes.ITEM_STACK_EXACT.of(rep))
					.first();
			if(slot.peek().isPresent()) {


				slot.poll();
			}
		});

		return false;
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
