package com.nickimpact.gts.storage.dao.file;

import com.nickimpact.gts.GTS;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class FileWatcher implements Runnable {

	private final GTS plugin;

	private final Text PREFIX = Text.of(
			TextColors.YELLOW, "GTS ", TextColors.GRAY, "(", TextColors.DARK_AQUA, "File Watcher",
			TextColors.GRAY, ") \u00bb "
	);

	private final Map<String, WatchedLocation> keyMap;
	private final Map<String, Long> internalChanges;
	private WatchService watchService;

	public FileWatcher(GTS plugin) {
		this.plugin = plugin;
		this.keyMap = Collections.synchronizedMap(new HashMap<>());
		this.internalChanges = Collections.synchronizedMap(new HashMap<>());
		try {
			this.watchService = plugin.getDataDirectory().toPath().getFileSystem().newWatchService();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void subscribe(String id, Path path, Consumer<String> consumer) {
		if(watchService == null) {
			return;
		}

		// Register with a delay to ignore changes made at startup
		Sponge.getScheduler().createTaskBuilder().async().delayTicks(40).execute(() -> {
			try {
				if(keyMap.containsKey(id))
					throw new IllegalArgumentException("ID already registered");

				WatchKey key = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
				keyMap.put(id, new WatchedLocation(path, key, consumer));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).submit(GTS.getInstance());
	}

	public void registerChange(StorageLocation location, String fileName) {
		internalChanges.put(location.name().toLowerCase() + "/" + fileName, System.currentTimeMillis());
	}

	public void close() {
		if(watchService == null)
			return;

		try {
			watchService.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		long expireTime = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(4);
		internalChanges.values().removeIf(lastChange -> lastChange < expireTime);

		List<String> expired = new ArrayList<>();

		for(Map.Entry<String, WatchedLocation> ent : keyMap.entrySet()) {
			String id = ent.getKey();
			Path path = ent.getValue().getPath();
			WatchKey key = ent.getValue().getKey();

			List<WatchEvent<?>> watchEvents = key.pollEvents();

			for(WatchEvent<?> event : watchEvents) {
				Path context = (Path) event.context();

				if(context == null)
					continue;

				Path file = path.resolve(context);
				String fileName = context.toString();

				// Ignore temporary changes
				if(fileName.endsWith(".tmp"))
					continue;

				if(internalChanges.containsKey(id + "/" + fileName))
					continue;

				internalChanges.put(id + "/" + fileName, System.currentTimeMillis());
				plugin.getConsole().ifPresent(console -> console.sendMessages(
						Text.of(PREFIX, TextColors.DARK_AQUA, "Detected change in file: " + file.toString())
				));

				// Process the change
				ent.getValue().getFileConsumer().accept(fileName);
			}

			boolean valid = key.reset();
			if(!valid) {
				new RuntimeException("WatchKey no longer valid: " + key.toString()).printStackTrace();
				expired.add(id);
			}
		}

		expired.forEach(keyMap::remove);
	}

	@Getter
	@RequiredArgsConstructor
	private static class WatchedLocation {
		private final Path path;
		private final WatchKey key;
		private final Consumer<String> fileConsumer;
	}
}
