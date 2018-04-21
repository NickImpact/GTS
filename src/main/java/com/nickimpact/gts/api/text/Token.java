package com.nickimpact.gts.api.text;

import com.nickimpact.gts.GTS;
import io.github.nucleuspowered.nucleus.internal.text.Tokens;
import lombok.Getter;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

@Getter
public class Token {

	private final String key;
	private final Translator translator;

	public Token(Builder builder) {
		key = builder.key;
		translator = builder.translator;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String key;
		private Translator translator;

		public Builder key(String key) {
			this.key = key;
			return this;
		}

		public Builder translator(Translator translator) {
			this.translator = translator;
			return this;
		}

		public Token build() {
			if(key == null) {
				throw new IllegalArgumentException("The key must not be null...");
			}

			if(translator == null) {
				translator = (src, variables, mappings) -> Optional.empty();
			}

			return new Token(this);
		}
	}
}
