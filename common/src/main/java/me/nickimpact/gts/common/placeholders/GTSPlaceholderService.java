/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package me.nickimpact.gts.common.placeholders;

import me.nickimpact.gts.api.placeholders.PlaceholderMetadata;
import me.nickimpact.gts.api.placeholders.PlaceholderParser;
import me.nickimpact.gts.api.placeholders.PlaceholderService;
import me.nickimpact.gts.api.placeholders.PlaceholderVariables;
import me.nickimpact.gts.api.user.Source;
import me.nickimpact.gts.common.placeholders.builders.GTSPlaceholderBuilder;
import net.kyori.text.TextComponent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class GTSPlaceholderService implements PlaceholderService {

	public static final Pattern SUFFIX_PATTERN = Pattern.compile(":([sp]+)$", Pattern.CASE_INSENSITIVE);
	private static final Pattern SEPARATOR = Pattern.compile("[\\s|:_]]");
	private final Map<String, PlaceholderMetadata> parsers = new HashMap<>();

	@Override
	public TextComponent parse(@Nullable Source source, String input, PlaceholderVariables variables) {
		String token = input.toLowerCase().trim().replace("{{", "").replaceAll("}}", "");
		Matcher m = SUFFIX_PATTERN.matcher(token);
		TextComponent append = TextComponent.empty();
		TextComponent prepend = TextComponent.empty();
		if(m.find(0)) {
			String match = m.group(1).toLowerCase();
			if(match.contains("s")) {
				append = TextComponent.of(" ");
			}
			if(match.contains("p")) {
				prepend = TextComponent.of(" ");
			}

			token = token.replaceAll(SUFFIX_PATTERN.pattern(), "");
		}

		return new GTSPlaceholderBuilder()
				.setToken(token)
				.setAssociatedSource(source)
				.setPlaceholderVariables(variables)
				.setPrependingTextIfNotEmpty(prepend)
				.setAppendingTextIfNotEmpty(append)
				.build()
				.toText();
	}

	@Override
	public void registerToken(String tokenName, PlaceholderParser parser) {
		if (SEPARATOR.asPredicate().test(tokenName)) {
			// can't be registered.
			throw new IllegalArgumentException("Tokens must not contain |, :, _ or space characters.");
		}
		String token = tokenName.toLowerCase();
		if (!this.parsers.containsKey(token)) {
			this.parsers.put(token, new PlaceholderMetadata(token, parser));
		} else {
			throw new IllegalStateException("Token " + tokenName.toLowerCase() + " has already been registered.");
		}
	}

	@Override
	public Optional<PlaceholderParser> getParser(String token) {
		return Optional.empty();
	}

	public PlaceholderMetadata getMetadata(String token) {
		PlaceholderMetadata m = this.parsers.get(token);
		if (m == null) {
			throw new NoSuchElementException("Parser does not exist.");
		}

		return m;
	}

}
