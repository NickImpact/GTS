package me.nickimpact.gts.listeners;

import com.nickimpact.impactor.api.event.annotations.Subscribe;
import com.nickimpact.impactor.api.event.listener.ImpactorEventListener;
import me.nickimpact.gts.api.events.TestEvent;
import me.nickimpact.gts.common.plugin.GTSPlugin;

public class TestListener implements ImpactorEventListener {

    @Subscribe
    public void onTestEvent(TestEvent<Integer> event) {
        GTSPlugin.getInstance().getPluginLogger().info("Event Triggered: " + event.getData());
    }

}
