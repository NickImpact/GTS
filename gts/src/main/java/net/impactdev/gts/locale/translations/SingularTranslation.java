package net.impactdev.gts.locale.translations;

import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.impactor.api.adventure.LocalizedAudience;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.utilities.context.Context;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class SingularTranslation implements Translation<Component> {

    private final ConfigKey<String> source;

    SingularTranslation(ConfigKey<String> source) {
        this.source = source;
    }

    @Override
    public @NotNull Component build(@NotNull Locale locale, @NotNull Context context) {
        return this.translate(
                GTSPlugin.instance().translations().fetchFromLocaleOrDefault(locale).get(this.source),
                context
        );
    }

    @Override
    public void send(@NotNull LocalizedAudience audience, @NotNull Context context) {
        audience.sendMessage(this.build(audience.locale(), context));
    }

}
