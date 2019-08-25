package me.nickimpact.gts.api.holders;

import me.nickimpact.gts.api.GtsService;

public class ServiceInstance {

	private static GtsService service;

	public static GtsService getService() {
		return service;
	}

	public static void setService(GtsService s) {
		service = s;
	}

}
