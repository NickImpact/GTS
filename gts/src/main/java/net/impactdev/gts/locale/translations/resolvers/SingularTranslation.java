package net.impactdev.gts.locale.translations.resolvers;

import net.impactdev.gts.locale.translations.Translation;
import net.impactdev.impactor.api.utility.Context;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public final class SingularTranslation implements Translation<Component> {

    private final String template;

    public SingularTranslation(String template) {
        this.template = template;
    }

    @Override
    public @NotNull Component build(@NotNull Context context) {
        return this.processor().parse(this.template, context);
    }

    @Override
    public void send(@NotNull Audience audience, @NotNull Context context) {
        audience.sendMessage(this.build(context));
    }

}
