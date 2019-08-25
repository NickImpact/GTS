package me.nickimpact.gts.generations.deprecated;

import com.nickimpact.impactor.api.json.JsonTyping;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import me.nickimpact.gts.api.deprecated.Entry;
import me.nickimpact.gts.generations.utils.GsonUtils;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;

@Deprecated
@JsonTyping("pokemon")
public class PokemonEntry extends Entry<String, EntityPixelmon> {

	public PokemonEntry() {}

	@Override
	public EntityPixelmon getEntry() {
		return (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(GsonUtils.deserialize(this.element), (World) Sponge.getServer().getWorlds().iterator().next());
	}
}
