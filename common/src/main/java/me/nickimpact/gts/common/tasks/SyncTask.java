package me.nickimpact.gts.common.tasks;

import lombok.RequiredArgsConstructor;
import me.nickimpact.gts.common.cache.BufferedRequest;
import me.nickimpact.gts.common.plugin.GTSPlugin;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class SyncTask implements Runnable {

	private final GTSPlugin plugin;

	@Override
	public void run() {

	}

	public static class Buffer extends BufferedRequest<Void> {

		private final GTSPlugin plugin;

		public Buffer(GTSPlugin plugin) {
			super(500L, TimeUnit.MILLISECONDS, plugin.getScheduler());
			this.plugin = plugin;
		}

		@Override
		protected Void perform() {
			new SyncTask(this.plugin).run();
			return null;
		}

	}
}
