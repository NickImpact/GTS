package me.nickimpact.gts.api.services;

public interface Service {

	interface RequiresInit extends Service {

		void init();

	}

}
