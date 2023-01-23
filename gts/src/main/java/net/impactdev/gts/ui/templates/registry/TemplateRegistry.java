package net.impactdev.gts.ui.templates.registry;

import com.google.common.collect.Maps;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.gts.ui.templates.configs.SimpleViewConfig;
import net.impactdev.gts.ui.templates.designs.ChestTemplate;
import net.impactdev.gts.ui.views.listings.BINView;
import net.impactdev.gts.ui.views.listings.ListingsView;
import net.impactdev.gts.util.GTSKeys;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.platform.sources.PlatformPlayer;
import net.impactdev.impactor.api.ui.containers.View;
import net.impactdev.impactor.api.ui.containers.views.ChestView;
import net.kyori.adventure.key.Key;

import java.util.Map;

public final class TemplateRegistry {

    private static TemplateRegistry instance;

    static {
        new TemplateRegistry();
    }

    private final Map<Key, TemplateRegistration<?>> templates = Maps.newHashMap();

    public TemplateRegistry() {
        instance = this;
        this.init();
    }

    public void init() {
        this.templates.put(GTSKeys.gts("main-menu"), TemplateRegistration.builder(ChestView.class)
                .config(Config.builder()
                        .path(GTSPlugin.instance().configurationDirectory().resolve("views").resolve("main.conf"))
                        .provider(SimpleViewConfig.class)
                        .build()
                )
                .deserializer(config -> ChestTemplate.create(
                        config.get(SimpleViewConfig.TITLE),
                        config.get(SimpleViewConfig.ROWS),
                        config.get(SimpleViewConfig.ICONS)
                ))
                .build()
        );
        this.templates.put(GTSKeys.gts("bin-listings"), BINView.template());
    }

    public static TemplateRegistry instance() {
        return instance;
    }

    public <T extends View> T provide(Key key, PlatformPlayer viewer) {
        return (T) this.templates.get(key).template().generate(viewer);
    }
}
