/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package me.nickimpact.gts.common.placeholders.parsers;

import lombok.RequiredArgsConstructor;
import me.nickimpact.gts.api.placeholders.Placeholder;
import me.nickimpact.gts.api.placeholders.PlaceholderParser;
import net.kyori.text.TextComponent;

import java.util.function.Predicate;

@RequiredArgsConstructor
public class ConditionalParser implements PlaceholderParser {

	private final TextComponent out;
	private final Predicate<Placeholder.Standard> condition;

	@Override
	public TextComponent parse(Placeholder.Standard placeholder) {
		return this.condition.test(placeholder) ? out : TextComponent.empty();
	}
}
