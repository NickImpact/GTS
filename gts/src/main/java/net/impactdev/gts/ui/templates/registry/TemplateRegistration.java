package net.impactdev.gts.ui.templates.registry;

import net.impactdev.gts.ui.templates.Template;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.ui.containers.View;
import net.impactdev.impactor.api.utility.builders.Builder;

public class TemplateRegistration<T extends View> {

    private final Config config;
    private final TemplateDeserializer<T> deserializer;

    private Template<T> template;

    public TemplateRegistration(RegistrationBuilder<T> builder) {
        this.config = builder.config;
        this.deserializer = builder.deserializer;

        this.template = this.deserializer.deserialize(this.config);
    }

    public void reload() {
        this.config.reload();
        this.template = this.deserializer.deserialize(this.config);
    }

    public Template<T> template() {
        if(Boolean.getBoolean("gts.view-dev")) {
            this.reload();
        }

        return this.template;
    }

    public static <T extends View> RegistrationBuilder<T> builder(Class<T> ignored) {
        return new RegistrationBuilder<>();
    }

    public static final class RegistrationBuilder<T extends View> implements Builder<TemplateRegistration<T>> {

        private Config config;
        private TemplateDeserializer<T> deserializer;

        public RegistrationBuilder<T> config(Config config) {
            this.config = config;
            return this;
        }

        public RegistrationBuilder<T> deserializer(TemplateDeserializer<T> deserializer) {
            this.deserializer = deserializer;
            return this;
        }

        @Override
        public TemplateRegistration<T> build() {
            return new TemplateRegistration<>(this);
        }
    }

    @FunctionalInterface
    public interface TemplateDeserializer<T extends View> {

        Template<T> deserialize(Config config);

    }

}
