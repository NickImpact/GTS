package com.nickimpact.gts.entries.pixelmon;

import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.utils.GsonUtils;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class Pokemon {

	private transient EntityPixelmon pokemon;

	private transient NBTTagCompound nbt;

	private String nbtJSON;

	public Pokemon(EntityPixelmon pokemon) {
		this.pokemon = pokemon;
		NBTTagCompound nbt = new NBTTagCompound();
		this.nbt = pokemon.writeToNBT(nbt);
		nbtJSON = GsonUtils.serialize(this.nbt);
	}

	public EntityPixelmon getPokemon() {
		if(this.pokemon == null) {
			this.pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(
					decode(), (World) Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorldName()).get()
			);
		}

		return this.pokemon;
	}

	private NBTTagCompound decode() {
		return nbt != null ? nbt : (nbt = GsonUtils.deserialize(nbtJSON));
	}
}
