package me.nickimpact.gts.scheduling;

import lombok.Builder;
import me.nickimpact.gts.GTSSpongeBootstrap;
import me.nickimpact.gts.api.scheduling.SchedulerAdapter;
import me.nickimpact.gts.api.scheduling.SchedulerTask;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.Task;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

@Builder
public class SpongeSchedulerAdapter implements SchedulerAdapter<SpongeExecutorService> {

	private final GTSSpongeBootstrap bootstrap;

	private final Scheduler scheduler;
	private final SpongeExecutorService sync;
	private final SpongeExecutorService async;

	private final Set<Task> tasks = Collections.newSetFromMap(new WeakHashMap<>());

	@Override
	public SpongeExecutorService async() {
		return this.async;
	}

	@Override
	public SpongeExecutorService sync() {
		return this.sync;
	}

	@Override
	public void executeAsync(Runnable task) {
		this.scheduler.createTaskBuilder().async().execute(task).submit(this.bootstrap);
	}

	@Override
	public void executeSync(Runnable task) {
		this.scheduler.createTaskBuilder().execute(task).submit(this.bootstrap);
	}

	@Override
	public SchedulerTask asyncLater(Runnable task, long delay, TimeUnit unit) {
		Task t = this.scheduler.createTaskBuilder()
				.async()
				.execute(task)
				.delay(delay, unit)
				.submit(this.bootstrap);

		this.tasks.add(t);
		return t::cancel;
	}

	@Override
	public SchedulerTask asyncRepeating(Runnable task, long interval, TimeUnit unit) {
		Task t = this.scheduler.createTaskBuilder()
				.async()
				.execute(task)
				.delay(interval, unit)
				.interval(interval, unit)
				.submit(this.bootstrap);

		this.tasks.add(t);
		return t::cancel;
	}

	@Override
	public void shutdownScheduler() {
		for(Task task : this.tasks) {
			try {
				task.cancel();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void shutdownExecutor() {}

}
