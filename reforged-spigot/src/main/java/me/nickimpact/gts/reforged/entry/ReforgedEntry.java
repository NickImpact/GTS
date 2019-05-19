package me.nickimpact.gts.reforged.entry;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.utilities.Time;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.comm.EnumUpdateType;
import com.pixelmonmod.pixelmon.entities.pixelmon.EnumSpecialTexture;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.EnumNoForm;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.reforged.ReforgedBridge;
import me.nickimpact.gts.reforged.config.ReforgedKeys;
import me.nickimpact.gts.reforged.utils.Flags;
import me.nickimpact.gts.reforged.utils.SpriteItemUtil;
import me.nickimpact.gts.spigot.MessageUtils;
import me.nickimpact.gts.spigot.SpigotEntry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReforgedEntry extends SpigotEntry<String, Pokemon> {

	private transient Pokemon pokemon;

	private static final Function<Pokemon, String> TO_BASE_64 = poke -> {
		ByteBuf buffer = Unpooled.buffer();
		poke.writeToByteBuffer(buffer, EnumUpdateType.ALL);
		return Base64.getEncoder().encodeToString(buffer.array());
	};

	private static final Function<String, Pokemon> FROM_BASE_64 = base64 -> {
		byte[] bytes = Base64.getDecoder().decode(base64);
		ByteBuf buffer = Unpooled.copiedBuffer(bytes);
		Pokemon out = Pixelmon.pokemonFactory.create(EnumSpecies.Bidoof);
		out.readFromByteBuffer(buffer, EnumUpdateType.ALL);
		return out;
	};

	public ReforgedEntry(Pokemon element) {
		super(TO_BASE_64.apply(element));
		this.pokemon = element;
	}

	@Override
	public Pokemon getEntry() {
		return pokemon != null ? pokemon : (pokemon = FROM_BASE_64.apply(this.element));
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
				ivs.hp,
				ivs.attack,
				ivs.defence,
				ivs.specialAttack,
				ivs.specialDefence,
				ivs.speed,
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
		lore.addAll(Lists.newArrayList(
				"&7Price: &e" + listing.getPrice().getText(),
				"&7Time Remaining: &e" + new Time(Duration.between(LocalDateTime.now(), listing.getExpiration()).getSeconds()).toString()
		));
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
		storage.add(this.getEntry());
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
			player.sendMessage(MessageUtils.parse("That pokemon is marked as untradable...", true));
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
}
