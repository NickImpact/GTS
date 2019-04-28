package me.nickimpact.gts.listings;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.json.JsonTyping;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.spigot.SpigotEntry;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

@JsonTyping("item")
public class SpigotItemEntry extends SpigotEntry<Map<String, Object>, ItemStack> {

	private transient ItemStack itemStack;

	public SpigotItemEntry(ItemStack element) {
		super(element.serialize());
		GTS.getInstance().getPluginLogger().debug(GTS.getInstance().getGson().toJson(element.serialize().toString()));
		this.itemStack = element;
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
		return Lists.newArrayList();
	}

	@Override
	public ItemStack baseItemStack(Player player, Listing listing) {
		ItemStack stack = new ItemStack(this.getEntry().getType(), this.getEntry().getAmount(), this.getEntry().getDurability());
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_AQUA + this.materialToName(itemStack.getType().name()));
		stack.setItemMeta(meta);

		return stack;
	}

	@Override
	public ItemStack confirmItemStack(Player player, Listing listing) {
		return this.getEntry();
	}

	@Override
	public boolean supportsOffline() {
		return false;
	}

	@Override
	public boolean giveEntry(OfflinePlayer user) {
		return false;
	}

	@Override
	public boolean doTakeAway(Player player) {
		return true;
	}

	private String materialToName(String input) {
		String[] split = input.split("_");
		final char[] delimiters = { ' ', '_' };
		if(split[0].equalsIgnoreCase("PIXELMON")) {
			StringBuilder sb = new StringBuilder(split[1]);
			for(int i = 2; i < split.length; i++) {
				sb.append(" ").append(split[i]);
			}

			return WordUtils.capitalizeFully(sb.toString(), delimiters);
		}

		return WordUtils.capitalizeFully(input, delimiters);
	}
}
