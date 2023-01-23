package net.impactdev.gts.locale.translations.resolvers;

import net.impactdev.gts.locale.translations.Translation;
import net.impactdev.impactor.api.utility.Context;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MultiLineTranslation implements Translation<List<Component>> {

    private final List<String> template;

    public MultiLineTranslation(final List<String> template) {
        this.template = template;
    }

    @Override
    public @NotNull List<Component> build(@NotNull Context context) {
        return this.processor().parse(this.template, context);
    }

    @Override
    public void send(final @NotNull Audience audience, final @NotNull Context context) {
        for(Component line : this.build(context)) {
            audience.sendMessage(line);
        }
    }

}
