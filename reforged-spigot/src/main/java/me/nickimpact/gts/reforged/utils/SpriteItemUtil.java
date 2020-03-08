package me.nickimpact.gts.reforged.utils;

import com.google.common.collect.Maps;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.EnumSpecialTexture;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.EnumGreninja;
import com.pixelmonmod.pixelmon.enums.forms.EnumNoForm;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import me.nickimpact.gts.reforged.ReforgedBridge;
import me.nickimpact.gts.reforged.config.ReforgedMsgConfigKeys;
import me.nickimpact.gts.reforged.entry.EnumHidableDetail;
import me.nickimpact.gts.reforged.entry.KeyDetailHolder;
import me.nickimpact.gts.spigot.SpigotGTSPlugin;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class SpriteItemUtil {

	public static ItemStack createPicture(Pokemon pokemon) {
		net.minecraft.item.ItemStack nativeItem = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
		NBTTagCompound nbt = new NBTTagCompound();
		String idValue = String.format("%03d", pokemon.getBaseStats().nationalPokedexNumber);
		if (pokemon.isEgg()){
			switch(pokemon.getSpecies()) {
				case Manaphy:
				case Togepi:
					nbt.setString(NbtKeys.SPRITE_NAME, String.format("pixelmon:sprites/eggs/%s%d", pokemon.getSpecies().name.toLowerCase(), pokemon.getEggCycles() > 10 ? 1 : pokemon.getEggCycles() > 5 ? 2 : 3));
					break;
				default:
					nbt.setString(NbtKeys.SPRITE_NAME, String.format("pixelmon:sprites/eggs/egg%d", pokemon.getEggCycles() > 10 ? 1 : pokemon.getEggCycles() > 5 ? 2 : 3));
					break;
			}
		} else {
			if (pokemon.isShiny()) {
				nbt.setString(NbtKeys.SPRITE_NAME,
						"pixelmon:sprites/shinypokemon/" + idValue + getSpriteExtraProperly(pokemon.getSpecies(), pokemon.getFormEnum(), pokemon.getGender(), pokemon.getSpecialTexture()));
			} else {
				nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/pokemon/" + idValue + getSpriteExtraProperly(pokemon.getSpecies(), pokemon.getFormEnum(), pokemon.getGender(), pokemon.getSpecialTexture()));
			}
		}
		nativeItem.setTagCompound(nbt);
		return CraftItemStack.asBukkitCopy((net.minecraft.server.v1_12_R1.ItemStack) (Object) nativeItem);
	}

	public static List<String> getDetails(Pokemon pokemon) {
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("pokemon", pokemon);

		List<String> lore = ((SpigotGTSPlugin) PluginInstance.getInstance()).getTokenService().process(
				ReforgedBridge.getInstance().getMsgConfig(), ReforgedMsgConfigKeys.POKEMON_PREVIEW_LORE, null, null, variables
		);

		if(addLore(pokemon, lore)) {
			lore.add("");
		}

		return lore;
	}

	private static boolean addLore(Pokemon pokemon, List<String> template) {
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("pokemon", pokemon);

		boolean any = false;
		for (EnumHidableDetail detail : EnumHidableDetail.values()) {
			if (detail.getCondition().test(pokemon)) {
				KeyDetailHolder d = detail.getField().apply(pokemon);
				template.addAll(((SpigotGTSPlugin) PluginInstance.getInstance()).getTokenService().process(ReforgedBridge.getInstance().getMsgConfig(), d.getKey(), null, d.getTokens(), variables));
				any = true;
			}
		}

		return any;
	}

	public static double calcEVPercent(EVStore evs) {
		return (evs.hp + evs.attack + evs.defence + evs.specialAttack + evs.specialDefence + evs.speed) / 510.0 * 100;
	}

	public static int getIV(IVStore ivs, StatsType type) {
		return ivs.isHyperTrained(type) ? 31 : ivs.get(type);
	}

	public static double calcIVPercent(IVStore ivs) {
		return (getIV(ivs, StatsType.HP) + getIV(ivs, StatsType.Attack) + getIV(ivs, StatsType.Defence) + getIV(ivs, StatsType.SpecialAttack) + getIV(ivs, StatsType.SpecialDefence) + getIV(ivs, StatsType.Speed)) / 186.0 * 100;
	}

	public static String formattedAbility(Pokemon pokemon) {
		boolean first = false;
		StringBuilder ability = new StringBuilder();
		String initial = pokemon.getAbilityName();
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

	public static String formattedStepsRemainingOnEgg(Pokemon pokemon) {
		int total = (pokemon.getBaseStats().eggCycles + 1) * PixelmonConfig.stepsPerEggCycle;
		int walked = pokemon.getEggSteps() + ((pokemon.getBaseStats().eggCycles - pokemon.getEggCycles()) * PixelmonConfig.stepsPerEggCycle);
		return String.format("%d/%d", walked, total);
	}

	public static String moveset(Pokemon pokemon) {
		Moveset moves = pokemon.getMoveset();
		StringBuilder out = new StringBuilder();
		for(Attack attack : moves.attacks) {
			if(attack == null) continue;
			out.append(attack.getActualMove().getLocalizedName()).append(" - ");
		}

		return out.substring(0, out.length() - 3);
	}

	private static String getSpriteExtraProperly(EnumSpecies species, IEnumForm form, Gender gender, EnumSpecialTexture specialTexture) {
		if (species == EnumSpecies.Greninja && (form == EnumGreninja.BASE || form == EnumGreninja.BATTLE_BOND) && specialTexture.id > 0 && species.hasSpecialTexture()) {
			return "-special";
		}

		if(form != EnumNoForm.NoForm) {
			return species.getFormEnum(form.getForm()).getSpriteSuffix();
		}

		if(EnumSpecies.mfSprite.contains(species)) {
			return "-" + gender.name().toLowerCase();
		}

		if(specialTexture.id > 0 && species.hasSpecialTexture()) {
			return "-special";
		}

		return "";
	}
}
