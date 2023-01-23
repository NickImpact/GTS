package net.impactdev.gts.ui.icons.deserializers;

import net.impactdev.gts.ui.icons.TemplateProperties;
import net.impactdev.impactor.api.items.ImpactorItemStack;
import net.impactdev.impactor.api.items.extensions.SkullStack;
import net.impactdev.impactor.api.items.types.ItemType;
import net.impactdev.impactor.api.items.types.ItemTypes;
import net.kyori.adventure.key.Key;
import org.spongepowered.configurate.ConfigurationNode;

public final class SkullDeserializer extends AbstractSerializer {

    @Override
    public TemplateProperties resolve(ConfigurationNode node) throws Exception {
        TemplateProperties base = super.resolve(node);

        ConfigurationNode type = node.node("player");
        if(type.getBoolean()) {
            base.type = ItemTypes.PLAYER_HEAD;

            ConfigurationNode texture = node.node("texture");
            if(texture.virtual()) {
                ConfigurationNode name = node.node("name");
                if(name.virtual()) {
                    throw new IllegalArgumentException("Skull parameters for player skull not supplied");
                }

                SkullStack skull = ImpactorItemStack.skull()
                        .player(name.getString(), false)
                        .build();
                base.nbt = skull.nbt();
            } else {
                base.nbt = ImpactorItemStack.skull().player(texture.getString(), true).build().nbt();
            }
        } else {
            base.type = ItemType.from(Key.key(node.node("key").getString("minecraft:skull")));
        }

        return base;
    }

}
