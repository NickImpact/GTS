/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package me.nickimpact.gts.api.placeholders;

import me.nickimpact.gts.api.services.Service;
import me.nickimpact.gts.api.user.Source;
import net.kyori.text.TextComponent;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Provides a way to supply placeholders to the minigame design
 */
public interface PlaceholderService extends Service.RequiresInit {

	/**
	 * Parses a string on behalf of a {@link Source} based on the Nucleus
	 * chat format.
	 *
	 * @param commandSource The source that tokens should use as a context. May
	 *                      be {@code null}, but some tokens may not parse
	 *                      without this supplied.
	 * @param token The token text to parse.
	 * @return The parsed {@link TextComponent}.
	 */
	default TextComponent parse(@Nullable Source commandSource, String token) {
		return parse(commandSource, token, PlaceholderVariables.empty());
	}

	/**
	 * Parses a string on behalf of a {@link Source} based on the Nucleus
	 * chat format.
	 *
	 * @param commandSource The source that tokens should use as a context. May
	 *                      be {@code null}, but some tokens may not parse
	 *                      without this supplied.
	 * @param token The token text to parse.
	 * @param variables The variables to pass to the placeholder parser.
	 * @return The parsed {@link TextComponent}.
	 */
	TextComponent parse(@Nullable Source commandSource, String token, PlaceholderVariables variables);

	/**
	 * Registers a token.
	 *
	 * @param token The name of the token to register. This will be converted
	 *                  to lowercase.
	 * @param parser The parser to register.
	 * @throws IllegalArgumentException if the token name contains whitespace, :, | or _
	 * @throws IllegalStateException if the token name has already been registered
	 */
	void registerToken(String token, PlaceholderParser parser);

	/**
	 * Gets the parser associated with the provided token name, if any.
	 *
	 * @param token The token name
	 * @return The {@link PlaceholderParser}, if any
	 */
	Optional<PlaceholderParser> getParser(String token);

}
