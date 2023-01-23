package net.impactdev.gts.ui.templates.configs;

import net.impactdev.gts.ui.icons.IconTemplate;
import net.impactdev.impactor.api.configuration.key.ConfigKey;

import java.util.List;
import java.util.stream.Collectors;

import static net.impactdev.impactor.api.configuration.key.ConfigKeyFactory.intKey;
import static net.impactdev.impactor.api.configuration.key.ConfigKeyFactory.key;
import static net.impactdev.impactor.api.configuration.key.ConfigKeyFactory.stringKey;

public final class SimpleViewConfig {

    public static final ConfigKey<String> TITLE = stringKey("title", "");
    public static final ConfigKey<Integer> ROWS = intKey("rows", 6);
    public static final ConfigKey<List<IconTemplate>> ICONS = key(
            adapter -> adapter.getNodeList("icons")
                .stream()
                .map(IconTemplate::fromConfiguration)
                .collect(Collectors.toList())
    );

}
