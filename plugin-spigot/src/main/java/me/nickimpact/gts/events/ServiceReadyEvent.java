package me.nickimpact.gts.events;

import me.nickimpact.gts.api.GtsService;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ServiceReadyEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private GtsService service;

	public ServiceReadyEvent(GtsService service) {
		this.service = service;
	}

	public GtsService getService() {
		return service;
	}

	@Override
	public HandlerList getHandlers() {
	return handlers;
}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
