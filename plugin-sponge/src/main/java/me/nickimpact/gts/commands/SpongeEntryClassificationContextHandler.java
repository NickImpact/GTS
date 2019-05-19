package me.nickimpact.gts.commands;

import co.aikar.commands.SpongeCommandExecutionContext;
import co.aikar.commands.contexts.ContextResolver;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.holders.EntryClassification;

import java.util.List;

public class SpongeEntryClassificationContextHandler {

	public static ContextResolver<EntryClassification, SpongeCommandExecutionContext> getContextResolver() {
		return c -> {
			String first = c.popFirstArg();
			List<EntryClassification> classifications = GTS.getInstance().getAPIService().getEntryRegistry().getClassifications();

			return classifications.stream().filter(classification -> ((List<String>)classification.getIdentifers()).stream()
					.anyMatch(identifier -> identifier.equalsIgnoreCase(first)))
					.findAny()
					.orElse(null);
		};
	}

}
