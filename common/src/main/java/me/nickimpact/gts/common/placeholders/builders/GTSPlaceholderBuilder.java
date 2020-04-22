/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package me.nickimpact.gts.common.placeholders.builders;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.placeholders.Placeholder;
import me.nickimpact.gts.api.placeholders.PlaceholderMetadata;
import me.nickimpact.gts.api.placeholders.PlaceholderService;
import me.nickimpact.gts.api.placeholders.PlaceholderVariables;
import me.nickimpact.gts.api.user.Source;
import me.nickimpact.gts.common.placeholders.GTSPlaceholderService;
import me.nickimpact.gts.common.placeholders.types.GTSPlaceholder;
import me.nickimpact.gts.common.placeholders.variables.GTSPlaceholderVariables;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import net.kyori.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class GTSPlaceholderBuilder implements Placeholder.StandardBuilder {

	public static final PlaceholderVariables EMPTY = new GTSPlaceholderVariables(ImmutableMap.of());

	private final GTSPlaceholderService placeholderService;
	@Nullable
	private PlaceholderMetadata metadata;
	@Nullable private Source source;
	private TextComponent prepend = TextComponent.empty();
	private TextComponent append = TextComponent.empty();
	private PlaceholderVariables placeholderVariables = PlaceholderVariables.empty();

	public GTSPlaceholderBuilder() {
		this.placeholderService = (GTSPlaceholderService) GTSService.getInstance().getRegistry().get(PlaceholderService.class);
	}

	@Override
	public Placeholder.StandardBuilder setToken(String token) {
		this.metadata = this.placeholderService.getMetadata(token);
		return this;
	}

	@Override
	public Placeholder.StandardBuilder setAssociatedSource(@Nullable Source source) {
		this.source = source;
		return this;
	}

	@Override
	public Placeholder.StandardBuilder setPlaceholderVariables(PlaceholderVariables placeholderVariables) {
		this.placeholderVariables = placeholderVariables == null ? EMPTY : placeholderVariables;
		return this;
	}

	@Override
	public Placeholder.StandardBuilder setPrependingTextIfNotEmpty(TextComponent prefix) {
		this.prepend = prefix == null ? TextComponent.empty() : prefix;
		return this;
	}

	@Override
	public Placeholder.StandardBuilder setAppendingTextIfNotEmpty(TextComponent prefix) {
		this.append = append == null ? TextComponent.empty() : prefix;
		return this;
	}

	@Override
	public Placeholder.Standard build() {
		Preconditions.checkState(this.metadata != null, "Parser has not been set!");
		Placeholder.Standard placeholder = new GTSPlaceholder(this.metadata, this.source, this.prepend, this.append, this.placeholderVariables);
		this.metadata.getParser().validate(placeholder);
		return placeholder;
	}

	@Override
	public Placeholder.@NonNull StandardBuilder from(Placeholder.@NonNull Standard value) {
		Preconditions.checkArgument(value instanceof GTSPlaceholder, "Must be a GTS Placeholder");
		GTSPlaceholder np = (GTSPlaceholder) value;
		this.metadata = np.getMetadata();
		this.source = np.getAssociatedSource().orElse(null);
		this.placeholderVariables = value.getVariables();
		return this;
	}

}
