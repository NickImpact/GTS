package me.nickimpact.gts.generations.ui;

import com.pixelmongenerations.common.entity.pixelmon.EntityPixelmon;
import com.pixelmongenerations.core.config.PixelmonItems;
import com.pixelmongenerations.core.enums.EnumSpecies;
import com.pixelmongenerations.core.storage.NbtKeys;
import com.pixelmongenerations.core.util.helper.SpriteHelper;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.item.inventory.ItemStack;

public class PixelmonIcons {

	public static ItemStack pokemonDisplay(EntityPixelmon pokemon, int form) {
		return pokemonDisplay(pokemon.getSpecies(), form, pokemon.isEgg, pokemon.isShiny());
	}

	public static ItemStack pokemonDisplay(EnumSpecies species, int form, boolean isEgg, boolean isShiny) {
		net.minecraft.item.ItemStack nativeItem = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
		NBTTagCompound nbt = new NBTTagCompound();
		String idValue = String.format("%03d", species.getNationalPokedexInteger());
		if (isEgg){
			switch(species) {
				case Manaphy:
					nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/manaphy1");
					break;
				case Togepi:
					nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/togepi1");
					break;
				default:
					nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/egg1");
					break;
			}
		} else {
			if (isShiny) {
				nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/shinypokemon/" + idValue + SpriteHelper.getSpriteExtra(
						species.name, form)
				);
			} else {
				nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/pokemon/" + idValue + SpriteHelper.getSpriteExtra(
						species.name, form)
				);
			}
		}
		nativeItem.setTagCompound(nbt);
		return (ItemStack) (Object) (nativeItem);
	}
}
