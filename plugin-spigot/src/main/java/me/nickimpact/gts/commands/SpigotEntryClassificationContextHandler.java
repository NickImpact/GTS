package me.nickimpact.gts.commands;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.contexts.ContextResolver;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.wrappers.EntryClassification;

import java.util.List;

public class SpigotEntryClassificationContextHandler {

	public static ContextResolver<EntryClassification, BukkitCommandExecutionContext> getContextResolver() {
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
