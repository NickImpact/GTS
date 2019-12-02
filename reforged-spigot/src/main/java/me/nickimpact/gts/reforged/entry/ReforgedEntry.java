package me.nickimpact.gts.reforged.entry;

import co.aikar.commands.CommandIssuer;
import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.configuration.Config;
import com.nickimpact.impactor.api.utilities.Time;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.comm.EnumUpdateType;
import com.pixelmonmod.pixelmon.entities.pixelmon.EnumSpecialTexture;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.EnumNoForm;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.nickimpact.gts.api.enums.CommandResults;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.plugin.PluginInstance;
import me.nickimpact.gts.config.ConfigKeys;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.discord.Message;
import me.nickimpact.gts.reforged.ReforgedBridge;
import me.nickimpact.gts.reforged.config.ReforgedKeys;
import me.nickimpact.gts.reforged.utils.Flags;
import me.nickimpact.gts.reforged.utils.GsonUtils;
import me.nickimpact.gts.reforged.utils.SpriteItemUtil;
import me.nickimpact.gts.spigot.MessageUtils;
import me.nickimpact.gts.spigot.SpigotEntry;
import me.nickimpact.gts.spigot.SpigotListing;
import net.milkbowl.vault.economy.Economy;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReforgedEntry extends SpigotEntry<String, Pokemon> {

	private transient Pokemon pokemon;
	private transient boolean messaged;

	public ReforgedEntry(Pokemon element) {
		super(GsonUtils.serialize(element.writeToNBT(new NBTTagCompound())));
		this.pokemon = element;
	}

	@Override
	public Entry setEntry(String backing) {
		this.element = backing;
		return this;
	}

	@Override
	public Pokemon getEntry() {
		return pokemon != null ? pokemon : (pokemon = Pixelmon.pokemonFactory.create(GsonUtils.deserialize(this.element)));
	}

	@Override
	public String getSpecsTemplate() {
		String out = "";
		if(!this.getEntry().isEgg()) {
			out += String.format("%s %.2f%% IV ", SpriteItemUtil.formattedAbility(pokemon).toLowerCase(), SpriteItemUtil.calcIVPercent(pokemon.getIVs()));
			if(this.pokemon.isShiny()) {
				out += "shiny ";
			}

			if(this.pokemon.getCustomTexture() != null && !this.pokemon.getCustomTexture().isEmpty()) {
				StringBuilder sb = new StringBuilder();
				String[] split = this.pokemon.getCustomTexture().split("\\s+");

				boolean first = true;
				for(String word : split) {
					if(!first) {
						sb.append(" ");
					}
					sb.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
					first = false;
				}

				out += sb.toString() + " ";
			}

			out += pokemon.getSpecies().getLocalizedName();
		} else {
			out = pokemon.getSpecies().getLocalizedName();
		}

		return out;
	}

	@Override
	public String getName() {
		return this.getEntry().getSpecies().getLocalizedName();
	}

	@Override
	public List<String> getDetails() {
		Pokemon pokemon = this.getEntry();
		List<String> output = Lists.newArrayList();

		if(pokemon.getNickname() != null && !pokemon.getNickname().isEmpty()) {
			output.add("- Nickname: " + pokemon.getNickname());
		}

		output.add("- Level: " + pokemon.getLevel());

		if(pokemon.getFormEnum() != EnumNoForm.NoForm) {
			output.add("- Form: " + pokemon.getFormEnum().getLocalizedName());
		}

		output.add("- Traits:");
		if(pokemon.isShiny()) output.add("|  Shiny");
		if(pokemon.isEgg()) {
			output.add("|  Egg");
			output.add("|  - Steps Walked: " + SpriteItemUtil.formattedStepsRemainingOnEgg(pokemon));
		}
		if(pokemon.getPokerus() != null) output.add("|  Infected w/ Pok\u00e9rus");
		if(pokemon.getSpecialTexture() != null && pokemon.getSpecialTexture() != EnumSpecialTexture.None) output.add("|  Special Texture: " + pokemon.getSpecialTexture().name());
		if(pokemon.getCustomTexture() != null && !pokemon.getCustomTexture().isEmpty()) {
			StringBuilder sb = new StringBuilder();
			String[] split = pokemon.getCustomTexture().split("\\s+");

			boolean first = true;
			for(String word : split) {
				if(!first) {
					sb.append(" ");
				}
				sb.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
				first = false;
			}
			output.add("|  Custom Texture: " + sb.toString());
		}

		output.add("");

		output.add("- Ability: " + SpriteItemUtil.formattedAbility(pokemon));
		output.add("- Nature: " + pokemon.getNature().getLocalizedName());
		output.add("- Gender: " + pokemon.getGender().getLocalizedName());
		output.add("- Growth: " + pokemon.getGrowth().getLocalizedName());

		EVStore evs = pokemon.getEVs();
		output.add("- EVs: " + String.format(
				"%d/%d/%d/%d/%d/%d (%.2f%%)",
				evs.hp,
				evs.attack,
				evs.defence,
				evs.specialAttack,
				evs.specialDefence,
				evs.speed,
				SpriteItemUtil.calcEVPercent(evs)
		));

		IVStore ivs = pokemon.getIVs();
		output.add("- IVs: " + String.format(
				"%d/%d/%d/%d/%d/%d (%.2f%%)",
				SpriteItemUtil.getIV(ivs, StatsType.HP),
				SpriteItemUtil.getIV(ivs, StatsType.Attack),
				SpriteItemUtil.getIV(ivs, StatsType.Defence),
				SpriteItemUtil.getIV(ivs, StatsType.SpecialAttack),
				SpriteItemUtil.getIV(ivs, StatsType.SpecialDefence),
				SpriteItemUtil.getIV(ivs, StatsType.Speed),
				SpriteItemUtil.calcIVPercent(ivs)
		));

		output.add("Moveset: " + SpriteItemUtil.moveset(pokemon));

		return output;
	}

	@Override
	public ItemStack baseItemStack(Player player, Listing listing) {
		ItemStack icon = SpriteItemUtil.createPicture(this.getEntry());
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', String.format(
				!this.getEntry().isEgg() ? "&3%s &7| &aLvl %d" : "&3%s Egg",
				this.getEntry().getSpecies().getLocalizedName(),
				this.getEntry().getLevel()))
		);

		List<String> lore = Lists.newArrayList();
		lore.addAll(Lists.newArrayList("&7Seller: &e" + Bukkit.getOfflinePlayer(listing.getOwnerUUID()).getName()));
		lore.addAll(SpriteItemUtil.getDetails(this.getEntry()));
		lore.add("&7Price: &e" + listing.getPrice().getText());
		if(!listing.getExpiration().equals(LocalDateTime.MAX)) {
			lore.add("&7Time Remaining: &e" + new Time(Duration.between(LocalDateTime.now(), listing.getExpiration()).getSeconds()).toString());
		}
		meta.setLore(lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
		icon.setItemMeta(meta);

		return icon;
	}

	@Override
	public boolean supportsOffline() {
		return true;
	}

	@Override
	public boolean giveEntry(OfflinePlayer user) {
		PlayerPartyStorage storage = Pixelmon.storageManager.getParty(user.getUniqueId());
		if(!storage.add(this.getEntry())) {
			if(!messaged) {
				user.getPlayer().sendMessage(MessageUtils.parse("Your pokemon storage is unfortunately full...", true));
				messaged = true;
			}
			return false;
		}
		return true;
	}

	@Override
	public boolean doTakeAway(Player player) {
		PlayerPartyStorage storage = Pixelmon.storageManager.getParty(player.getUniqueId());
		if(BattleRegistry.getBattle(storage.getPlayer()) != null) {
			player.sendMessage(MessageUtils.parse("You can't sell a pokemon while in battle...", true));
			return false;
		}

		if(Flags.UNTRADABLE.matches(this.getEntry())) {
			player.sendMessage(MessageUtils.parse("That pokemon is marked as untradeable...", true));
			return false;
		}

		List<EnumSpecies> blacklisted = ReforgedBridge.getInstance().getConfiguration()
				.get(ReforgedKeys.BLACKLISTED_POKEMON)
				.stream()
				.map(EnumSpecies::getFromNameAnyCase)
				.collect(Collectors.toList());
		if(blacklisted.contains(this.getEntry().getSpecies())) {
			player.sendMessage(MessageUtils.parse("That pokemon has been blacklisted from being added on the GTS...", true));
			return false;
		}

		storage.retrieveAll();
		storage.set(storage.getPosition(this.getEntry()), null);

		return true;
	}

	public enum LakeTrio {
		Mesprit(EnumSpecies.Mesprit),
		Azelf(EnumSpecies.Azelf),
		Uxie(EnumSpecies.Uxie);

		private EnumSpecies species;

		LakeTrio(EnumSpecies species) {
			this.species = species;
		}

		public static boolean isMember(EnumSpecies species) {
			for(LakeTrio guardian : values()) {
				if(guardian.species == species) {
					return true;
				}
			}

			return false;
		}
	}

	public static CommandResults execute(CommandIssuer src, List<String> args, boolean permanent) {
		if(args.size() < 2) {
			src.sendMessage(MessageUtils.parse("Not enough arguments...", true));
			return CommandResults.FAILED;
		}

		if(src.getIssuer() instanceof Player) {
			int slot;
			double price;

			try {
				slot = Integer.parseInt(args.get(0));
				price = Double.parseDouble(args.get(1));
			} catch (Exception e) {
				src.sendMessage(MessageUtils.parse("Invalid arguments supplied, check usage below...", true));
				src.sendMessage(MessageUtils.parse("/gts sell items <slot | ex: 2> <price | ex: 1000, 100.50>", true));
				return CommandResults.FAILED;
			}

			if(slot < 1 || slot > 6) {
				src.sendMessage(MessageUtils.parse("Invalid arguments supplied, must supply a slot between 1-6...", true));
				return CommandResults.FAILED;
			}

			if(price <= 0) {
				src.sendMessage(MessageUtils.parse("Invalid price supplied, price must be positive...", true));
				return CommandResults.FAILED;
			}

			if(price > PluginInstance.getInstance().getConfiguration().get(ConfigKeys.MAX_MONEY_PRICE)) {
				src.sendMessage(MessageUtils.parse("Invalid price supplied, price must not exceed the set max of " + Bukkit.getServicesManager().getRegistration(Economy.class).getProvider().format(PluginInstance.getInstance().getConfiguration().get(ConfigKeys.MAX_MONEY_PRICE)) + "...", true));
				return CommandResults.FAILED;
			}

			if(!src.hasPermission("gts.command.sell.pokemon.base")) {
				src.sendMessage(MessageUtils.parse("Unfortunately, you don't have permission to sell pokemon...", true));
				return CommandResults.FAILED;
			}

			PlayerPartyStorage party = Pixelmon.storageManager.getParty(((Player) src.getIssuer()).getUniqueId());
			Pokemon pokemon = party.get(slot - 1);
			if(pokemon == null) {
				src.sendMessage(MessageUtils.parse("Unfortunately, that slot is empty...", true));
				return CommandResults.FAILED;
			}

			if(!pokemon.isEgg() && party.getTeam().size() <= 1) {
				src.sendMessage(MessageUtils.parse("Unfortunately, you can't sell your last non-egg party memeber...", true));
				return CommandResults.FAILED;
			}

//			if(ReforgedBridge.getInstance().getConfig().get(PokemonConfigKeys.BLACKLISTED_POKEMON).stream().anyMatch(species -> species.toLowerCase().equals(pokemon.getSpecies().getPokemonName().toLowerCase()))) {
//				if(!src.hasPermission("gts.command.sell.pokemon.bypass")) {
//					src.sendMessage(parser.fetchAndParseMsg(src, config, MsgConfigKeys.BLACKLISTED, null, null));
//					return CommandResults.FAILED;
//				}
//			}

			SpigotListing listing = SpigotListing.builder()
					.entry(new ReforgedEntry(pokemon))
					.id(UUID.randomUUID())
					.owner(((Player) src.getIssuer()).getUniqueId())
					.price(price)
					.expiration(permanent ? LocalDateTime.MAX : LocalDateTime.now().plusSeconds(PluginInstance.getInstance().getConfiguration().get(ConfigKeys.LISTING_TIME)))
					.build();
			listing.publish(PluginInstance.getInstance(), ((Player) src.getIssuer()).getUniqueId());

			return CommandResults.SUCCESSFUL;
		}

		src.sendMessage(MessageUtils.parse("Unfortunately, you must be a player to use this command...", true));
		return CommandResults.FAILED;
	}
}
