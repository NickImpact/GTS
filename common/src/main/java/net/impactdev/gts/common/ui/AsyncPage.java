package net.impactdev.gts.common.ui;

import com.google.common.collect.Lists;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.gui.Icon;
import net.impactdev.impactor.api.gui.InventoryDimensions;
import net.impactdev.impactor.api.gui.Page;
import net.impactdev.impactor.api.gui.UI;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import lombok.Getter;
import net.impactdev.gts.common.plugin.GTSPlugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents a page which may or may not have its contents readily available at the time of being created.
 * As such, we can't readily apply the page options to the view. With this page type, you'll be able to dynamically
 * load the entries in the background, whilst providing a static view for the player until the page contents
 * are actually ready.
 *
 * @param <P> The type of player viewing the Page
 * @param <T> The type of item populating the page
 * @param <U> The UI implementation used to represent this UI
 * @param <I> The Icon implementation used to represent an element on the UI
 * @param <L> The layout design used for the UI
 * @param <S> The type used to represent the title of the UI
 * @param <M> The object type representing the base form of an item
 */
@SuppressWarnings("unchecked")
public abstract class AsyncPage<P, T, U extends UI, I extends Icon, L, S, M> implements Page<P, T, U, I> {

	private GTSPlugin plugin;

	private P viewer;
	private U view;

	private int page;
	protected L layout;

	@Getter
	private List<T> contents = Lists.newArrayList();
	private Function<T, I> applier;
	private Comparator<T> sorter;

	protected S title;
	private InventoryDimensions contentZone;

	private int rOffset;
	private int cOffset;

	protected Collection<Predicate<T>> conditions = Lists.newArrayList();

	protected Map<PageIconType, PageIcon<M>> pageIcons;

	private CompletableFuture<List<T>> future;

	public AsyncPage(GTSPlugin plugin, P viewer, CompletableFuture<List<T>> future, Predicate<T>... conditions) {
		this.plugin = plugin;
		this.viewer = viewer;
		this.title = this.getTitle();
		this.contentZone = this.getContentZone();

		Tuple<Integer, Integer> offsets = this.getOffsets();
		this.rOffset = offsets.getFirst();
		this.cOffset = offsets.getSecond();
		this.pageIcons = this.getPageIcons();
		this.page = 1;
		this.future = future;
		this.conditions.addAll(Arrays.asList(conditions));
	}

	@Override
	public P getViewer() {
		return this.viewer;
	}

	@Override
	public U getView() {
		return this.view;
	}

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	protected abstract S getTitle();

	protected abstract Map<PageIconType, PageIcon<M>> getPageIcons();

	protected abstract InventoryDimensions getContentZone();

	protected abstract Tuple<Integer, Integer> getOffsets();

	protected abstract Tuple<Long, TimeUnit> getTimeout();

	protected abstract L pagedDesign(L from);

	protected abstract L design();

	protected abstract U build(L layout);

	@Override
	public AsyncPage<P, T, U, I, L, S, M> applier(Function<T, I> function) {
		this.applier = function;
		return this;
	}

	private void queue(CompletableFuture<List<T>> future, long timeout, TimeUnit unit) {
		future.acceptEither(this.timeoutAfter(timeout, unit), list -> {
			this.applyWhenReady().accept(list);
			Impactor.getInstance().getScheduler().executeSync(() -> this.define(list));
		}).exceptionally(ex -> {
			Impactor.getInstance().getScheduler().executeSync(() -> this.doProvidedFill(this.getTimeoutIcon()));
			return null;
		});
	}

	private <W> CompletableFuture<W> timeoutAfter(long timeout, TimeUnit unit) {
		CompletableFuture<W> result = new CompletableFuture<>();
		Impactor.getInstance().getScheduler().asyncLater(() -> result.completeExceptionally(new TimeoutException()), timeout, unit);
		return result;
	}

	@Override
	public void define(List<T> list) {
		this.contents = list;
		this.clean();
		this.apply();
	}

	protected abstract I getLoadingIcon();

	protected abstract I getTimeoutIcon();

	protected abstract Consumer<List<T>> applyWhenReady();

	private void doProvidedFill(I icon) {
		this.clean();
		int index = this.cOffset + this.view.getDimension().getColumns() * this.rOffset;

		for(int r = 0; r < this.contentZone.getRows(); r++) {
			for(int c = 0; c < this.contentZone.getColumns(); c++) {
				this.view.setSlot(index + c, icon);
			}

			index += 9;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void open() {
		this.layout = this.pagedDesign(this.design());
		this.view = this.build(this.layout);
		this.doProvidedFill(this.getLoadingIcon());
		Tuple<Long, TimeUnit> timeout = this.getTimeout();
		this.queue(this.future, timeout.getFirst(), timeout.getSecond());
		this.view.open(this.viewer);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void close() {
		this.view.close(this.viewer);
	}

	public void setSorter(Comparator<T> comparator) {
		this.sorter = comparator;
	}

	@Override
	public void clean() {
		int index = this.cOffset + this.view.getDimension().getColumns() * this.rOffset;

		for(int r = 0; r < this.contentZone.getRows(); r++) {
			for(int c = 0; c < this.contentZone.getColumns(); c++) {
				int slot = index + c;
				this.view.clear(slot);
			}

			index += 9;
		}
	}

	@Override
	public void apply() {
		int capacity = this.contentZone.getColumns() * this.contentZone.getRows();
		if (this.contents.isEmpty()) {
			return;
		}

		AtomicInteger filtered = new AtomicInteger(this.contents.size());
		List<T> viewable = this.contents.stream()
				.filter(x -> this.conditions.stream().allMatch(y -> y.test(x)))
				.collect(Collectors.toList());
		if(this.sorter != null) {
			viewable.sort(this.sorter);
		}
		filtered.set(viewable.size());
		int pages = viewable.isEmpty() ? 1 : (viewable.size() % capacity == 0 ? viewable.size() / capacity : viewable.size() / capacity + 1);
		if (pages < this.page) {
			this.page = pages;
		}
		viewable = viewable.subList((this.page - 1) * capacity, this.page == pages ? filtered.get() : this.page * capacity);
		List<I> translated = viewable.stream().map(x -> this.applier.apply(x)).collect(Collectors.toList());

		int index = this.cOffset + this.view.getDimension().getColumns() * this.rOffset;
		int c = 0;
		int cap = index + this.contentZone.getColumns() - 1 + 9 * (this.contentZone.getRows() - 1);

		Iterator<I> iterator = translated.iterator();
		for (; index <= cap; index++, c++) {
			if (c == this.contentZone.getColumns()) {
				index += this.view.getDimension().getColumns() - this.contentZone.getColumns();
				c = 0;
			}

			if(iterator.hasNext()) {
				I icon = iterator.next();
				this.view.setSlot(index, icon);
			} else {
				this.view.clear(index);
			}

		}

	}

	public void cancelIfRunning() {
		if(!this.future.isDone()) {
			this.future.cancel(false);
		}
	}

}
