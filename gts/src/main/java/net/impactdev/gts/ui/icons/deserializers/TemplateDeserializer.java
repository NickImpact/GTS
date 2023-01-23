package net.impactdev.gts.ui.icons.deserializers;

import net.impactdev.gts.ui.icons.TemplateProperties;
import org.spongepowered.configurate.ConfigurationNode;

public interface TemplateDeserializer {

    TemplateProperties resolve(ConfigurationNode node) throws Exception;

}
