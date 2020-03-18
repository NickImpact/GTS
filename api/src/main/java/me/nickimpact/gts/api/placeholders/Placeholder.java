/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package me.nickimpact.gts.api.placeholders;

import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.registry.GTSRegistry;
import me.nickimpact.gts.api.user.Source;
import me.nickimpact.gts.api.util.Builder;
import net.kyori.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * A {@link TextComponent} that can be
 *
 * <p>While such placeholders will generally be built from tokenized strings,
 * these objects make no assumption about the format of text templating.</p>
 *
 * <p>Due to the potential for game objects being stored within the
 * placeholders, they should not be held for longer than necessary.</p>
 */
public interface Placeholder {

	/**
	 * Returns a builder that creates a {@link Placeholder} that represents
	 * plugin provided placeholders.
	 *
	 * @return A {@link StandardBuilder}
	 */
	static StandardBuilder builder() {
		return GtsService.getInstance().getRegistry().createBuilder(StandardBuilder.class);
	}

	/**
	 * Gets any {@link TextComponent} that will be prepend to the output returned by
	 * this placeholder if it not empty.
	 *
	 * @return The {@link TextComponent}
	 */
	TextComponent getPrependingTextIfNotEmpty();

	/**
	 * Gets any {@link TextComponent} that will be appended to the output returned by
	 * this placeholder if it not empty.
	 *
	 * @return The {@link TextComponent}
	 */
	TextComponent getAppendingTextIfNotEmpty();

	@NonNull TextComponent toText();

	/**
	 * A placeholder that represents a plugin provided placeholder.
	 */
	interface Standard extends Placeholder {

		/**
		 * The registered token text.
		 *
		 * @return The token
		 */
		String getRegisteredToken();

		/**
		 * Gets the {@link PlaceholderParser} that handles this
		 * placeholder.
		 *
		 * @return The {@link PlaceholderParser}
		 */
		PlaceholderParser getParser();

		/**
		 * Gets the {@link PlaceholderVariables} associated with this
		 * placeholder.
		 *
		 * @return The {@link PlaceholderVariables}
		 */
		PlaceholderVariables getVariables();

		/**
		 * If provided, the Source which to pull information
		 * from when building the placeholder text.
		 *
		 * <p>Examples of how this might affect a placeholder are:</p>
		 *
		 * <ul>
		 *     <li>
		 *         For a "name" placeholder that prints out the source's name,
		 *         the name would be selected from this source.
		 *     </li>
		 *     <li>
		 *         For a "balance" placeholder that returns a player's monetary
		 *         balance, this would pull the balance from the player.
		 *     </li>
		 * </ul>
		 *
		 * <p>It is important to note that the associated source does not
		 * necessarily have to be the sender/invoker of a message, nor does it
		 * have to be the recipient. The source is selected by the context of
		 * builder. It is up to plugins that use such placeholders to be aware
		 * of the context of which the placeholder is used.</p>
		 *
		 * @return The associated Source, if any.
		 */
		Optional<Source> getAssociatedSource();

	}

	/**
	 * A placeholder that represents a additional provided placeholder.
	 */
	interface Custom extends Standard {}

	/**
	 * A builder for {@link Placeholder.Standard} objects.
	 */
	interface StandardBuilder extends PlaceholderBuilder<Standard, StandardBuilder> {

		/**
		 * Sets the token that represents a {@link PlaceholderParser} for use
		 * in this {@link Placeholder}.
		 *
		 * @param token The token that represents a {@link PlaceholderParser}
		 * @return This, for chaining
		 * @throws NoSuchElementException if the {@code token} does not exist
		 */
		StandardBuilder setToken(String token) throws NoSuchElementException;

		/**
		 * Sets the {@link Source} to use as a source of information
		 * for this {@link Placeholder}. If {@code null}, removes this source.
		 *
		 * @param source The source, or null
		 * @return This, for chaining
		 *
		 * @see Standard#getAssociatedSource()
		 */
		StandardBuilder setAssociatedSource(@Nullable Source source);

		/**
		 * Sets the {@link PlaceholderVariables} to use as a source of information
		 * for this {@link Placeholder}.
		 *
		 * @param placeholderVariables The variables
		 * @return This, for chaining
		 */
		StandardBuilder setPlaceholderVariables(PlaceholderVariables placeholderVariables);

	}

	/**
	 * A builder for {@link Placeholder.Custom}
	 */
	interface CustomBuilder extends PlaceholderBuilder<Custom, CustomBuilder> {

		/**
		 * Sets the token that represents a {@link PlaceholderParser} for use
		 * in this {@link Placeholder}.
		 *
		 * @param metadata The data wrapping a token and {@link PlaceholderParser}
		 * @return This, for chaining
		 * @throws NoSuchElementException if the {@code token} does not exist
		 */
		CustomBuilder setMetadata(PlaceholderMetadata metadata);

		/**
		 * Sets the {@link Source} to use as a source of information
		 * for this {@link Placeholder}. If {@code null}, removes this source.
		 *
		 * @param source The source, or null
		 * @return This, for chaining
		 *
		 * @see Standard#getAssociatedSource()
		 */
		CustomBuilder setAssociatedSource(@Nullable Source source);

		/**
		 * Sets the {@link PlaceholderVariables} to use as a source of information
		 * for this {@link Placeholder}.
		 *
		 * @param placeholderVariables The variables
		 * @return This, for chaining
		 */
		CustomBuilder setPlaceholderVariables(PlaceholderVariables placeholderVariables);

	}

	/**
	 * A common interface for the placeholder builders.
	 *
	 * @param <O> The type of output
	 * @param <T> The type of builder
	 */
	interface PlaceholderBuilder<O, T extends Builder<O, T>> extends Builder<O, T> {

		/**
		 * The {@link TextComponent} that will be prepended to the placeholder
		 * if the returned text is not empty.
		 *
		 * @param prefix The prefix
		 * @return This, for chaining
		 */
		T setPrependingTextIfNotEmpty(@Nullable TextComponent prefix);

		/**
		 * The {@link TextComponent} that will be appended to the placeholder
		 * if the returned text is not empty.
		 *
		 * @param prefix The prefix
		 * @return This, for chaining
		 */
		T setAppendingTextIfNotEmpty(@Nullable TextComponent prefix);

		/**
		 * Builds and returns the placeholder.
		 *
		 * @return The appropriate {@link Placeholder}
		 * @throws IllegalStateException if the builder has not been completed,
		 *  or the associated {@link PlaceholderParser} could not validate the
		 *  built {@link Placeholder}, if applicable.
		 */
		O build() throws IllegalStateException;

	}

}
