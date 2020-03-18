/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package me.nickimpact.gts.sponge.text;

import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.placeholders.Placeholder;
import me.nickimpact.gts.api.placeholders.PlaceholderMetadata;
import me.nickimpact.gts.api.placeholders.PlaceholderParser;
import me.nickimpact.gts.api.placeholders.PlaceholderService;
import me.nickimpact.gts.api.placeholders.PlaceholderVariables;
import me.nickimpact.gts.api.services.ServiceManager;
import me.nickimpact.gts.api.text.MessageService;
import me.nickimpact.gts.common.placeholders.GTSPlaceholderService;
import me.nickimpact.gts.sponge.sources.SpongeSource;
import net.kyori.text.TextComponent;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpongeMessageService implements MessageService<Text, SpongeSource> {

	private static final Pattern TOKEN_LOCATOR = Pattern.compile("(^[^{]+)?([{][{][\\w-:]+[}][}])(.+)?");

	@Override
	public Text getForSource(String message, SpongeSource source, @Nullable Map<String, PlaceholderParser> tokens, @Nullable PlaceholderVariables variables) {
		ServiceManager manager = GtsService.getInstance().getServiceManager();
		Text text = Text.EMPTY;

		String reference = message;
		while(!reference.isEmpty()) {
			Matcher matcher = TOKEN_LOCATOR.matcher(reference);
			if(matcher.find()) {
				String token = matcher.group(2).replace("{{", "").replace("}}", "");
				TextRepresentable out = tokens != null && tokens.containsKey(token) ?
						this.parseToken(source, token, tokens.get(token), variables) :
						this.convert(manager.get(PlaceholderService.class).get().parse(source, token, variables));

				if(matcher.group(1) != null) {
					text = Text.of(text, TextSerializers.FORMATTING_CODE.deserialize(matcher.group(1) + TextSerializers.FORMATTING_CODE.serialize(out.toText())));
					reference = reference.replaceFirst("^[^{]+", "");
				} else {
					text = Text.of(text, out.toText());
				}

				reference = reference.replaceFirst("[{][{][\\w-:]+[}][}]", "");
			} else {
				text = Text.of(text, TextSerializers.FORMATTING_CODE.deserialize(reference));
				break;
			}
		}

		return text;
	}

	private TextRepresentable parseToken(SpongeSource source, String token, PlaceholderParser parser, PlaceholderVariables variables) {
		Matcher m = GTSPlaceholderService.SUFFIX_PATTERN.matcher(token);
		TextComponent appendSpace = TextComponent.empty();
		TextComponent prependSpace = TextComponent.empty();
		if (m.find(0)) {
			String match = m.group(1).toLowerCase();
			if (match.contains("s")) {
				appendSpace = TextComponent.space();
			}
			if (match.contains("p")) {
				prependSpace = TextComponent.space();
			}

			token = token.replaceAll(GTSPlaceholderService.SUFFIX_PATTERN.pattern(), "");
		}

		return this.convert(GtsService.getInstance().getRegistry().createBuilder(Placeholder.CustomBuilder.class)
				.setMetadata(new PlaceholderMetadata(token, parser))
				.setAssociatedSource(source)
				.setPlaceholderVariables(variables)
				.setPrependingTextIfNotEmpty(prependSpace)
				.setAppendingTextIfNotEmpty(appendSpace)
				.build()
				.toText()
		);
	}

	private TextRepresentable convert(TextComponent input) {
		return TextSerializers.JSON.deserialize(GsonComponentSerializer.INSTANCE.serialize(input));
	}

}
