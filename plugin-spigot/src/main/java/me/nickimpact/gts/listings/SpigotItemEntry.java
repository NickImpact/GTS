package me.nickimpact.gts.listings;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.json.JsonTyping;
import com.nickimpact.impactor.api.utilities.Time;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.enums.CommandResults;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.spigot.SpigotEntry;
import me.nickimpact.gts.spigot.SpigotListing;
import me.nickimpact.gts.spigot.MessageUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@JsonTyping("item")
public class SpigotItemEntry extends SpigotEntry<Map<String, Object>, ItemStack> {

	private transient ItemStack itemStack;

	private transient boolean messageSent;

	public SpigotItemEntry(ItemStack element) {
		super(element.serialize());
		GTS.getInstance().getPluginLogger().debug(GTS.getInstance().getGson().toJson(element.serialize().toString()));
		this.itemStack = element;
	}

	@Override
	public Entry setEntry(Map<String, Object> backing) {
		this.element = backing;
		return this;
	}

	@Override
	public ItemStack getEntry() {
		return itemStack != null ? itemStack : (itemStack = ItemStack.deserialize(this.element));
	}

	@Override
	public String getSpecsTemplate() {
		return "Testing";
	}

	@Override
	public String getName() {
		return this.getEntry().getItemMeta().hasDisplayName() ? itemStack.getItemMeta().getDisplayName() : this.materialToName(itemStack.getType().name());
	}

	@Override
	public List<String> getDetails() {
		ItemMeta meta = this.getEntry().getItemMeta();
		List<String> output = Lists.newArrayList();

		if(meta.hasDisplayName()) {
			output.add("Display Name: " + meta.getDisplayName());
		}

		if(meta.hasLore()) {
			output.add("Lore:");
			output.addAll(meta.getLore());
		}

		if(meta.hasEnchants()) {
			output.add("Enchantments:");
			output.addAll(meta.getEnchants().entrySet().stream().map(entry -> entry.getKey().getName() + " - " + entry.getValue()).collect(Collectors.toList()));
		}

		return output;
	}

	@Override
	public ItemStack baseItemStack(Player player, Listing listing) {
		ItemStack stack = new ItemStack(this.getEntry().getType(), this.getEntry().getAmount(), this.getEntry().getDurability());
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_AQUA + this.materialToName(itemStack.getType().name()));

		List<String> lore = Lists.newArrayList(
				"&7Seller: &e" + Bukkit.getServer().getOfflinePlayer(listing.getOwnerUUID()).getName(),
				"",
				"&7Price: &e" + listing.getPrice().getText()
		).stream().map(str -> ChatColor.translateAlternateColorCodes('&', str)).collect(Collectors.toList());
		if(!listing.getExpiration().equals(LocalDateTime.MAX)) {
			lore.add(ChatColor.translateAlternateColorCodes('&', "&7Time Remaining: &e" + new Time(Duration.between(LocalDateTime.now(), listing.getExpiration()).getSeconds()).toString()));
		}
		meta.setLore(lore);
		stack.setItemMeta(meta);

		return stack;
	}

	@Override
	public boolean supportsOffline() {
		return false;
	}

	@Override
	public boolean giveEntry(OfflinePlayer user) {
		Player player = user.getPlayer(); // Always present as this doesn't support offline users
		Inventory inv = player.getInventory();
		if(Arrays.stream(inv.getContents()).filter(i -> i != null && i.getType() != Material.AIR).count() >= 36) {
			if(!messageSent) {
				Optional.ofNullable(user.getPlayer()).ifPresent(p -> p.sendMessage(MessageUtils.parse("Your inventory is full, so your item couldn't be returned...", true)));
				messageSent = true;
			}
			return false;
		}
		inv.addItem(this.getEntry());
		return true;
	}

	@Override
	public boolean doTakeAway(Player player) {
		Inventory inv = player.getInventory();
		Optional<ItemStack> item = Arrays.stream(inv.getContents()).filter(i -> i != null && i.isSimilar(this.getEntry())).findAny();
		item.ifPresent(inv::remove);
		return true;
	}

	private String materialToName(String input) {
		String[] split = input.split("_");
		final char[] delimiters = {' ', '_'};
		if(split[0].equalsIgnoreCase("PIXELMON")) {
			StringBuilder sb = new StringBuilder(split[1]);
			for(int i = 2; i < split.length; i++) {
				sb.append(" ").append(split[i]);
			}

			if(sb.toString().equals("PC")) {
				return "PC";
			}

			return WordUtils.capitalizeFully(sb.toString(), delimiters).replace("Poke", "Pok\u00e9");
		}

		return WordUtils.capitalizeFully(input, delimiters);
	}

	public static CommandResults cmdExecutor(CommandSender src, List<String> args, boolean permanent) {
		if(args.size() < 2) {
			src.sendMessage(MessageUtils.parse("Not enough arguments...", true));
			return CommandResults.FAILED;
		}

		int amount;
		double price;

		try {
			amount = Integer.parseInt(args.get(0));
			price = Double.parseDouble(args.get(1));
		} catch (Exception e) {
			src.sendMessage(MessageUtils.parse("Invalid arguments supplied, check usage below...", true));
			src.sendMessage(MessageUtils.parse("/gts sell items <amount | ex: 2> <price | ex: 1000, 100.50>", true));
			return CommandResults.FAILED;
		}

		if(price <= 0) {
			src.sendMessage(MessageUtils.parse("Invalid price supplied, price must be positive...", true));
			return CommandResults.FAILED;
		}


		if(src instanceof Player) {
			if(!src.hasPermission("gts.command.sell.items.base")) {
				src.sendMessage(MessageUtils.parse("Unfortunately, you don't have permission to sell items...", true));
				return CommandResults.FAILED;
			}

			Player player = (Player) src;
			Optional<ItemStack> hand = Optional.ofNullable(player.getInventory().getItemInMainHand());
			if(!hand.isPresent()) {
				player.sendMessage(MessageUtils.parse("You have no item in your hand to sell...", true));
				return CommandResults.FAILED;
			}

			if(GTS.getInstance().getConfiguration().get(ConfigKeys.BLACKLISTED_ITEMS).contains(hand.get().getType().name().toLowerCase())) {
				if(!src.hasPermission("gts.command.sell.items.bypass")) {
					player.sendMessage(MessageUtils.parse("That item is blacklisted from being sold...", true));
					return CommandResults.FAILED;
				}
			}

			if(amount < 1 || amount > hand.get().getAmount()) {
				amount = amount < 1 ? 1 : hand.get().getAmount();
			}

			ItemStack item = new ItemStack(hand.get().getType(), amount);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(hand.get().getItemMeta().getDisplayName());
			meta.setLore(hand.get().getItemMeta().getLore());
			hand.get().getItemMeta().getEnchants().forEach((e, l) -> meta.addEnchant(e, l, true));
			meta.setUnbreakable(hand.get().getItemMeta().isUnbreakable());
			hand.get().getItemMeta().getItemFlags().forEach(meta::addItemFlags);
			item.setItemMeta(meta);

			SpigotListing listing = SpigotListing.builder()
					.entry(new SpigotItemEntry(item))
					.id(UUID.randomUUID())
					.owner(player.getUniqueId())
					.expiration(permanent ? LocalDateTime.MAX : LocalDateTime.now().plusDays(1))
					.price(price)
					.build();
			listing.publish(GTS.getInstance(), player.getUniqueId());
		} else {
			src.sendMessage(MessageUtils.parse("You must be a player to use this command...", true));
			return CommandResults.FAILED;
		}

		return CommandResults.SUCCESSFUL;
	}
}
