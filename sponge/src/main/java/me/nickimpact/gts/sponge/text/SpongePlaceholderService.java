package me.nickimpact.gts.sponge.text;

import me.nickimpact.gts.api.GTSService;
import me.nickimpact.gts.common.config.MsgConfigKeys;
import me.nickimpact.gts.common.placeholders.GTSPlaceholderService;
import me.nickimpact.gts.sponge.SpongePlugin;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;

public class SpongePlaceholderService extends GTSPlaceholderService {

	private final SpongePlugin plugin;

	public SpongePlaceholderService(SpongePlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void init() {
		this.register();
	}

	private void register() {
		GTSService.getInstance().registerPlaceholder("gts_prefix", placeholder -> LegacyComponentSerializer.legacy().deserialize(plugin.getConfiguration().get(MsgConfigKeys.PREFIX)));

	}
}
