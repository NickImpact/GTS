package me.nickimpact.gts.util;

import com.nickimpact.impactor.api.configuration.ConfigKey;
import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.api.text.MessageService;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.sponge.text.SpongeMessageService;

public class GTSReferences {

	public static final SpongeMessageService PARSER = (SpongeMessageService) GTSService.getInstance().getServiceManager().get(MessageService.class).get();

	public static <T> T readMessageConfigOption(ConfigKey<T> key) {
		return GTSPlugin.getInstance().getMsgConfig().get(key);
	}

}
