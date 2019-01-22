package me.nickimpact.gts.entries.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.json.Typing;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.configuration.ConfigKeys;
import me.nickimpact.gts.configuration.MsgConfigKeys;
import me.nickimpact.gts.entries.prices.MoneyPrice;
import me.nickimpact.gts.internal.TextParsingUtils;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.InventoryTransformation;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represents an ItemStack that can be added as an element into the GTS market. The generified typing
 * here represents the lower-level phase of the ItemStack, as an ItemStack in a generic setup
 * like such produces a MalformedParameterizedTypeException, prevention serialization. Luckily
 * for us, Sponge provides a way to go from this low-level back up, so we can simply store the
 * data, then cache the ItemStack once it is recreated.
 *
 * @author NickImpact
 */
@Typing("Item")
public class ItemEntry extends Entry<DataContainer> {

	/** The cached version of the ItemStack */
	@Getter private transient ItemStack item;

	private String name;

	private static Map<UUID, Integer> amounts = Maps.newHashMap();

	public ItemEntry() {
		super();
	}

	public ItemEntry(ItemStack element, MoneyPrice price) {
		super(element.toContainer(), price);
		this.item = element;
		this.name = element.getTranslation().get();
	}

	private ItemStack decode() {
		return item != null ? item : (item = ItemStack.builder().fromContainer(this.element).build());
	}

	@Override
	public String getSpecsTemplate() {
		return GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_SPEC_TEMPLATE);
	}

	@Override
	public List<String> getLogTemplate() {
		return Lists.newArrayList();
	}

	@Override
	public String getName() {
		if(name == null) {
			ItemStack item = this.decode();
			return item.get(Keys.DISPLAY_NAME).orElse(Text.of(item.getTranslation().get())).toPlain();
		} else {
			return name;
		}
	}

	@Override
	public ItemStack baseItemStack(Player player, Listing listing) {
		ItemStack icon = ItemStack.builder()
				.itemType(this.decode().getType())
				.quantity(this.decode().getQuantity())
				.add(Keys.ITEM_ENCHANTMENTS, this.decode().get(Keys.ITEM_ENCHANTMENTS).orElse(Lists.newArrayList()))
				.build();

		List<String> lore = Lists.newArrayList();

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);
		if(this.item.get(Keys.DISPLAY_NAME).isPresent()) {
			Pattern pattern = Pattern.compile("[&][a-fk-or0-9]");
			Matcher matcher = pattern.matcher(TextSerializers.FORMATTING_CODE.serialize(this.item.get(Keys.DISPLAY_NAME).get()));
			if(!matcher.find()) {
				icon.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, item.getTranslation().get(player.getLocale())));
				lore.add("&7Item Name: " + this.item.get(Keys.DISPLAY_NAME).get().toPlain());
				lore.add("");
			} else {
				icon.offer(Keys.DISPLAY_NAME, TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_BASE_TITLE), player, null, variables));
			}
		}

		lore.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_BASE_LORE));
		this.decode().get(Keys.ITEM_LORE).ifPresent(l -> {
			if(l.size() > 0) {
				lore.add("&aItem Lore: ");
				lore.addAll(l.stream().map(TextSerializers.FORMATTING_CODE::serialize).collect(Collectors.toList()));
			}
		});

		if(listing.getAucData() != null) {
			lore.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.AUCTION_INFO));
		} else {
			lore.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ENTRY_INFO));
		}

		icon.offer(Keys.ITEM_LORE, TextParsingUtils.parse(lore, player, null, variables));

		return icon;
	}

	@Override
	public ItemStack confirmItemStack(Player player, Listing listing) {
		ItemStack icon = ItemStack.builder()
				.itemType(ItemTypes.PAPER)
				.build();

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);
		icon.offer(Keys.DISPLAY_NAME, TextParsingUtils.parse(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_CONFIRM_TITLE), player, null, variables));
		List<String> lore = Lists.newArrayList();
		lore.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_CONFIRM_LORE));
		icon.offer(Keys.ITEM_LORE, TextParsingUtils.parse(lore, player, null, variables));

		return icon;
	}

	@Override
	public boolean supportsOffline() {
		return false;
	}

	@Override
	public boolean giveEntry(User user) {
		// User will always be a player here due to the offline support check
		Player player = (Player)user;
		if(player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class)).size() == 36) {
			player.sendMessage(Text.of("Your inventory is full, so we can't award you this listing..."));
			return false;
		}

		player.getInventory().transform(InventoryTransformation.of(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class), QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class))).offer(this.decode());
		return true;
	}

	@Override
	public boolean doTakeAway(Player player) {
		Optional<ItemStack> item = player.getItemInHand(HandTypes.MAIN_HAND);
		if(item.isPresent()) {
			if(!GTS.getInstance().getConfig().get(ConfigKeys.CUSTOM_NAME_ALLOWED)) {
				if(item.get().get(Keys.DISPLAY_NAME).isPresent()) {
					return false;
				}
			}

			int amount = amounts.get(player.getUniqueId());
			item.get().setQuantity(item.get().getQuantity() - amount);
			player.setItemInHand(HandTypes.MAIN_HAND, item.get());
			return true;
		}
		return false;
	}
}
