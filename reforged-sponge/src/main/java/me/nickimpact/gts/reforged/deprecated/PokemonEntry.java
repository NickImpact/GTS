package me.nickimpact.gts.reforged.deprecated;

import com.nickimpact.impactor.api.json.JsonTyping;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import me.nickimpact.gts.api.deprecated.Entry;
import me.nickimpact.gts.api.plugin.PluginInstance;
import me.nickimpact.gts.reforged.utils.GsonUtils;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;

@Deprecated
@JsonTyping("reforged")
public class PokemonEntry extends Entry<String, Pokemon> {

	public PokemonEntry() {}

	@Override
	public Pokemon getEntry() {
		try {
			return Pixelmon.pokemonFactory.create(JsonToNBT.getTagFromJson(this.element));
		} catch (NBTException e) {
			return Pixelmon.pokemonFactory.create(GsonUtils.deserialize(this.element));
		}
	}
}
