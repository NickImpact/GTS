package me.nickimpact.gts.pixelmon.ui;

import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.util.helpers.SpriteHelper;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.item.inventory.ItemStack;

public class PixelmonIcons {

	public static ItemStack pokemonDisplay(EntityPixelmon pokemon, int form) {
		net.minecraft.item.ItemStack nativeItem = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
		NBTTagCompound nbt = new NBTTagCompound();
		String idValue = String.format("%03d", pokemon.baseStats.nationalPokedexNumber);
		if (pokemon.isEgg) {
			switch(pokemon.getSpecies()) {
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
			if (pokemon.getIsShiny()) {
				nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/shinypokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon.baseStats.pixelmonName, form));
			} else {
				nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/pokemon/" + idValue + SpriteHelper.getSpriteExtra(pokemon.baseStats.pixelmonName, form));
			}
		}
		nativeItem.setTagCompound(nbt);
		return (ItemStack) (Object) (nativeItem);
	}

	public static ItemStack pokemonDisplay(EnumPokemon species, int form, boolean isEgg, boolean isShiny) {
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
