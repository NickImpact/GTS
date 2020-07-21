package me.nickimpact.gts.util;

import com.nickimpact.impactor.api.Impactor;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.services.text.MessageService;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import org.spongepowered.api.text.Text;

public class GTSReferences {

	@SuppressWarnings("unchecked")
	public static final MessageService<Text> PARSER = Impactor.getInstance().getRegistry().get(MessageService.class);

	public static <T> T readMessageConfigOption(ConfigKey<T> key) {
		return GTSPlugin.getInstance().getMsgConfig().get(key);
	}

}
