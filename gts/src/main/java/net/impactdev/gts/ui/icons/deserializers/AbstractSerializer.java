package net.impactdev.gts.ui.icons.deserializers;

import com.google.common.collect.Lists;
import net.impactdev.gts.ui.icons.IconTemplate;
import net.impactdev.gts.ui.icons.TemplateProperties;
import net.impactdev.gts.ui.icons.functions.IconFunctionRegistry;
import net.kyori.adventure.key.Key;
import org.spongepowered.configurate.ConfigurationNode;

public abstract class AbstractSerializer implements TemplateDeserializer {

    @Override
    public TemplateProperties resolve(ConfigurationNode node) throws Exception {
        TemplateProperties properties = new TemplateProperties();
        properties.title = node.node("title").getString("");
        properties.lore = node.node("lore").getList(String.class, Lists.newArrayList());
        properties.slots = IconTemplate.SlotResolver.slots(node.node("position"));
        properties.function = IconFunctionRegistry.locate(
                Key.key(node.node("function").getString(""))
        ).orElse(null);

        return properties;
    }
}
