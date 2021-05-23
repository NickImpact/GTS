package net.impactdev.gts.common.ui

import com.google.common.collect.Lists
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.impactor.api.Impactor
import net.impactdev.impactor.api.gui.Icon
import net.impactdev.impactor.api.gui.InventoryDimensions
import net.impactdev.impactor.api.gui.Page
import net.impactdev.impactor.api.gui.Page.PageIcon
import net.impactdev.impactor.api.gui.Page.PageIconType
import net.impactdev.impactor.api.gui.UI
import net.impactdev.impactor.api.utilities.mappings.Tuple
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors

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
</M></S></L></I></U></T></P> */
abstract class AsyncPage<P, T, U : UI<*, *, *, *>?, I : Icon<*, *, *>?, L, S, M>(
    private val plugin: GTSPlugin,
    private val viewer: P,
    future: CompletableFuture<List<T>>,
    vararg conditions: Predicate<T>?
) : Page<P, T, U, I> {
    private var view: U? = null
    var page: Int
    @kotlin.jvm.JvmField
    protected var layout: L? = null
    var contents: List<T> = Lists.newArrayList()
        private set
    private var applier: Function<T, I>? = null
    private var sorter: Comparator<T>? = null
    @kotlin.jvm.JvmField
    protected var title: S
    private val contentZone: InventoryDimensions
    private val rOffset: Int
    private val cOffset: Int
    @kotlin.jvm.JvmField
    protected var conditions: MutableCollection<Predicate<T>> = Lists.newArrayList()
    @kotlin.jvm.JvmField
    protected var pageIcons: Map<PageIconType, PageIcon<M>>
    private val future: CompletableFuture<List<T>>
    override fun getViewer(): P {
        return viewer
    }

    override fun getView(): U {
        return view
    }

    protected abstract fun getTitle(): S
    protected abstract fun getPageIcons(): Map<PageIconType, PageIcon<M>>
    protected abstract fun getContentZone(): InventoryDimensions
    protected abstract val offsets: Tuple<Int, Int>
    protected abstract val timeout: Tuple<Long, TimeUnit>
    protected abstract fun pagedDesign(from: L): L
    protected abstract fun design(): L
    protected abstract fun build(layout: L): U
    override fun applier(function: Function<T, I>): AsyncPage<P, T, U, I, L, S, M> {
        applier = function
        return this
    }

    private fun queue(future: CompletableFuture<List<T>>, timeout: Long, unit: TimeUnit) {
        future.acceptEither(timeoutAfter(timeout, unit)) { list: List<T> ->
            applyWhenReady().accept(list)
            Impactor.getInstance().scheduler.executeSync { define(list) }
        }.exceptionally { ex: Throwable? ->
            Impactor.getInstance().scheduler.executeSync { doProvidedFill(timeoutIcon) }
            null
        }
    }

    private fun <W> timeoutAfter(timeout: Long, unit: TimeUnit): CompletableFuture<W> {
        val result = CompletableFuture<W>()
        Impactor.getInstance().scheduler.asyncLater({ result.completeExceptionally(TimeoutException()) }, timeout, unit)
        return result
    }

    override fun define(list: List<T>) {
        contents = list
        clean()
        this.apply()
    }

    protected abstract val loadingIcon: I
    protected abstract val timeoutIcon: I
    protected abstract fun applyWhenReady(): Consumer<List<T>?>
    protected fun applyIfEmpty(): Runnable {
        return Runnable {}
    }

    private fun doProvidedFill(icon: I) {
        clean()
        var index = cOffset + view!!.getDimension().columns * rOffset
        for (r in 0 until contentZone.rows) {
            for (c in 0 until contentZone.columns) {
                view!!.setSlot(index + c, icon)
            }
            index += 9
        }
    }

    override fun open() {
        layout = pagedDesign(design())
        view = build(layout)
        doProvidedFill(loadingIcon)
        val timeout = timeout
        queue(future, timeout.first, timeout.second)
        view!!.open(viewer)
    }

    override fun close() {
        view!!.close(viewer)
    }

    fun setSorter(comparator: Comparator<T>?) {
        sorter = comparator
    }

    override fun clean() {
        var index = cOffset + view!!.getDimension().columns * rOffset
        for (r in 0 until contentZone.rows) {
            for (c in 0 until contentZone.columns) {
                val slot = index + c
                view!!.clear(slot)
            }
            index += 9
        }
    }

    override fun apply() {
        val capacity = contentZone.columns * contentZone.rows
        if (contents.isEmpty()) {
            return
        }
        val filtered = AtomicInteger(contents.size)
        var viewable = contents.stream()
            .filter { x: T -> conditions.stream().allMatch { y: Predicate<T> -> y.test(x) } }
            .collect(Collectors.toList())
        if (sorter != null) {
            viewable.sort(sorter)
        }
        filtered.set(viewable.size)
        val pages =
            if (viewable.isEmpty()) 1 else if (viewable.size % capacity == 0) viewable.size / capacity else viewable.size / capacity + 1
        if (pages < page) {
            page = pages
        }
        viewable = viewable.subList((page - 1) * capacity, if (page == pages) filtered.get() else page * capacity)
        val translated = viewable.stream().map { x: T -> applier!!.apply(x) }.collect(Collectors.toList())
        var index = cOffset + view!!.getDimension().columns * rOffset
        var c = 0
        val cap = index + contentZone.columns - 1 + 9 * (contentZone.rows - 1)
        val iterator: Iterator<I> = translated.iterator()
        while (index <= cap) {
            if (c == contentZone.columns) {
                index += view!!.getDimension().columns - contentZone.columns
                c = 0
            }
            if (iterator.hasNext()) {
                val icon = iterator.next()
                view!!.setSlot(index, icon)
            } else {
                view!!.clear(index)
            }
            index++
            c++
        }
        if (viewable.isEmpty()) {
            applyIfEmpty().run()
        }
    }

    fun cancelIfRunning() {
        if (!future.isDone) {
            future.cancel(false)
        }
    }

    init {
        title = getTitle()
        contentZone = getContentZone()
        val offsets = offsets
        rOffset = offsets.first
        cOffset = offsets.second
        pageIcons = getPageIcons()
        page = 1
        this.future = future
        this.conditions.addAll(Arrays.asList(*conditions))
    }
}