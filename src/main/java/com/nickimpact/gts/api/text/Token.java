package com.nickimpact.gts.api.text;

import io.github.nucleuspowered.nucleus.internal.text.Tokens;
import lombok.Getter;

import java.util.Optional;

@Getter
public class Token {

	private final String key;
	private final Tokens.Translator translator;

	public Token(Builder builder) {
		key = builder.key;
		translator = builder.translator;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String key;
		private Tokens.Translator translator;

		public Builder key(String key) {
			this.key = key;
			return this;
		}

		public Builder translator(Tokens.Translator translator) {
			this.translator = translator;
			return this;
		}

		public Token build() throws Exception {
			if(key == null) {
				throw new Exception("Attempt to build token without a key...");
			}

			if(translator == null) {
				translator = (src, variables, mappings) -> Optional.empty();
			}

			return new Token(this);
		}
	}
}
