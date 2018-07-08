/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.nickimpact.gts.storage.dao.file;

import com.google.common.collect.Lists;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import com.nickimpact.gts.api.listings.Listing;
import com.nickimpact.gts.api.listings.entries.EntryHolder;
import com.nickimpact.gts.api.listings.pricing.PriceHolder;
import com.nickimpact.gts.logs.Log;
import com.nickimpact.gts.storage.dao.AbstractDao;
import lombok.Getter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public abstract class ConfigurateDao extends AbstractDao {

	@Getter
	private final String fileExtension;

	private final Text PREFIX = Text.of(
			TextColors.YELLOW, "GTS ", TextColors.GRAY, "(", TextColors.DARK_AQUA, "File Watcher",
			TextColors.GRAY, ") \u00bb "
	);

	private File listingsDir;
	private File logsDir;

	protected ConfigurateDao(GTS plugin, String name, String fileExtension) {
		super(plugin, name);
		this.fileExtension = fileExtension;
	}

	protected abstract ConfigurationLoader<? extends ConfigurationNode> loader(Path path);

	private ConfigurationNode readFile(StorageLocation location, String name) throws IOException {
		File file = new File(getDirectory(location), name + fileExtension);
		registerFileAction(location, file);
		return readFile(file);
	}

	private ConfigurationNode readFile(File file) throws IOException {
		if(!file.exists())
			return null;

		return loader(file.toPath()).load();
	}

	private void saveFile(StorageLocation location, String name, ConfigurationNode node) throws IOException {
		File file = new File(getDirectory(location), name + fileExtension);
		registerFileAction(location, file);
		saveFile(file, node);
	}

	private void saveFile(File file, ConfigurationNode node) throws IOException {
		if (node == null) {
			if (file.exists()) {
				file.delete();
			}
			return;
		}

		loader(file.toPath()).save(node);
	}

	private File getDirectory(StorageLocation location) {
		switch (location) {
			case LISTINGS:
				return listingsDir;
			case LOGS:
				return logsDir;
			default:
				throw new RuntimeException();
		}
	}

	private FilenameFilter getFileTypeFilter() {
		return (dir, name) -> name.endsWith(fileExtension);
	}

	private Exception reportException(String file, Exception ex) throws Exception {
		plugin.getConsole().ifPresent(console -> console.sendMessage(
				Text.of(GTSInfo.ERROR, "Exception thrown whilst performing i/o: " + file)
		));
		ex.printStackTrace();
		throw ex;
	}

	private void registerFileAction(StorageLocation type, File file) {
		plugin.getFileWatcher().ifPresent(fileWatcher -> fileWatcher.registerChange(type, file.getName()));
	}

	private void setupFiles() throws IOException {
		File data = new File(GTS.getInstance().getDataDirectory(), "data");
		data.mkdirs();

		listingsDir = new File(data, "listings");
		logsDir = new File(data, "logs");

		plugin.getFileWatcher().ifPresent(watcher -> {
			watcher.subscribe("listings", listingsDir.toPath(), s -> {
				if(!s.endsWith(fileExtension))
					return;

				plugin.getConsole().ifPresent(console -> console.sendMessages(
						Text.of(PREFIX, "Refreshing listings...")
				));
				plugin.getStorage().getListings();
			});
		});
	}

	@Override
	public void init() {
		try {
			setupFiles();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {

	}

	@Override
	public void addListing(Listing listing) {}

	@Override
	public void updateListing(Listing listing) {}

	@Override
	public void removeListing(UUID uuid) {}

	@Override
	public List<Listing> getListings() throws Exception {
		return Lists.newArrayList();
	}

	@Override
	public void addLog(Log log) {}

	@Override
	public void removeLog(int id) {}

	@Override
	public List<Log> getLogs(UUID uuid) throws Exception {
		return Lists.newArrayList();
	}

	@Override
	public void addHeldElement(EntryHolder holder) {}

	@Override
	public void removeHeldElement(EntryHolder holder) {}

	@Override
	public List<EntryHolder> getHeldElements() {
		return Lists.newArrayList();
	}

	@Override
	public void addHeldPrice(PriceHolder holder) {}

	@Override
	public void removeHeldPrice(PriceHolder holder) {}

	@Override
	public List<PriceHolder> getHeldPrices() {
		return Lists.newArrayList();
	}

	@Override
	public void addIgnorer(UUID uuid) {}

	@Override
	public void removeIgnorer(UUID uuid) {}

	@Override
	public List<UUID> getIgnorers() {
		return Lists.newArrayList();
	}

	@Override
	public void purge(boolean logs) throws Exception {}

	@Override
	public void save() throws Exception {}
}
