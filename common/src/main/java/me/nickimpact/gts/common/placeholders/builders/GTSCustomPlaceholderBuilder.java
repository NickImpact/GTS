/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package me.nickimpact.gts.common.placeholders.builders;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import me.nickimpact.gts.api.placeholders.Placeholder;
import me.nickimpact.gts.api.placeholders.PlaceholderMetadata;
import me.nickimpact.gts.api.placeholders.PlaceholderVariables;
import me.nickimpact.gts.api.user.Source;
import me.nickimpact.gts.common.placeholders.types.GTSCustomPlaceholder;
import me.nickimpact.gts.common.placeholders.types.GTSPlaceholder;
import me.nickimpact.gts.common.placeholders.variables.GTSPlaceholderVariables;
import net.kyori.text.TextComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class GTSCustomPlaceholderBuilder implements Placeholder.CustomBuilder {

	public static final PlaceholderVariables EMPTY = new GTSPlaceholderVariables(ImmutableMap.of());

	@Nullable
	private PlaceholderMetadata metadata;

	@Nullable
	private Source source;

	private TextComponent prepend = TextComponent.empty();
	private TextComponent append = TextComponent.empty();
	private PlaceholderVariables placeholderVariables = PlaceholderVariables.empty();

	@Override
	public Placeholder.CustomBuilder setMetadata(PlaceholderMetadata metadata) {
		this.metadata = metadata;
		return this;
	}

	@Override
	public Placeholder.CustomBuilder setAssociatedSource(@Nullable Source source) {
		this.source = source;
		return this;
	}

	@Override
	public Placeholder.CustomBuilder setPlaceholderVariables(PlaceholderVariables placeholderVariables) {
		this.placeholderVariables = placeholderVariables == null ? EMPTY : placeholderVariables;
		return this;
	}

	@Override
	public Placeholder.CustomBuilder setPrependingTextIfNotEmpty(TextComponent prefix) {
		this.prepend = prefix == null ? TextComponent.empty() : prefix;
		return this;
	}

	@Override
	public Placeholder.CustomBuilder setAppendingTextIfNotEmpty(TextComponent prefix) {
		this.append = append == null ? TextComponent.empty() : prefix;
		return this;
	}

	@Override
	public Placeholder.CustomBuilder from(Placeholder.Custom value) {
		Preconditions.checkArgument(value instanceof GTSCustomPlaceholder, "Must be a GTS Placeholder");
		GTSPlaceholder np = (GTSPlaceholder) value;
		this.metadata = np.getMetadata();
		this.source = np.getAssociatedSource().orElse(null);
		this.placeholderVariables = value.getVariables();
		return this;
	}

	@Override
	public Placeholder.Custom build() {
		Preconditions.checkState(this.metadata != null, "Parser has not been set!");
		Placeholder.Custom placeholder = new GTSCustomPlaceholder(this.metadata, this.source, this.prepend, this.append, this.placeholderVariables);
		this.metadata.getParser().validate(placeholder);
		return placeholder;
	}

}
