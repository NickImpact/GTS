package me.nickimpact.gts.spigot.events;

import me.nickimpact.gts.api.GTSService;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ServiceReadyEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private GTSService service;

	public ServiceReadyEvent(GTSService service) {
		this.service = service;
	}

	public GTSService getService() {
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
