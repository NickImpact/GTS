package me.nickimpact.gts.api.text;

import me.nickimpact.gts.api.placeholders.PlaceholderParser;
import me.nickimpact.gts.api.placeholders.PlaceholderVariables;
import me.nickimpact.gts.api.services.Service;
import me.nickimpact.gts.api.user.Source;

import javax.annotation.Nullable;
import java.util.Map;

/**
 *
 *
 * @param <T> The output type for a message
 * @param <S> The implementation of a source
 */
public interface MessageService<T, S extends Source> extends Service {

	default T getForSource(String message, S source) {
		return this.getForSource(message, source, null);
	}

	default T getForSource(String message, S source, @Nullable Map<String, PlaceholderParser> tokens) {
		return this.getForSource(message, source, tokens, PlaceholderVariables.empty());
	}

	/**
	 * Parses out a Textual instance where the tokens have been parsed from the viewpoint of the supplied {@link Source}.
	 * Any unknown tokens in the parsed text will be left blank.
	 *
	 * <p>
	 * Should the additional tokens field be populated, these tokens will act as additional tokens that could be encountered,
	 * and will be used above standard tokens. This is useful for having a token in a specific context, such as "displayfrom",
	 * which might only be used in a message, and is not worth registering in a {@link me.nickimpact.gts.api.placeholders.PlaceholderService}.
	 * They must not contain the token start or end delimiters, simply just the id of the token.
	 * </p>
	 *
	 * <p>
	 * By supplying variables, you allow for any parsed placeholder to receive a set of additional information that might be
	 * useful to that particular placeholder. For example, when parsing data relative to a Pokemon, we obviously can't use
	 * just the {@link Source} for that information, as this data might not even belong to the source. Instead, this
	 * placeholder can ask for a variable matching its requested key, and parse its output based on that variable.
	 * </p>
	 *
	 * <p>
	 * Both the additional tokens and placeholder variables are optional fields, and are not required.
	 * </p>
	 *
	 * @param message The base message to translate into text, with color codes and placeholders parsed as needed
	 * @param source The source that reflective placeholders will focus on
	 * @param tokens Any additional placeholders that should be offered for translation should they be present in the input
	 * @param variables Any variable objects that might help to populate a placeholder
	 * @return The resulting Text as an outcome of the input.
	 */
	T getForSource(String message, S source, @Nullable Map<String, PlaceholderParser> tokens, @Nullable PlaceholderVariables variables);

}
