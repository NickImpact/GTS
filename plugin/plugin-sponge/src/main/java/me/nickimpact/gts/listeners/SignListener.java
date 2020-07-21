package me.nickimpact.gts.listeners;

import com.ichorpowered.protocolcontrol.event.PacketEvent;
import com.ichorpowered.protocolcontrol.lib.kyori.event.method.annotation.Subscribe;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import net.minecraft.network.play.client.CPacketUpdateSign;

public class SignListener {

    @Subscribe
    public void onGTSSignUpdate(PacketEvent<CPacketUpdateSign> event) {
        String value = event.packet().getLines()[0];
        GTSPlugin.getInstance().getPluginLogger().info("Sign Value: " + value);
    }

}
