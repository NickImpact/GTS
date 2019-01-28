package me.nickimpact.gts.api.events;

import lombok.Getter;
import me.nickimpact.gts.api.listings.Listing;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DataReceivedEvent implements Event {

	/** Represents the data that was successfully read in from the storage provider */
	@Getter private List<Listing> data;

	@Getter private boolean edited;

	public DataReceivedEvent(List<Listing> data) {
		this.data = data;
	}

	@Override
	public Cause getCause() {
		return Sponge.getCauseStackManager().getCurrentCause();
	}

	public List<Listing> filter(Predicate<Listing> predicate) {
		return this.data.stream().filter(predicate).collect(Collectors.toList());
	}

	public void filterAndEdit(Predicate<Listing> predicate, Consumer<List<Listing>> consumer) {
		consumer.accept(this.filter(predicate));
		edited = true;
	}
}
