package me.nickimpact.gts.api.holders;

import me.nickimpact.gts.api.enums.CommandResults;
import me.nickimpact.gts.api.flags.CommandFlag;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.ui.EntryUI;

import java.util.List;

public interface EntryClassification<Source, Material> {

	/** Represents the command functionality for an entry */
	//private TriFunction<T, List<String>, Boolean, CommandResults> cmdHandler;

	Class<? extends Entry> getClassType();

	List<String> getIdentifiers();

	Material getMaterial();

	EntryUI getUI();

	CommandProcessor<Source> getCommandHandler();

	default String getPrimaryIdentifier() {
		return this.getIdentifiers().size() > 0 ? this.getIdentifiers().get(0) : "???";
	}

	interface EntryClassificationBuilder {

		EntryClassificationBuilder classification(Class<? extends Entry> type);

		EntryClassificationBuilder identifiers(String... identifiers);

		EntryClassificationBuilder material(String material);

		EntryClassificationBuilder ui(EntryUI ui);

		EntryClassification build();

	}

	@FunctionalInterface
	interface CommandProcessor<Source> {

		CommandResults process(Source source, List<String> arguments, List<CommandFlag> flags);

	}
}
