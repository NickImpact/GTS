package me.nickimpact.gts.generations.utils;

import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.ComputerBox;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class PcUtils {

	public static List<NBTTagCompound> getPokemonInBox(ComputerBox[] pc){
		List<NBTTagCompound> pcContents = new ArrayList<>();
		for(ComputerBox b : pc){
			for(int i = 0; i < 30; i++){
				if(b.getNBTByPosition(i) != null){
					pcContents.add(b.getNBTByPosition(i));
				}
			}
		}
		return pcContents;
	}

	public static EntityPixelmon selectSlot(Player player, List<NBTTagCompound> nbt, int pos){
		int slot = pos % 30;

		return (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt.get(slot), (World)player.getWorld());
	}
}
