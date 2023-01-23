package net.impactdev.gts.locale.translations;

import net.impactdev.gts.locale.TranslationManager;
import net.impactdev.gts.locale.translations.resolvers.MultiLineTranslation;
import net.impactdev.gts.locale.translations.resolvers.SingularTranslation;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.impactor.api.platform.audience.LocalizedAudience;
import net.impactdev.impactor.api.utility.Context;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class TranslationProvider<T> {

    private static final Locale FALLBACK = Locale.US;
    private final String key;

    private TranslationProvider(String key) {
        this.key = key;
    }

    public static <T> TranslationProvider<T> provider(@NotNull final String key) {
        return new TranslationProvider<>(key);
    }

    public T resolve(@NotNull final Audience audience, @NotNull final Context context) {
        return this.translation(audience).build(context);
    }

    public void send(@NotNull final Audience audience, @NotNull final Context context) {
        Translation<T> resolved = this.translation(audience);
        if(resolved instanceof SingularTranslation) {
            audience.sendMessage(((SingularTranslation) resolved).build(context));
        } else {
            ((MultiLineTranslation) resolved).build(context).forEach(audience::sendMessage);
        }
    }

    private Translation<T> translation(@NotNull final Audience audience) {
        TranslationManager manager = GTSPlugin.instance().translations();
        Locale target = FALLBACK;
        if(audience instanceof LocalizedAudience) {
            target = ((LocalizedAudience) audience).locale();
        }

        return manager.fetchFromLocaleOrDefault(target).translation(this.key);
    }

}
