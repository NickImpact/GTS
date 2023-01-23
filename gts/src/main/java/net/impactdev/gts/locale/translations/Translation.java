package net.impactdev.gts.locale.translations;

import net.impactdev.impactor.api.text.TextProcessor;
import net.impactdev.impactor.api.utility.Context;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

public interface Translation<T> {

    @NotNull
    T build(final @NotNull Context context);

    void send(final @NotNull Audience audience, final @NotNull Context context);

    default TextProcessor processor() {
        return TextProcessor.mini();
    }

}
