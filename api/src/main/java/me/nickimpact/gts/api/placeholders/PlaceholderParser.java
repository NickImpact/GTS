/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package me.nickimpact.gts.api.placeholders;

import net.kyori.text.TextComponent;

@FunctionalInterface
public interface PlaceholderParser {

	/**
	 * Creates a {@link TextComponent} based on the provided {@link Placeholder.Standard}.
	 *
	 * <p>This method should not throw an error, instead doing so in
	 * {@link #validate(Placeholder.Standard)} which is called when the
	 * {@link Placeholder} that will call this is built.</p>
	 *
	 * @param placeholder The {@link Placeholder.Standard}
	 * @return The {@link TextComponent}
	 */
	TextComponent parse(Placeholder.Standard placeholder);

	/**
	 * Validates a {@link Placeholder.Standard} as its being built. Throw an
	 * {@link IllegalStateException} if the placeholder will not be valid.
	 *
	 * @param placeholder The newly built {@link Placeholder.Standard}
	 * @throws IllegalStateException if the placeholder is not valid.
	 */
	default void validate(Placeholder.Standard placeholder) throws IllegalStateException { }

	interface RequireSender extends PlaceholderParser {

		/**
		 * Validates a {@link Placeholder} as its being built. Throw an
		 * {@link IllegalStateException} if the placeholder will not be valid.
		 *
		 * <p>This is default implemented to require {@link Placeholder.Standard#getAssociatedSource()}
		 * to contain a source.</p>
		 *
		 * @param placeholder The newly built {@link Placeholder.Standard}
		 * @throws IllegalStateException if the placeholder is not valid.
		 */
		default void validate(Placeholder.Standard placeholder) throws IllegalStateException {
			placeholder.getAssociatedSource().orElseThrow(() -> new IllegalStateException("Must contain an associated source!"));
		}

	}

}