// This file originates from both Nucleus 2.0 (dualspiral) and Mixin (SpongePowered)
package net.impactdev.gts.api.util

import com.google.common.base.Strings
import com.google.common.collect.ImmutableMap
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import net.impactdev.impactor.api.json.factory.JElement
import net.impactdev.impactor.api.logging.Logger
import java.io.PrintStream
import java.util.*
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.regex.Pattern

/**
 * Prints information in a pretty box
 */
class PrettyPrinter @JvmOverloads constructor(width: Int = 100) {
    enum class Level {
        INFO, WARNING, DEBUG, ERROR
    }

    /**
     * Interface for an object which supports printing to pretty printer
     */
    interface IPrettyPrintable {
        /**
         * Append this object to the specified pretty printer
         *
         * @param printer The printer to append to
         */
        fun print(printer: PrettyPrinter)
    }

    /**
     * Interface for objects which need their width calculated prior to printing
     */
    internal interface IVariableWidthEntry {
        /**
         * Represents the width of the entry
         *
         * @return A positive value indicating the width
         */
        fun getWidth(): Int
    }

    /**
     * Interface for objects which control their own output format
     */
    internal interface ISpecialEntry
    private class BigX : IPrettyPrintable {
        override fun print(printer: PrettyPrinter) {
            for (i in 0..7) {
                printer.add("\\" + Strings.repeat(" ", 2 * (8 - i - 1)) + "/").center()
            }
            for (i in 7 downTo 0) {
                printer.add("/" + Strings.repeat(" ", 2 * (8 - i - 1)) + "\\").center()
            }
        }
    }

    private inner class KeyValue(private val key: String, private val value: Any) : IVariableWidthEntry {
        override fun toString(): String {
            return String.format(kvFormat, key, value)
        }

        override fun getWidth(): Int {
            return this.toString().length
        }
    }

    private inner class HorizontalRule(private vararg val hrChars: Char) : ISpecialEntry {
        override fun toString(): String {
            return Strings.repeat(String(hrChars), width + 2)
        }
    }

    private inner class CenteredText(private val centered: Any) {
        override fun toString(): String {
            val text = centered.toString()
            return String.format("%" + ((width - text.length) / 2 + text.length) + "s", text)
        }
    }

    /**
     * Table column alignment
     */
    internal enum class Alignment(private val format: BiFunction<Int, Int, String>) {
        LEFT(BiFunction<Int, Int, String> { width: Int, text: Int? -> "%-" + width + "s" }), CENTER(
            BiFunction { width: Int, text: Int ->
                "%" + ((width - text) / 2 + text - 2) + "s" + Strings.repeat(
                    " ",
                    Math.max(0, (width - text) / 2 - text + 2)
                )
            }),
        RIGHT(
            BiFunction<Int, Int, String> { width: Int, text: Int? -> "%" + width + "s" });

        fun getFormat(width: Int, text: String): String {
            return format.apply(width, text.length)
        }
    }

    private class Table : IVariableWidthEntry {
        val columns: MutableList<Column> = ArrayList()
        private val rows: MutableList<Row> = ArrayList()
        var format = "%s"
            private set
        private var colSpacing = 2
        var addHeader = true
        fun headerAdded() {
            addHeader = false
        }

        fun setColSpacing(spacing: Int) {
            colSpacing = Math.max(0, spacing)
            updateFormat()
        }

        fun grow(size: Int): Table {
            while (columns.size < size) {
                columns.add(Column(this))
            }
            updateFormat()
            return this
        }

        fun add(column: Column): Column {
            columns.add(column)
            return column
        }

        fun add(row: Row): Row {
            rows.add(row)
            return row
        }

        fun addColumn(title: String): Column {
            return this.add(Column(this, title))
        }

        fun addColumn(align: Alignment, size: Int, title: String): Column {
            return this.add(Column(this, align, size, title))
        }

        fun addRow(width: Int, vararg args: Any?): Row {
            return this.add(Row(this, width, *args))
        }

        fun updateFormat() {
            val spacing = Strings.repeat(" ", colSpacing / 2)
            val format = StringBuilder()
            var addSpacing = false
            for (column in columns) {
                if (addSpacing) {
                    format.append(spacing)
                    format.append("|")
                    format.append(spacing)
                }
                addSpacing = true
                format.append(column.format)
            }
            this.format = format.toString()
        }

        val titles: Array<Any>
            get() {
                val titles: MutableList<Any> = ArrayList()
                for (column in columns) {
                    titles.add(column.getTitle())
                }
                return titles.toTypedArray()
            }

        override fun toString(): String {
            var nonEmpty = false
            val titles = arrayOfNulls<String>(columns.size)
            for (col in columns.indices) {
                titles[col] = columns[col].toString()
                nonEmpty = nonEmpty or !titles[col]!!.isEmpty()
            }
            return if (nonEmpty) String.format(format, *titles as Array<Any?>) else null
        }

        override fun getWidth(): Int {
            val str = this.toString()
            return str?.length ?: 0
        }
    }

    /** A [table&#39;s][Table] column  */
    private class Column(private val parent: Table) : IVariableWidthEntry {
        private var align = Alignment.LEFT
        private var minWidth = 1
        private var maxWidth = Int.MAX_VALUE
        private var size = 0
        private var title = ""
        var format = "%s"
            private set

        constructor(parent: Table, title: String) : this(parent) {
            this.title = title
            minWidth = title.length
            updateFormat()
        }

        constructor(table: Table, align: Alignment, size: Int, title: String) : this(table, title) {
            this.align = align
            this.size = size
        }

        fun setAlignment(alignment: Alignment) {
            align = alignment
        }

        fun setWidth(width: Int) {
            if (width > size) {
                size = width
                updateFormat()
            }
        }

        fun setMinWidth(width: Int) {
            if (width > minWidth) {
                minWidth = width
                updateFormat()
            }
        }

        fun setMaxWidth(width: Int) {
            size = Math.min(size, maxWidth)
            maxWidth = Math.max(1, width)
            updateFormat()
        }

        fun setTitle(title: String) {
            this.title = title
            setWidth(title.length)
        }

        private fun updateFormat() {
            val width = getWidth()
            format = align.getFormat(width, title)
            parent.updateFormat()
        }

        fun getMaxWidth(): Int {
            return maxWidth
        }

        fun getTitle(): String {
            return title
        }

        override fun toString(): String {
            return if (title.length > maxWidth) {
                title.substring(0, maxWidth)
            } else title
        }

        override fun getWidth(): Int {
            return Math.min(maxWidth, if (size == 0) minWidth else size)
        }
    }

    private class Row(parent: Table, width: Int, vararg args: Any) : IVariableWidthEntry {
        private val parent: Table
        private val args: Array<String?>
        override fun toString(): String {
            val args = arrayOfNulls<Any>(parent.columns.size)
            for (col in args.indices) {
                val column = parent.columns[col]
                if (col >= this.args.size) {
                    args[col] = ""
                } else {
                    args[col] = if (this.args[col]!!.length > column.getMaxWidth()) this.args[col]!!
                        .substring(0, column.getMaxWidth()) else this.args[col]
                }
            }
            val joiner = StringJoiner(" | ")
            for (i in args.indices) {
                val column = parent.columns[i]
                val out = args[i].toString()
                joiner.add(String.format(Alignment.LEFT.getFormat(column.getWidth(), out), out))
            }
            return joiner.toString()
        }

        override fun getWidth(): Int {
            return this.toString().length
        }

        init {
            this.parent = parent.grow(args.size)
            this.args = arrayOfNulls(args.size)
            for (i in 0 until args.size) {
                this.args[i] = args[i].toString()
                for (column in this.parent.columns) {
                    column.setMinWidth(width)
                }
            }
        }
    }

    /** Horizontal rule  */
    private val horizontalRule: HorizontalRule = HorizontalRule('*')

    /** Content lines  */
    private val lines: MutableList<Any> = ArrayList()

    /** Table  */
    private var table: Table? = null

    /** True when a variable-width entry is added whose width must be calculated on print  */
    private var recalcWidth = false

    /** Box width (adapts to contents)  */
    protected var width = 100

    /** Wrap width used when an explicit wrap width is not specified  */
    protected var wrapWidth = 80

    /** Key/value key width  */
    protected var kvKeyWidth = 10
    protected var kvFormat = makeKvFormat(kvKeyWidth)

    /**
     * Set the wrap width (default 80 columns)
     *
     * @param wrapWidth new width (in characters) to wrap to
     * @return fluent interface
     */
    fun wrapTo(wrapWidth: Int): PrettyPrinter {
        this.wrapWidth = wrapWidth
        return this
    }

    /**
     * Get the current wrap width
     *
     * @return the current wrap width
     */
    fun wrapTo(): Int {
        return wrapWidth
    }

    /**
     * Begin a new table with no header and adaptive column widths
     *
     * @return fluent interface
     */
    fun table(): PrettyPrinter {
        table = Table()
        return this
    }

    /**
     * Begin a new table with the specified headers and adaptive column widths
     *
     * @param titles Column titles
     * @return fluent interface
     */
    fun table(vararg titles: String): PrettyPrinter {
        table = Table()
        for (title in titles) {
            table!!.addColumn(title)
        }
        return this
    }

    /**
     * Begin a new table with the specified format. The format is specified as a
     * sequence of values with [String]s defining column titles,
     * [Integer]s defining column widths, and [Alignment]s defining
     * column alignments. Widths and alignment specifiers should follow the
     * relevant column title. Specify a *negative* value to specify the
     * *maximum* width for a column (values will be truncated).
     *
     *
     * For example, to specify a table with two columns of width 10:
     *
     * `printer.table("Column 1", 10, "Column 2", 10);`
     *
     *
     * A table with a column 30 characters wide and a right-aligned column 20
     * characters wide:
     *
     * `printer.table("Column 1", 30, "Column 2", 20, Alignment.RIGHT);
    ` *
     *
     * @param format format string, see description
     * @return fluent interface
     */
    fun table(vararg format: Any?): PrettyPrinter {
        table = Table()
        var column: Column? = null
        for (entry in format) {
            if (entry is String) {
                column = table!!.addColumn(entry)
            } else if (entry is Int && column != null) {
                val width = entry
                if (width > 0) {
                    column.setWidth(width)
                } else if (width < 0) {
                    column.setMaxWidth(-width)
                }
            } else if (entry is Alignment && column != null) {
                column.setAlignment(entry)
            } else if (entry != null) {
                column = table!!.addColumn(entry.toString())
            }
        }
        return this
    }

    /**
     * Set the column spacing for the current table. Default = 2
     *
     * @param spacing Column spacing in characters
     * @return fluent interface
     */
    fun spacing(spacing: Int): PrettyPrinter {
        if (table == null) {
            table = Table()
        }
        table!!.setColSpacing(spacing)
        return this
    }

    /**
     * Print the current table header. The table header is automatically printed
     * before the first row if not explicitly specified by calling this method.
     *
     * @return fluent interface
     */
    fun th(): PrettyPrinter {
        return this.th(false)
    }

    private fun th(onlyIfNeeded: Boolean): PrettyPrinter {
        if (table == null) {
            table = Table()
        }
        if (!onlyIfNeeded || table!!.addHeader) {
            table!!.headerAdded()
            hr('-')
            addLine(table)
            hr('-')
        }
        return this
    }

    /**
     * Print a table row with the specified values. If more columns are
     * specified than exist in the table, then the table is automatically
     * expanded.
     *
     * @param args column values
     * @return fluent interface
     */
    fun tr(vararg args: Any?): PrettyPrinter {
        this.th(true)
        addLine(table!!.addRow(width / args.size - 1, *args))
        recalcWidth = true
        return this
    }

    /**
     * Adds a blank line to the output
     *
     * @return fluent interface
     */
    fun add(): PrettyPrinter {
        addLine("")
        return this
    }

    /**
     * Adds a string line to the output
     *
     * @param string format string
     * @return fluent interface
     */
    fun add(string: String): PrettyPrinter {
        addLine(string)
        width = Math.max(width, string.length)
        return this
    }

    /**
     * Adds a formatted line to the output
     *
     * @param format format string
     * @param args arguments
     *
     * @return fluent interface
     */
    fun add(format: String?, vararg args: Any?): PrettyPrinter {
        val line = String.format(format!!, *args)
        addLine(line)
        width = Math.max(width, line.length)
        return this
    }
    /**
     * Add elements of the array to the output, one per line
     *
     * @param array Array of objects to print
     * @param format Format for each row
     * @return fluent interface
     */
    /**
     * Add elements of the array to the output, one per line
     *
     * @param array Array of objects to print
     * @return fluent interface
     */
    @JvmOverloads
    fun add(array: Array<Any?>, format: String? = "%s"): PrettyPrinter {
        for (element in array) {
            this.add(format, element)
        }
        return this
    }

    /**
     * Add elements of the array to the output, one per line, with array indices
     *
     * @param array Array of objects to print
     * @return fluent interface
     */
    fun addIndexed(array: Array<Any?>): PrettyPrinter {
        val indexWidth = (array.size - 1).toString().length
        val format = "[%" + indexWidth + "d] %s"
        for (index in array.indices) {
            this.add(format, index, array[index])
        }
        return this
    }

    /**
     * Add elements of the collection to the output, one per line, with indices
     *
     * @param c Collection of objects to print
     * @return fluent interface
     */
    fun addWithIndices(c: Collection<*>): PrettyPrinter {
        return addIndexed(c.toTypedArray())
    }

    /**
     * Adds a pretty-printable object to the output, the object is responsible
     * for adding its own representation to this printer
     *
     * @param printable object to add
     * @return fluent interface
     */
    fun add(printable: IPrettyPrintable?): PrettyPrinter {
        printable?.print(this)
        return this
    }

    /**
     * Print a formatted representation of the specified [JElement] as a pretty
     * printed JSON output.
     *
     * @param json The element to print
     * @return fluent interface
     */
    fun add(json: JElement): PrettyPrinter {
        this.add(json.toJson())
        return this
    }

    /**
     * Print a formatted representation of the specified [JsonElement] as a pretty
     * printed JSON output.
     *
     * @param json The element to print
     * @return fluent interface
     */
    fun add(json: JsonElement?): PrettyPrinter {
        val raw = GsonBuilder().setPrettyPrinting().create().toJson(json)
        for (line in raw.split("\n".toRegex()).toTypedArray()) {
            this.add(line)
        }
        return this
    }
    /**
     * Print a formatted representation of the specified throwable with the
     * specified indent
     *
     * @param th Throwable to print
     * @param indent Indent size for stacktrace lines
     * @return fluent interface
     */
    /**
     * Print a formatted representation of the specified throwable with the
     * default indent (4)
     *
     * @param th Throwable to print
     * @return fluent interface
     */
    @JvmOverloads
    fun add(th: Throwable, indent: Int = 4): PrettyPrinter {
        this.add("%s: %s", th.javaClass.name, th.message)
        this.add(th.stackTrace, indent)
        return addCause(th.cause, indent)
    }

    private fun addCause(th: Throwable?, indent: Int): PrettyPrinter {
        var th = th
        while (th != null) {
            this.add("Caused by: %s: %s", th.javaClass.name, th.message)
            this.add(th.stackTrace, indent)
            th = th.cause
        }
        return this
    }

    /**
     * Print a formatted representation of the specified stack trace with the
     * specified indent
     *
     * @param stackTrace stack trace to print
     * @param indent Indent size for stacktrace lines
     * @return fluent interface
     */
    fun add(stackTrace: Array<StackTraceElement?>, indent: Int): PrettyPrinter {
        val margin = Strings.repeat(" ", indent)
        for (st in stackTrace) {
            this.add("%s%s", margin, st)
        }
        return this
    }
    /**
     * Adds the specified object to the output
     *
     * @param object object to add
     * @param indent indent amount
     * @return fluent interface
     */
    /**
     * Adds the specified object to the output
     *
     * @param object object to add
     * @return fluent interface
     */
    @JvmOverloads
    fun add(`object`: Any, indent: Int = 0): PrettyPrinter {
        val margin = Strings.repeat(" ", indent)
        return this.append(`object`, indent, margin)
    }

    fun consume(consumer: Consumer<PrettyPrinter?>): PrettyPrinter {
        consumer.accept(this)
        return this
    }

    private fun append(`object`: Any, indent: Int, margin: String): PrettyPrinter {
        if (`object` is String) {
            return this.add("%s%s", margin, `object`)
        } else if (`object` is Iterable<*>) {
            for (entry in `object`) {
                this.append(entry, indent, margin)
            }
            return this
        } else if (`object` is Map<*, *>) {
            kvWidth(indent)
            return this.add(`object`)
        } else if (`object` is IPrettyPrintable) {
            return this.add(`object`)
        } else if (`object` is Throwable) {
            return this.add(`object`, indent)
        } else if (`object`.javaClass.isArray) {
            return this.add(`object` as Array<Any?>, "$indent%s")
        }
        return this.add("%s%s", margin, `object`)
    }

    /**
     * Adds a formatted line to the output, and attempts to wrap the line
     * content to the current wrap width
     *
     * @param format format string
     * @param args arguments
     *
     * @return fluent interface
     */
    fun addWrapped(format: String?, vararg args: Any?): PrettyPrinter {
        return this.addWrapped(wrapWidth, format, *args)
    }

    /**
     * Adds a formatted line to the output, and attempts to wrap the line
     * content to the specified width
     *
     * @param width wrap width to use for this content
     * @param format format string
     * @param args arguments
     *
     * @return fluent interface
     */
    fun addWrapped(width: Int, format: String?, vararg args: Any?): PrettyPrinter {
        var indent = ""
        val line = String.format(format!!, *args).replace("\t", "    ")
        val indentMatcher = Pattern.compile("^(\\s+)(.*)$").matcher(line)
        if (indentMatcher.matches()) {
            indent = indentMatcher.group(1)
        }
        try {
            for (wrappedLine in getWrapped(width, line, indent)) {
                addLine(wrappedLine)
            }
        } catch (ex: Exception) {
            this.add(line)
        }
        return this
    }

    private fun getWrapped(width: Int, line: String, indent: String): List<String> {
        var line = line
        val lines: MutableList<String> = ArrayList()
        while (line.length > width) {
            var wrapPoint = line.lastIndexOf(' ', width)
            if (wrapPoint < 10) {
                wrapPoint = width
            }
            val head = line.substring(0, wrapPoint)
            lines.add(head)
            line = indent + line.substring(wrapPoint + 1)
        }
        if (line.length > 0) {
            lines.add(line)
        }
        return lines
    }

    /**
     * Add a formatted key/value pair to the output
     *
     * @param key Key
     * @param format Value format
     * @param args Value args
     * @return fluent interface
     */
    fun kv(key: String?, format: String?, vararg args: Any?): PrettyPrinter {
        return this.kv(key, String.format(format!!, *args))
    }

    /**
     * Add a key/value pair to the output
     *
     * @param key Key
     * @param value Value
     * @return fluent interface
     */
    fun kv(key: String, value: Any?): PrettyPrinter {
        addLine(KeyValue(key, value))
        return kvWidth(key.length)
    }

    /**
     * Set the minimum key display width
     *
     * @param width width to set
     * @return fluent
     */
    fun kvWidth(width: Int): PrettyPrinter {
        if (width > kvKeyWidth) {
            kvKeyWidth = width
            kvFormat = makeKvFormat(width)
        }
        recalcWidth = true
        return this
    }

    /**
     * Add all values of the specified map to this printer as key/value pairs
     *
     * @param map Map with entries to add
     * @return fluent
     */
    fun add(map: Map<*, *>): PrettyPrinter {
        for ((key1, value) in map) {
            val key = key1?.toString() ?: "null"
            this.kv(key, value)
        }
        return this
    }
    /**
     * Adds a horizontal rule of the specified char to the output
     *
     * @param ruleChar character to use for the horizontal rule
     * @return fluent interface
     */
    /**
     * Adds a horizontal rule to the output
     *
     * @return fluent interface
     */
    @JvmOverloads
    fun hr(ruleChar: Char = '*'): PrettyPrinter {
        addLine(HorizontalRule(ruleChar))
        return this
    }

    /**
     * Center the last line added
     *
     * @return fluent interface
     */
    fun center(): PrettyPrinter {
        if (!lines.isEmpty()) {
            val lastLine = lines[lines.size - 1]
            if (lastLine is String) {
                addLine(CenteredText(lines.removeAt(lines.size - 1)))
            }
        }
        return this
    }

    fun bigX(): PrettyPrinter {
        BigX().print(this)
        return this
    }

    private fun addLine(line: Any?) {
        if (line == null) {
            return
        }
        lines.add(line)
        recalcWidth = recalcWidth or line is IVariableWidthEntry
    }

    /**
     * Print this printer to the specified output
     *
     * @param stream stream to print to
     * @return fluent interface
     */
    fun print(stream: PrintStream): PrettyPrinter {
        updateWidth()
        printSpecial(stream, horizontalRule)
        for (line in lines) {
            if (line is ISpecialEntry) {
                printSpecial(stream, line)
            } else {
                printString(stream, line.toString())
            }
        }
        printSpecial(stream, horizontalRule)
        return this
    }

    private fun printSpecial(stream: PrintStream, line: ISpecialEntry) {
        stream.printf("/*%s*/\n", line.toString())
    }

    private fun printString(stream: PrintStream, string: String?) {
        if (string != null) {
            stream.printf(
                """
    /* %-${width}s */
    
    """.trimIndent(), string
            )
        }
    }
    /**
     * Write this printer to the specified logger
     *
     * @param logger logger to log to
     * @param level log level
     * @return fluent interface
     */
    /**
     * Write this printer to the specified logger at [Level.INFO]
     *
     * @param logger logger to log to
     * @return fluent interface
     */
    @JvmOverloads
    fun log(logger: Logger, level: Level = Level.INFO): PrettyPrinter {
        updateWidth()
        logSpecial(logger, level, horizontalRule)
        for (line in lines) {
            if (line is ISpecialEntry) {
                logSpecial(logger, level, line)
            } else {
                logString(logger, level, line.toString())
            }
        }
        logSpecial(logger, level, horizontalRule)
        return this
    }

    private fun logSpecial(logger: Logger, level: Level, line: ISpecialEntry) {
        LOGGER_LEVEL.getOrDefault(level, DEFAULT_LOGGER).accept(logger, String.format("/*%s*/", line.toString()))
    }

    private fun logString(logger: Logger, level: Level, line: String?) {
        if (line != null) {
            LOGGER_LEVEL.getOrDefault(level, DEFAULT_LOGGER)
                .accept(logger, String.format("/* %-" + width + "s */", line))
        }
    }

    private fun updateWidth() {
        if (recalcWidth) {
            recalcWidth = false
            for (line in lines) {
                if (line is IVariableWidthEntry) {
                    width = Math.min(4096, Math.max(width, line.getWidth()))
                }
            }
        }
    }

    companion object {
        private val LOGGER_LEVEL = ImmutableMap.builder<Level, BiConsumer<Logger, String>>()
            .put(Level.INFO, BiConsumer { obj: Logger, s: String? -> obj.info(s) })
            .put(Level.WARNING, BiConsumer { obj: Logger, s: String? -> obj.warn(s) })
            .put(Level.ERROR, BiConsumer { obj: Logger, s: String? -> obj.error(s) })
            .put(Level.DEBUG, BiConsumer { obj: Logger, s: String? -> obj.debug(s) })
            .build()
        private val DEFAULT_LOGGER = BiConsumer<Logger, String> { obj: Logger, s: String? -> obj.info(s) }
        private fun makeKvFormat(keyWidth: Int): String {
            return String.format("%%%ds : %%s", keyWidth)
        }

        /**
         * Convenience method, alternative to using <tt>Thread.dumpStack</tt> which
         * prints to stderr in pretty-printed format.
         */
        fun dumpStack(logger: Logger?) {
            PrettyPrinter().add(Exception("Stack trace")).print(System.err)
        }

        /**
         * Convenience methods, pretty-prints the specified throwable to stderr
         *
         * @param th Throwable to log
         */
        fun print(logger: Logger?, th: Throwable) {
            PrettyPrinter().add(th).print(System.err)
        }
    }

    init {
        this.width = width
    }
}