package net.impactdev.gts.api;

import com.google.common.collect.ImmutableList;
import net.impactdev.gts.api.extensions.Extension;

public interface GTSService {

	static GTSService getInstance() {
		return GTSServiceProvider.get();
	}

	/**
	 * Returns an unmodifiable list of extensions currently hooked and running with GTS
	 *
	 * @return An immutable list of all loaded extensions hooked to GTS
	 */
	ImmutableList<Extension> getAllExtensions();

}
