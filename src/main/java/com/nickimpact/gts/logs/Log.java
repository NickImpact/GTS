package com.nickimpact.gts.logs;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.api.configuration.ConfigKey;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.utils.MessageUtils;
import com.nickimpact.gts.configuration.MsgConfigKeys;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import lombok.Builder;
import lombok.Getter;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Getter
@Builder
public class Log {

	public static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy - hh:mm aaa z");

	/** The ID of the log */
	private final UUID id = UUID.randomUUID();

	/** The date the log was issued */
	private final Date date = Date.from(Instant.now());

	/** The individual this log belongs to */
	private final UUID source;

	/** The action that forged the log */
	private final LogAction action;

	/** The actual text to the log itself */
	private final List<String> hover;

	@SuppressWarnings("unchecked")
	public static List<String> forgeTemplate(Player player, Listing listing, LogAction action) {
		List<String> template = Lists.newArrayList();
		template.addAll(GTS.getInstance().getMsgConfig().get(action.getTemplate()));
		template.addAll(listing.getEntry().getLogTemplate());

		Map<String, Object> variables = Maps.newHashMap();
		variables.put("dummy", listing.getEntry().getEntry());
		variables.put("dummy2", listing);
		variables.put("dummy3", listing.getEntry());

		try {
			template = GTS.getInstance().getTextParsingUtils().parse(
					template,
					player,
					null,
					variables
			).stream().map(TextSerializers.FORMATTING_CODE::serialize).collect(Collectors.toList());
		} catch (NucleusException e) {
			e.printStackTrace();
		}

		return template;
	}

	public Text toText(CommandSource src, int id) {
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("action", this.action.name());
		variables.put("issued", sdf.format(this.date));

		//try {
			Text base = Text.of(
					TextColors.GREEN, id, TextColors.GRAY, ") ", TextColors.YELLOW, this.action.name(),
					TextColors.GRAY, " - Issued: ", TextColors.GREEN, sdf.format(this.date)
			);

			return Text.builder().append(base).onHover(TextActions.showText(this.toSingle())).build();
		//} catch (NucleusException e) {
			//e.printStackTrace();
			//return Text.EMPTY;
		//}
	}

	private Text toSingle() {
		Text result = Text.EMPTY;
		for(String str : this.hover) {
			result = Text.of(
					result, TextSerializers.FORMATTING_CODE.deserialize(str),
					this.hover.get(this.hover.size() - 1).equals(str) ? Text.EMPTY : Text.NEW_LINE
			);
		}

		return result;
	}
}
