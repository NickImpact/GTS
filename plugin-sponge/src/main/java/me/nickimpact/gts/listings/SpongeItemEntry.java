package me.nickimpact.gts.listings;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.json.JsonTyping;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.enums.CommandResults;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.sponge.SpongeEntry;
import me.nickimpact.gts.sponge.SpongeListing;
import me.nickimpact.gts.sponge.TextParsingUtils;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.InventoryTransformation;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@JsonTyping("item")
public class SpongeItemEntry extends SpongeEntry<DataContainer, ItemStack> {

	private transient ItemStack item;
	private transient boolean messageSent;

	private String name;

	public SpongeItemEntry() {}

	@Override
	public Entry setEntry(DataContainer backing) {
		this.element = backing;
		return this;
	}

	public SpongeItemEntry(ItemStack element) {
		super(element.toContainer());
		this.item = element;
		if(GTS.getInstance().getConfig().get(ConfigKeys.CUSTOM_NAME_ALLOWED)) {
			this.name = element.get(Keys.DISPLAY_NAME).map(TextSerializers.FORMATTING_CODE::serialize).map(TextSerializers.FORMATTING_CODE::stripCodes).orElse(element.getTranslation().get());
		} else {
			this.name = element.getTranslation().get();
		}
	}

	@Override
	public ItemStack getEntry() {
		return item != null ? item : ItemStack.builder().fromContainer(this.element).build();
	}

	@Override
	public String getSpecsTemplate() {
		return GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_SPEC_TEMPLATE);
	}

	@Override
	public String getName() {
		if(name == null) {
			ItemStack item = this.getEntry();
			return item.get(Keys.DISPLAY_NAME).orElse(Text.of(item.getTranslation().get())).toPlain();
		} else {
			return name;
		}
	}

	@Override
	public List<String> getDetails() {
		List<String> output = Lists.newArrayList();
		ItemStack item = this.getEntry();
		if(item.get(Keys.DISPLAY_NAME).isPresent()) {
			output.add("Display Name: " + item.get(Keys.DISPLAY_NAME).get().toPlain());
		}

		if(item.get(Keys.ITEM_LORE).isPresent()) {
			output.add("Lore:");
			output.addAll(item.get(Keys.ITEM_LORE).get().stream().map(Text::toPlain).collect(Collectors.toList()));
		}

		if(item.get(Keys.ITEM_ENCHANTMENTS).isPresent()) {
			output.add("Enchantments:");
			output.addAll(item.get(Keys.ITEM_ENCHANTMENTS).get().stream().map(e -> e.getType().getName() + " - " + e.getLevel()).collect(Collectors.toList()));
		}

		return output;
	}

	@Override
	public ItemStack baseItemStack(Player player, Listing listing) {
		ItemStack icon = ItemStack.builder()
				.fromItemStack(this.getEntry())
				.add(Keys.ITEM_ENCHANTMENTS, this.getEntry().get(Keys.ITEM_ENCHANTMENTS).orElse(Lists.newArrayList()))
				.build();

		List<String> lore = Lists.newArrayList();

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("listing", listing);
		variables.put("item", this.getEntry());

		if(this.getEntry().get(Keys.DISPLAY_NAME).isPresent()) {
			Pattern pattern = Pattern.compile("[&][a-fk-or0-9]");
			Matcher matcher = pattern.matcher(TextSerializers.FORMATTING_CODE.serialize(this.getEntry().get(Keys.DISPLAY_NAME).get()));
			if(!matcher.find()) {
				icon.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_AQUA, this.getEntry().getTranslation().get(player.getLocale())));
				lore.add("&7Item Name: &e" + this.getEntry().get(Keys.DISPLAY_NAME).get().toPlain());
				lore.add("");
			} else {
				icon.offer(Keys.DISPLAY_NAME, GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(player, GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_BASE_TITLE), null, variables));
			}
		} else {
			icon.offer(Keys.DISPLAY_NAME, GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(player, GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_BASE_TITLE), null, variables));
		}

		lore.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ITEM_ENTRY_BASE_LORE));
		this.getEntry().get(Keys.ITEM_LORE).ifPresent(l -> {
			if(l.size() > 0) {
				lore.add("");
				lore.add("&aItem Lore: ");
				lore.addAll(l.stream().map(TextSerializers.FORMATTING_CODE::serialize).collect(Collectors.toList()));
			}
		});
		lore.addAll(GTS.getInstance().getMsgConfig().get(MsgConfigKeys.ENTRY_INFO));

		icon.offer(Keys.ITEM_LORE, GTS.getInstance().getTextParsingUtils().parse(lore, player, null, variables));

		return icon;
	}

	@Override
	public boolean supportsOffline() {
		return false;
	}

	@Override
	public boolean giveEntry(User user) {
		Config config = GTS.getInstance().getMsgConfig();
		TextParsingUtils parser = GTS.getInstance().getTextParsingUtils();

		// User will always be a player here due to the offline support check
		Player player = user.getPlayer().get();
		if(player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class)).size() == 36) {
			if(!messageSent) {
				player.sendMessage(parser.fetchAndParseMsg(player, config, MsgConfigKeys.ITEMS_INVENTORY_FULL, null, null));
				messageSent = true;
			}
			return false;
		}

		player.getInventory().transform(InventoryTransformation.of(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class), QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class))).offer(this.getEntry());
		return true;
	}

	@Override
	public boolean doTakeAway(Player player) {
		Config config = GTS.getInstance().getMsgConfig();
		TextParsingUtils parser = GTS.getInstance().getTextParsingUtils();

		Optional<ItemStack> item = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class)).query(QueryOperationTypes.ITEM_STACK_IGNORE_QUANTITY.of(this.getEntry())).peek(this.getEntry().getQuantity());
		if(item.isPresent()) {
			if(!GTS.getInstance().getConfig().get(ConfigKeys.CUSTOM_NAME_ALLOWED)) {
				if(item.get().get(Keys.DISPLAY_NAME).isPresent()) {
					player.sendMessage(parser.fetchAndParseMsg(player, config, MsgConfigKeys.ITEMS_NO_CUSTOM_NAMES, null, null));
					return false;
				}
			}

			if(GTS.getInstance().getConfig().get(ConfigKeys.BLACKLISTED_ITEMS).contains(item.get().getType().getId())) {
				player.sendMessage(parser.fetchAndParseMsg(player, config, MsgConfigKeys.BLACKLISTED, null, null));
				return false;
			}

			player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class)).query(QueryOperationTypes.ITEM_STACK_IGNORE_QUANTITY.of(this.getEntry())).poll(this.getEntry().getQuantity());
			return true;
		}
		return false;
	}

	public static CommandResults cmdExecutor(CommandSource src, List<String> args, boolean permanent) {
		if(args.size() < 2) {
			src.sendMessage(GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(src, MsgConfigKeys.INVALID_ARGS, null, null));
			return CommandResults.FAILED;
		}

		int amount;
		double price;

		try {
			amount = Integer.parseInt(args.get(0));
			price = Double.parseDouble(args.get(1));
		} catch (Exception e) {
			src.sendMessage(GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(src, MsgConfigKeys.INVALID_ARGS, null, null));
			return CommandResults.FAILED;
		}

		if(price <= 0) {
			src.sendMessage(GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(src, MsgConfigKeys.PRICE_NOT_POSITIVE, null, null));
			return CommandResults.FAILED;
		}


		if(src instanceof Player) {
			if(!src.hasPermission("gts.command.sell.items.base")) {
				src.sendMessage(GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(src, MsgConfigKeys.NO_PERMISSION, null, null));
				return CommandResults.FAILED;
			}

			Player player = (Player) src;
			Optional<ItemStack> hand = player.getItemInHand(HandTypes.MAIN_HAND);
			if(!hand.isPresent()) {
				src.sendMessage(GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(src, MsgConfigKeys.ITEMS_NONE_IN_HAND, null, null));
				return CommandResults.FAILED;
			}

			if(GTS.getInstance().getConfiguration().get(ConfigKeys.BLACKLISTED_ITEMS).contains(hand.get().getType().getName().toLowerCase())) {
				if(!src.hasPermission("gts.command.sell.items.bypass")) {
					src.sendMessage(GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(src, MsgConfigKeys.BLACKLISTED, null, null));
					return CommandResults.FAILED;
				}
			}

			if(amount < 1 || amount > hand.get().getQuantity()) {
				amount = amount < 1 ? 1 : hand.get().getQuantity();
			}

			ItemStack item = ItemStack.builder().from(hand.get()).quantity(amount).build();

			SpongeListing listing = SpongeListing.builder()
					.entry(new SpongeItemEntry(item))
					.id(UUID.randomUUID())
					.owner(player.getUniqueId())
					.expiration(permanent ? LocalDateTime.MAX : LocalDateTime.now().plusSeconds(GTS.getInstance().getConfiguration().get(ConfigKeys.LISTING_TIME)))
					.price(price)
					.build();
			listing.publish(GTS.getInstance(), player.getUniqueId());
		} else {
			src.sendMessage(GTS.getInstance().getTextParsingUtils().fetchAndParseMsg(src, MsgConfigKeys.NOT_PLAYER, null, null));
			return CommandResults.FAILED;
		}

		return CommandResults.SUCCESSFUL;
	}
}
