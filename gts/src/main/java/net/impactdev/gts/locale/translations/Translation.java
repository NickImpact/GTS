package net.impactdev.gts.locale.translations;

import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.impactor.api.adventure.LocalizedAudience;
import net.impactdev.impactor.api.adventure.TextProcessor;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.utilities.context.Context;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public interface Translation<T> {

    static Translation<Component> singular(ConfigKey<String> key) {
        return new SingularTranslation(key);
    }

    static Translation<List<Component>> multiline(ConfigKey<List<String>> key) {
        return new MultiLineTranslation(key);
    }

    @NotNull
    T build(final @NotNull Locale locale, final @NotNull Context context);

    default Component translate(String input, Context context) {
        TextProcessor processor = GTSPlugin.instance().translations().processor();
        return processor.parse(input, context);
    }

    void send(final @NotNull LocalizedAudience audience, final @NotNull Context context);

}
