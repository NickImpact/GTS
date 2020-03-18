/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package me.nickimpact.gts.common.placeholders.types;

import lombok.RequiredArgsConstructor;
import me.nickimpact.gts.api.placeholders.Placeholder;
import me.nickimpact.gts.api.placeholders.PlaceholderMetadata;
import me.nickimpact.gts.api.placeholders.PlaceholderParser;
import me.nickimpact.gts.api.placeholders.PlaceholderVariables;
import me.nickimpact.gts.api.user.Source;
import net.kyori.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nullable;
import java.util.Optional;

@RequiredArgsConstructor
public class GTSCustomPlaceholder implements Placeholder.Custom {

	private final PlaceholderMetadata metadata;
	@Nullable private final Source source;
	private final TextComponent append;
	private final TextComponent prepend;
	private final PlaceholderVariables placeholderVariables;

	@Override
	public String getRegisteredToken() {
		return this.metadata.getToken();
	}

	@Override
	public PlaceholderParser getParser() {
		return this.metadata.getParser();
	}

	@Override
	public PlaceholderVariables getVariables() {
		return this.placeholderVariables;
	}

	@Override
	public Optional<Source> getAssociatedSource() {
		return Optional.ofNullable(this.source);
	}

	@Override
	public TextComponent getPrependingTextIfNotEmpty() {
		return this.prepend;
	}

	@Override
	public TextComponent getAppendingTextIfNotEmpty() {
		return this.append;
	}

	@NonNull
	@Override
	public TextComponent toText() {
		TextComponent result = this.metadata.getParser().parse(this);
		if (!result.equals(TextComponent.empty())) {
			return TextComponent.builder().append(this.prepend).append(result).append(this.append).build();
		}

		return result;
	}

}
