package net.impactdev.gts.locale.translations;

import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.impactor.api.adventure.LocalizedAudience;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.utilities.context.Context;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MultiLineTranslation implements Translation<List<Component>> {

    private final ConfigKey<List<String>> source;

    public MultiLineTranslation(ConfigKey<List<String>> key) {
        this.source = key;
    }

    @Override
    public @NotNull List<Component> build(@NotNull Locale locale, @NotNull Context context) {
        return GTSPlugin.instance().translations()
                .fetchFromLocaleOrDefault(locale)
                .get(this.source)
                .stream()
                .map(input -> this.translate(input, context))
                .collect(Collectors.toList());
    }

    @Override
    public void send(final @NotNull LocalizedAudience audience, final @NotNull Context context) {
        for(Component line : this.build(audience.locale(), context)) {
            audience.sendMessage(line);
        }
    }

}
