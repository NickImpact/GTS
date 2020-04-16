package me.nickimpact.gts.bungee.scheduling;

import me.nickimpact.gts.api.scheduling.SchedulerAdapter;
import me.nickimpact.gts.api.scheduling.SchedulerTask;
import me.nickimpact.gts.bungee.GTSBungeeBootstrap;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class BungeeSchedulerAdapter implements SchedulerAdapter<Executor> {

	private final GTSBungeeBootstrap bootstrap;

	private final Executor executor;
	private final Set<ScheduledTask> tasks = Collections.newSetFromMap(new WeakHashMap<>());

	public BungeeSchedulerAdapter(GTSBungeeBootstrap bootstrap) {
		this.bootstrap = bootstrap;
		this.executor = r -> bootstrap.getProxy().getScheduler().runAsync(bootstrap, r);
	}

	@Override
	public Executor async() {
		return this.executor;
	}

	@Override
	public Executor sync() {
		return this.executor;
	}

	@Override
	public SchedulerTask asyncLater(Runnable task, long delay, TimeUnit unit) {
		ScheduledTask t = this.bootstrap.getProxy().getScheduler().schedule(this.bootstrap, task, delay, unit);
		this.tasks.add(t);
		return t::cancel;
	}

	@Override
	public SchedulerTask asyncRepeating(Runnable task, long interval, TimeUnit unit) {
		ScheduledTask t = this.bootstrap.getProxy().getScheduler().schedule(this.bootstrap, task, interval, interval, unit);
		this.tasks.add(t);
		return t::cancel;
	}

	@Override
	public void shutdownScheduler() {
		for(ScheduledTask task : this.tasks) {
			try {
				task.cancel();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void shutdownExecutor() {

	}
}
