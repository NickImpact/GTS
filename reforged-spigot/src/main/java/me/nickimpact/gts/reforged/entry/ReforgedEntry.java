package me.nickimpact.gts.reforged.entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.utilities.Time;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.comm.EnumUpdateType;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.EnumNoForm;
import com.pixelmonmod.pixelmon.items.ItemPixelmonSprite;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.config.MsgConfigKeys;
import me.nickimpact.gts.reforged.ReforgedBridge;
import me.nickimpact.gts.reforged.utils.Flags;
import me.nickimpact.gts.spigot.SpigotEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
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
			out += String.format("%s %.2f%% IV ", this.formattedAbility().toLowerCase(), this.calcIVPercent(pokemon.getIVs()));
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
			output.add("|  - Steps Remaining: " + this.formattedStepsRemainingOnEgg(pokemon));
			output.add("|  - Status: " + pokemon.getEggDescription());
		}
		if(pokemon.getPokerus() != null) output.add("|  Infected w/ Pok\u00e9rus");
		if(pokemon.getSpecialTexture() != null) output.add("|  Special Texture: " + pokemon.getSpecialTexture().name());
		if(pokemon.getCustomTexture() != null){
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

		output.add("- Ability: " + this.formattedAbility());
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
				this.calcEVPercent(evs)
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
				this.calcIVPercent(ivs)
		));

		output.add("Moveset: " + this.moveset());

		return output;
	}

	@Override
	public ItemStack baseItemStack(Player player, Listing listing) {
		ItemStack icon = this.getPicture(this.getEntry());
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', String.format(
				!this.getEntry().isEgg() ? "&3%s &7| &aLvl %d" : "&3%s Egg",
				this.getEntry().getSpecies().getLocalizedName(),
				this.getEntry().getLevel()))
		);



		Gender gender = this.getEntry().getGender();
		EVStore evs = this.getEntry().getEVs();
		IVStore ivs = this.getEntry().getIVs();

		List<String> lore = Lists.newArrayList();
		lore.addAll(Lists.newArrayList(
				"&7Seller: &e" + Bukkit.getServer().getOfflinePlayer(listing.getOwnerUUID()).getName(),
				"",
				"&7Ability: &e" + this.formattedAbility(),
				"&7Gender: " + (gender == Gender.Male ? "&bMale" : gender == Gender.Female ? "&dFemale" : "&fNone"),
				"&7Nature: &e" + this.getEntry().getNature(),
				"&7Size: &e" + this.getEntry().getGrowth().name(),
				"&7EVs: " + String.format(
						"&e%d&7/&e%d&7/&e%d&7/&e%d&7/&e%d&7/&e%d &7(&a%.2f%%&7)",
						evs.hp,
						evs.attack,
						evs.defence,
						evs.specialAttack,
						evs.specialDefence,
						evs.speed,
						this.calcEVPercent(evs)
				),
				"&7IVs: " + String.format(
						"&e%d&7/&e%d&7/&e%d&7/&e%d&7/&e%d&7/&e%d &7(&a%.2f%%&7)",
						ivs.hp,
						ivs.attack,
						ivs.defence,
						ivs.specialAttack,
						ivs.specialDefence,
						ivs.speed,
						this.calcIVPercent(ivs)
				),
				""

		));
		if(this.addLore(lore)) {
			lore.add("");
		}
		lore.addAll(Lists.newArrayList(
				"&7Price: &e" + listing.getPrice().getText(),
				"&7Time Remaining: &e" + new Time(Duration.between(LocalDateTime.now(), listing.getExpiration()).getSeconds()).toString()
		));
		meta.setLore(lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
		icon.setItemMeta(meta);

		return icon;
	}

	@Override
	public ItemStack confirmItemStack(Player player, Listing listing) {
		ItemStack paper = new ItemStack(Material.PAPER);
		ItemMeta meta = paper.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&ePurchase " + this.getName() + "?"));
		paper.setItemMeta(meta);

		return paper;
	}

	private boolean addLore(List<String> template) {
		boolean any = false;
		for (EnumHidableDetail detail : EnumHidableDetail.values()) {
			if (detail.getCondition().test(this.getEntry())) {
				template.add(detail.getField().apply(this.getEntry()));
				any = true;
			}
		}

		return any;
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
			// TODO - Message
			return false;
		}

		if(Flags.UNTRADABLE.matches(this.getEntry())) {
			// TODO - Message
			return false;
		}

		// TODO - Check for blacklist

		storage.retrieveAll();
		storage.set(storage.getPosition(this.getEntry()), null);

		return true;
	}

	private ItemStack getPicture(Pokemon pokemon) {
		return CraftItemStack.asBukkitCopy((net.minecraft.server.v1_12_R1.ItemStack) (Object) ItemPixelmonSprite.getPhoto(pokemon));
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

	private double calcEVPercent(EVStore evs) {
		return (evs.hp + evs.attack + evs.defence + evs.specialAttack + evs.specialDefence + evs.speed) / 510.0 * 100;
	}

	private double calcIVPercent(IVStore ivs) {
		return (ivs.hp + ivs.attack + ivs.defence + ivs.specialAttack + ivs.specialDefence + ivs.speed) / 186.0 * 100;
	}

	private String formattedAbility() {
		boolean first = false;
		StringBuilder ability = new StringBuilder();
		String initial = this.getEntry().getAbilityName();
		for(int i = 0; i < initial.length(); i++) {
			char c = initial.charAt(i);
			if(c >= 'A' && c <= 'Z') {
				if (first) {
					ability.append(" ").append(c);
				} else {
					ability.append(c);
					first = true;
				}
			} else {
				ability.append(c);
			}
		}

		return ability.toString();
	}

	private String formattedStepsRemainingOnEgg(Pokemon pokemon) {
		int total = (pokemon.getEggCycles() + 1) * PixelmonConfig.stepsPerEggCycle;
		int walked = pokemon.getEggSteps();
		return String.format("%d/%d", walked, total);
	}

	private String moveset() {
		Moveset moves = pokemon.getMoveset();
		StringBuilder out = new StringBuilder();
		for(Attack attack : moves.attacks) {
			out.append(attack.baseAttack.getTranslatedName().getFormattedText()).append("-");
		}

		return out.substring(0, out.length() - 1);
	}
}
