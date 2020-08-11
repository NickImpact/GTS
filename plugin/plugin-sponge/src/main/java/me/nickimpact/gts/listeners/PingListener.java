package me.nickimpact.gts.listeners;

import com.nickimpact.impactor.api.event.annotations.Subscribe;
import com.nickimpact.impactor.api.event.listener.ImpactorEventListener;
import me.nickimpact.gts.api.events.PingEvent;
import me.nickimpact.gts.common.plugin.GTSPlugin;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class PingListener implements ImpactorEventListener {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.US)
            .withZone(ZoneId.systemDefault());

    @Subscribe
    public void onPing(PingEvent event) {
        GTSPlugin.getInstance().getPluginLogger().info("Ping ID: " + event.getPingID());
        GTSPlugin.getInstance().getPluginLogger().info("Time Sent: " + formatter.format(event.getTimeSent()));
    }

}
