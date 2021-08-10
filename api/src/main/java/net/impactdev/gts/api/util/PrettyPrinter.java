// This file originates from both Nucleus 2.0 (dualspiral) and Mixin (SpongePowered)

package net.impactdev.gts.api.util;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.impactdev.impactor.api.json.factory.JElement;
import net.impactdev.impactor.api.logging.Logger;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Prints information in a pretty box
 */
public class PrettyPrinter {

    private static final ImmutableMap<Level, BiConsumer<Logger, String>> LOGGER_LEVEL =
            ImmutableMap.<Level, BiConsumer<Logger, String>>builder()
                    .put(Level.INFO, Logger::info)
                    .put(Level.WARNING, Logger::warn)
                    .put(Level.ERROR, Logger::error)
                    .put(Level.DEBUG, Logger::debug)
                    .build();
    private static final BiConsumer<Logger, String> DEFAULT_LOGGER = Logger::info;

    public enum Level {
        INFO,
        WARNING,
        DEBUG,
        ERROR
    }

    /**
     * Interface for an object which supports printing to pretty printer
     */
    public interface IPrettyPrintable {

        /**
         * Append this object to the specified pretty printer
         *
         * @param printer The printer to append to
         */
        void print(PrettyPrinter printer);

    }

    /**
     * Interface for objects which need their width calculated prior to printing
     */
    interface IVariableWidthEntry {

        /**
         * Represents the width of the entry
         *
         * @return A positive value indicating the width
         */
        int getWidth();

    }

    /**
     * Interface for objects which control their own output format
     */
    interface ISpecialEntry {}

    private static class BigX implements IPrettyPrintable {

        @Override
        public void print(PrettyPrinter printer) {
            for(int i = 0; i < 8; i++) {
                printer.add("\\" + Strings.repeat(" ", 2 * (8 - i - 1)) + "/").center();
            }

            for(int i = 7; i >= 0; i--) {
                printer.add("/" + Strings.repeat(" ", 2 * (8 - i - 1)) + "\\").center();
            }
        }

    }

    private class KeyValue implements IVariableWidthEntry {

        private final String key;
        private final Object value;

        public KeyValue(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format(PrettyPrinter.this.kvFormat, this.key, this.value);
        }

        @Override
        public int getWidth() {
            return this.toString().length();
        }
    }

    private class HorizontalRule implements ISpecialEntry {

        private final char[] hrChars;

        public HorizontalRule(char... hrChars) {
            this.hrChars = hrChars;
        }

        @Override
        public String toString() {
            return Strings.repeat(new String(this.hrChars), PrettyPrinter.this.width + 2);
        }

    }

    private class CenteredText {

        private final Object centered;

        public CenteredText(Object centered) {
            this.centered = centered;
        }

        @Override
        public String toString() {
            String text = this.centered.toString();
            return String.format("%" + (((PrettyPrinter.this.width - (text.length())) / 2) + text.length()) + "s", text);
        }

    }

    /**
     * Table column alignment
     */
    enum Alignment {
        LEFT((width, text) -> "%-" + width + "s"),
        CENTER((width, text) -> "%" + (((width - text) / 2) + text - 2) + "s" + Strings.repeat(" ", Math.max(0, ((width - text) / 2) - text + 2))),
        RIGHT((width, text) -> "%" + width + "s");

        private BiFunction<Integer, Integer, String> format;

        Alignment(BiFunction<Integer, Integer, String> format) {
            this.format = format;
        }

        public String getFormat(int width, String text) {
            return this.format.apply(width, text.length());
        }
    }

    private static class Table implements IVariableWidthEntry {

        private final List<Column> columns = new ArrayList<>();
        private final List<Row> rows = new ArrayList<>();

        private String format = "%s";
        private int colSpacing = 2;

        private boolean addHeader = true;

        void headerAdded() {
            this.addHeader = false;
        }

        void setColSpacing(int spacing) {
            this.colSpacing = Math.max(0, spacing);
            this.updateFormat();
        }

        Table grow(int size) {
            while(this.columns.size() < size) {
                this.columns.add(new Column(this));
            }
            this.updateFormat();
            return this;
        }

        Column add(Column column) {
            this.columns.add(column);
            return column;
        }

        Row add(Row row) {
            this.rows.add(row);
            return row;
        }

        Column addColumn(String title) {
            return this.add(new Column(this, title));
        }

        Column addColumn(Alignment align, int size, String title) {
            return this.add(new Column(this, align, size, title));
        }

        Row addRow(int width, Object... args) {
            return this.add(new Row(this, width, args));
        }

        void updateFormat() {
            String spacing = Strings.repeat(" ", this.colSpacing / 2);
            StringBuilder format = new StringBuilder();
            boolean addSpacing = false;
            for (Column column : this.columns) {
                if (addSpacing) {
                    format.append(spacing);
                    format.append("|");
                    format.append(spacing);
                }
                addSpacing = true;
                format.append(column.getFormat());
            }
            this.format = format.toString();
        }

        String getFormat() {
            return this.format;
        }

        Object[] getTitles() {
            List<Object> titles = new ArrayList<Object>();
            for (Column column : this.columns) {
                titles.add(column.getTitle());
            }
            return titles.toArray();
        }

        @Override
        public String toString() {
            boolean nonEmpty = false;
            String[] titles = new String[this.columns.size()];
            for (int col = 0; col < this.columns.size(); col++) {
                titles[col] = this.columns.get(col).toString();
                nonEmpty |= !titles[col].isEmpty();
            }
            return nonEmpty ? String.format(this.format, (Object[])titles) : null;
        }

        @Override
        public int getWidth() {
            String str = this.toString();
            return str != null ? str.length() : 0;
        }

    }

    /** A {@link Table table's} column */
    private static class Column implements IVariableWidthEntry {

        private final Table parent;

        private Alignment align = Alignment.LEFT;

        private int minWidth = 1;
        private int maxWidth = Integer.MAX_VALUE;

        private int size = 0;

        private String title = "";

        private String format = "%s";

        public Column(Table parent) {
            this.parent = parent;
        }

        public Column(Table parent, String title) {
            this(parent);
            this.title = title;
            this.minWidth = title.length();
            this.updateFormat();
        }

        public Column(Table table, Alignment align, int size, String title) {
            this(table, title);
            this.align = align;
            this.size = size;
        }

        void setAlignment(Alignment alignment) {
            this.align = alignment;
        }

        void setWidth(int width) {
            if(width > this.size) {
                this.size = width;
                this.updateFormat();
            }
        }

        void setMinWidth(int width) {
            if(width > this.minWidth) {
                this.minWidth = width;
                this.updateFormat();
            }
        }

        void setMaxWidth(int width) {
            this.size = Math.min(this.size, this.maxWidth);
            this.maxWidth = Math.max(1, width);
            this.updateFormat();
        }

        void setTitle(String title) {
            this.title = title;
            this.setWidth(title.length());
        }

        private void updateFormat() {
            int width = this.getWidth();
            this.format = this.align.getFormat(width, this.title);
            this.parent.updateFormat();
        }

        int getMaxWidth() {
            return this.maxWidth;
        }

        String getTitle() {
            return this.title;
        }

        String getFormat() {
            return this.format;
        }

        @Override
        public String toString() {
            if(this.title.length() > this.maxWidth) {
                return this.title.substring(0, this.maxWidth);
            }

            return this.title;
        }

        @Override
        public int getWidth() {
            return Math.min(this.maxWidth, this.size == 0 ? this.minWidth : this.size);
        }
    }

    private static class Row implements IVariableWidthEntry {

        private final Table parent;

        private final String[] args;

        public Row(Table parent, int width, Object... args) {
            this.parent = parent.grow(args.length);
            this.args = new String[args.length];
            for(int i = 0; i < args.length; i++) {
                this.args[i] = args[i].toString();
                for(Column column : this.parent.columns) {
                    column.setMinWidth(width);
                }
            }
        }

        @Override
        public String toString() {
            Object[] args = new Object[this.parent.columns.size()];
            for (int col = 0; col < args.length; col++) {
                Column column = this.parent.columns.get(col);
                if (col >= this.args.length) {
                    args[col] = "";
                } else {
                    args[col] = (this.args[col].length() > column.getMaxWidth()) ? this.args[col].substring(0, column.getMaxWidth()) : this.args[col];
                }
            }

            StringJoiner joiner = new StringJoiner(" | ");
            for(int i = 0; i < args.length; i++) {
                Column column = this.parent.columns.get(i);
                String out = args[i].toString();
                joiner.add(String.format(Alignment.LEFT.getFormat(column.getWidth(), out), out));
            }

            return joiner.toString();
        }

        @Override
        public int getWidth() {
            return this.toString().length();
        }

    }

    /** Horizontal rule */
    private final HorizontalRule horizontalRule = new HorizontalRule('*');

    /** Content lines */
    private final List<Object> lines = new ArrayList<Object>();

    /** Table */
    private Table table;

    /** True when a variable-width entry is added whose width must be calculated on print */
    private boolean recalcWidth = false;

    /** Box width (adapts to contents) */
    protected int width = 100;

    /** Wrap width used when an explicit wrap width is not specified */
    protected int wrapWidth = 80;

    /** Key/value key width */
    protected int kvKeyWidth = 10;

    protected String kvFormat = PrettyPrinter.makeKvFormat(this.kvKeyWidth);

    public PrettyPrinter() {
        this(100);
    }

    public PrettyPrinter(int width) {
        this.width = width;
    }

    /**
     * Set the wrap width (default 80 columns)
     *
     * @param wrapWidth new width (in characters) to wrap to
     * @return fluent interface
     */
    public PrettyPrinter wrapTo(int wrapWidth) {
        this.wrapWidth = wrapWidth;
        return this;
    }

    /**
     * Get the current wrap width
     *
     * @return the current wrap width
     */
    public int wrapTo() {
        return this.wrapWidth;
    }

    /**
     * Begin a new table with no header and adaptive column widths
     *
     * @return fluent interface
     */
    public PrettyPrinter table() {
        this.table = new Table();
        return this;
    }

    /**
     * Begin a new table with the specified headers and adaptive column widths
     *
     * @param titles Column titles
     * @return fluent interface
     */
    public PrettyPrinter table(String... titles) {
        this.table = new Table();
        for (String title : titles) {
            this.table.addColumn(title);
        }
        return this;
    }

    /**
     * Begin a new table with the specified format. The format is specified as a
     * sequence of values with {@link String}s defining column titles,
     * {@link Integer}s defining column widths, and {@link Alignment}s defining
     * column alignments. Widths and alignment specifiers should follow the
     * relevant column title. Specify a <em>negative</em> value to specify the
     * <em>maximum</em> width for a column (values will be truncated).
     *
     * <p>For example, to specify a table with two columns of width 10:</p>
     *
     * <code>printer.table("Column 1", 10, "Column 2", 10);</code>
     *
     * <p>A table with a column 30 characters wide and a right-aligned column 20
     * characters wide:</p>
     *
     * <code>printer.table("Column 1", 30, "Column 2", 20, Alignment.RIGHT);
     * </code>
     *
     * @param format format string, see description
     * @return fluent interface
     */
    public PrettyPrinter table(Object... format) {
        this.table = new Table();
        Column column = null;
        for (Object entry : format) {
            if (entry instanceof String) {
                column = this.table.addColumn((String)entry);
            } else if (entry instanceof Integer && column != null) {
                int width = (Integer) entry;
                if (width > 0) {
                    column.setWidth(width);
                } else if (width < 0) {
                    column.setMaxWidth(-width);
                }
            } else if (entry instanceof Alignment && column != null) {
                column.setAlignment((Alignment)entry);
            } else if (entry != null) {
                column = this.table.addColumn(entry.toString());
            }
        }
        return this;
    }

    /**
     * Set the column spacing for the current table. Default = 2
     *
     * @param spacing Column spacing in characters
     * @return fluent interface
     */
    public PrettyPrinter spacing(int spacing) {
        if (this.table == null) {
            this.table = new Table();
        }
        this.table.setColSpacing(spacing);
        return this;
    }

    /**
     * Print the current table header. The table header is automatically printed
     * before the first row if not explicitly specified by calling this method.
     *
     * @return fluent interface
     */
    public PrettyPrinter th() {
        return this.th(false);
    }

    private PrettyPrinter th(boolean onlyIfNeeded) {
        if (this.table == null) {
            this.table = new Table();
        }
        if (!onlyIfNeeded || this.table.addHeader) {
            this.table.headerAdded();
            this.hr('-');
            this.addLine(this.table);
            this.hr('-');
        }
        return this;
    }

    /**
     * Print a table row with the specified values. If more columns are
     * specified than exist in the table, then the table is automatically
     * expanded.
     *
     * @param args column values
     * @return fluent interface
     */
    public PrettyPrinter tr(Object... args) {
        this.th(true);
        this.addLine(this.table.addRow(this.width / args.length - 1, args));
        this.recalcWidth = true;
        return this;
    }

    /**
     * Adds a blank line to the output
     *
     * @return fluent interface
     */
    public PrettyPrinter add() {
        this.addLine("");
        return this;
    }

    /**
     * Adds a string line to the output
     *
     * @param string format string
     * @return fluent interface
     */
    public PrettyPrinter add(String string) {
        this.addLine(string);
        this.width = Math.max(this.width, string.length());
        return this;
    }

    /**
     * Adds a formatted line to the output
     *
     * @param format format string
     * @param args arguments
     *
     * @return fluent interface
     */
    public PrettyPrinter add(String format, Object... args) {
        String line = String.format(format, args);
        this.addLine(line);
        this.width = Math.max(this.width, line.length());
        return this;
    }

    /**
     * Add elements of the array to the output, one per line
     *
     * @param array Array of objects to print
     * @return fluent interface
     */
    public PrettyPrinter add(Object[] array) {
        return this.add(array, "%s");
    }

    /**
     * Add elements of the array to the output, one per line
     *
     * @param array Array of objects to print
     * @param format Format for each row
     * @return fluent interface
     */
    public PrettyPrinter add(Object[] array, String format) {
        for (Object element : array) {
            this.add(format, element);
        }

        return this;
    }

    /**
     * Add elements of the array to the output, one per line, with array indices
     *
     * @param array Array of objects to print
     * @return fluent interface
     */
    public PrettyPrinter addIndexed(Object[] array) {
        int indexWidth = String.valueOf(array.length - 1).length();
        String format = "[%" + indexWidth + "d] %s";
        for (int index = 0; index < array.length; index++) {
            this.add(format, index, array[index]);
        }

        return this;
    }

    /**
     * Add elements of the collection to the output, one per line, with indices
     *
     * @param c Collection of objects to print
     * @return fluent interface
     */
    public PrettyPrinter addWithIndices(Collection<?> c) {
        return this.addIndexed(c.toArray());
    }

    /**
     * Adds a pretty-printable object to the output, the object is responsible
     * for adding its own representation to this printer
     *
     * @param printable object to add
     * @return fluent interface
     */
    public PrettyPrinter add(IPrettyPrintable printable) {
        if (printable != null) {
            printable.print(this);
        }
        return this;
    }

    /**
     * Print a formatted representation of the specified {@link JElement} as a pretty
     * printed JSON output.
     *
     * @param json The element to print
     * @return fluent interface
     */
    public PrettyPrinter add(JElement json) {
        this.add(json.toJson());
        return this;
    }

    /**
     * Print a formatted representation of the specified {@link JsonElement} as a pretty
     * printed JSON output.
     *
     * @param json The element to print
     * @return fluent interface
     */
    public PrettyPrinter add(JsonElement json) {
        String raw = new GsonBuilder().setPrettyPrinting().create().toJson(json);
        for(String line : raw.split("\n")) {
            this.add(line);
        }

        return this;
    }

    /**
     * Print a formatted representation of the specified throwable with the
     * default indent (4)
     *
     * @param th Throwable to print
     * @return fluent interface
     */
    public PrettyPrinter add(Throwable th) {
        return this.add(th, 4);
    }

    /**
     * Print a formatted representation of the specified throwable with the
     * specified indent
     *
     * @param th Throwable to print
     * @param indent Indent size for stacktrace lines
     * @return fluent interface
     */
    public PrettyPrinter add(Throwable th, int indent) {
        this.add("%s: %s", th.getClass().getName(), th.getMessage());
        this.add(th.getStackTrace(), indent);

        return this.addCause(th.getCause(), indent);
    }

    private PrettyPrinter addCause(Throwable th, int indent) {
        while(th != null) {
            this.add("Caused by: %s: %s", th.getClass().getName(), th.getMessage());
            this.add(th.getStackTrace(), indent);
            th = th.getCause();
        }

        return this;
    }

    /**
     * Print a formatted representation of the specified stack trace with the
     * specified indent
     *
     * @param stackTrace stack trace to print
     * @param indent Indent size for stacktrace lines
     * @return fluent interface
     */
    public PrettyPrinter add(StackTraceElement[] stackTrace, int indent) {
        String margin = Strings.repeat(" ", indent);
        for (StackTraceElement st : stackTrace) {
            this.add("%s%s", margin, st);
        }
        return this;
    }

    /**
     * Adds the specified object to the output
     *
     * @param object object to add
     * @return fluent interface
     */
    public PrettyPrinter add(Object object) {
        return this.add(object, 0);
    }

    /**
     * Adds the specified object to the output
     *
     * @param object object to add
     * @param indent indent amount
     * @return fluent interface
     */
    public PrettyPrinter add(Object object, int indent) {
        String margin = Strings.repeat(" ", indent);
        return this.append(object, indent, margin);
    }

    public PrettyPrinter consume(Consumer<PrettyPrinter> consumer) {
        consumer.accept(this);
        return this;
    }

    private PrettyPrinter append(Object object, int indent, String margin) {
        if (object instanceof String) {
            return this.add("%s%s", margin, object);
        } else if (object instanceof Iterable) {
            for (Object entry : (Iterable<?>)object) {
                this.append(entry, indent, margin);
            }
            return this;
        } else if (object instanceof Map) {
            this.kvWidth(indent);
            return this.add((Map<?, ?>)object);
        } else if (object instanceof IPrettyPrintable) {
            return this.add((IPrettyPrintable)object);
        } else if (object instanceof Throwable) {
            return this.add((Throwable)object, indent);
        } else if (object.getClass().isArray()) {
            return this.add((Object[])object, margin + "%s");
        }
        return this.add("%s%s", margin, object);
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
    public PrettyPrinter addWrapped(String format, Object... args) {
        return this.addWrapped(this.wrapWidth, format, args);
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
    public PrettyPrinter addWrapped(int width, String format, Object... args) {
        String indent = "";
        String line = String.format(format, args).replace("\t", "    ");
        Matcher indentMatcher = Pattern.compile("^(\\s+)(.*)$").matcher(line);
        if (indentMatcher.matches()) {
            indent = indentMatcher.group(1);
        }

        try {
            for (String wrappedLine : this.getWrapped(width, line, indent)) {
                this.addLine(wrappedLine);
            }
        } catch (Exception ex) {
            this.add(line);
        }
        return this;
    }

    private List<String> getWrapped(int width, String line, String indent) {
        List<String> lines = new ArrayList<String>();

        while (line.length() > width) {
            int wrapPoint = line.lastIndexOf(' ', width);
            if (wrapPoint < 10) {
                wrapPoint = width;
            }
            String head = line.substring(0, wrapPoint);
            lines.add(head);
            line = indent + line.substring(wrapPoint + 1);
        }

        if (line.length() > 0) {
            lines.add(line);
        }

        return lines;
    }

    /**
     * Add a formatted key/value pair to the output
     *
     * @param key Key
     * @param format Value format
     * @param args Value args
     * @return fluent interface
     */
    public PrettyPrinter kv(String key, String format, Object... args) {
        return this.kv(key, String.format(format, args));
    }

    /**
     * Add a key/value pair to the output
     *
     * @param key Key
     * @param value Value
     * @return fluent interface
     */
    public PrettyPrinter kv(String key, Object value) {
        this.addLine(new KeyValue(key, value));
        return this.kvWidth(key.length());
    }

    /**
     * Set the minimum key display width
     *
     * @param width width to set
     * @return fluent
     */
    public PrettyPrinter kvWidth(int width) {
        if (width > this.kvKeyWidth) {
            this.kvKeyWidth = width;
            this.kvFormat = PrettyPrinter.makeKvFormat(width);
        }
        this.recalcWidth = true;
        return this;
    }

    /**
     * Add all values of the specified map to this printer as key/value pairs
     *
     * @param map Map with entries to add
     * @return fluent
     */
    public PrettyPrinter add(Map<?, ?> map) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey() == null ? "null" : entry.getKey().toString();
            this.kv(key, entry.getValue());
        }
        return this;
    }

    /**
     * Adds a horizontal rule to the output
     *
     * @return fluent interface
     */
    public PrettyPrinter hr() {
        return this.hr('*');
    }


    /**
     * Adds a horizontal rule of the specified char to the output
     *
     * @param ruleChar character to use for the horizontal rule
     * @return fluent interface
     */
    public PrettyPrinter hr(char ruleChar) {
        this.addLine(new HorizontalRule(ruleChar));
        return this;
    }

    /**
     * Center the last line added
     *
     * @return fluent interface
     */
    public PrettyPrinter center() {
        if (!this.lines.isEmpty()) {
            Object lastLine = this.lines.get(this.lines.size() - 1);
            if (lastLine instanceof String) {
                this.addLine(new CenteredText(this.lines.remove(this.lines.size() - 1)));
            }
        }
        return this;
    }

    public PrettyPrinter bigX() {
        new BigX().print(this);
        return this;
    }

    private void addLine(Object line) {
        if (line == null) {
            return;
        }
        this.lines.add(line);
        this.recalcWidth |= line instanceof PrettyPrinter.IVariableWidthEntry;
    }

    /**
     * Print this printer to the specified output
     *
     * @param stream stream to print to
     * @return fluent interface
     */
    public PrettyPrinter print(PrintStream stream) {
        this.updateWidth();
        this.printSpecial(stream, this.horizontalRule);
        for (Object line : this.lines) {
            if (line instanceof ISpecialEntry) {
                this.printSpecial(stream, (ISpecialEntry)line);
            } else {
                this.printString(stream, line.toString());
            }
        }
        this.printSpecial(stream, this.horizontalRule);
        return this;
    }

    private void printSpecial(PrintStream stream, ISpecialEntry line) {
        stream.printf("/*%s*/\n", line.toString());
    }

    private void printString(PrintStream stream, String string) {
        if (string != null) {
            stream.printf("/* %-" + this.width + "s */\n", string);
        }
    }

    /**
     * Write this printer to the specified logger at {@link Level#INFO}
     *
     * @param logger logger to log to
     * @return fluent interface
     */
    public PrettyPrinter log(Logger logger) {
        return this.log(logger, Level.INFO);
    }

    /**
     * Write this printer to the specified logger
     *
     * @param logger logger to log to
     * @param level log level
     * @return fluent interface
     */
    public PrettyPrinter log(Logger logger, Level level) {
        this.updateWidth();
        this.logSpecial(logger, level, this.horizontalRule);
        for (Object line : this.lines) {
            if (line instanceof ISpecialEntry) {
                this.logSpecial(logger, level, (ISpecialEntry)line);
            } else {
                this.logString(logger, level, line.toString());
            }
        }
        this.logSpecial(logger, level, this.horizontalRule);
        return this;
    }

    private void logSpecial(Logger logger, Level level, ISpecialEntry line) {
        LOGGER_LEVEL.getOrDefault(level, DEFAULT_LOGGER).accept(logger,
                String.format("/*%s*/", line.toString()));
    }

    private void logString(Logger logger, Level level, String line) {
        if (line != null) {
            LOGGER_LEVEL.getOrDefault(level, DEFAULT_LOGGER).accept(logger,
                    String.format("/* %-" + this.width + "s */", line));
        }
    }

    private void updateWidth() {
        if (this.recalcWidth) {
            this.recalcWidth = false;
            for (Object line : this.lines) {
                if (line instanceof IVariableWidthEntry) {
                    this.width = Math.min(4096, Math.max(this.width, ((IVariableWidthEntry)line).getWidth()));
                }
            }
        }
    }

    private static String makeKvFormat(int keyWidth) {
        return String.format("%%%ds : %%s", keyWidth);
    }

    /**
     * Convenience method, alternative to using <tt>Thread.dumpStack</tt> which
     * prints to stderr in pretty-printed format.
     */
    public static void dumpStack(Logger logger) {
        new PrettyPrinter().add(new Exception("Stack trace")).print(System.err);
    }

    /**
     * Convenience methods, pretty-prints the specified throwable to stderr
     *
     * @param th Throwable to log
     */
    public static void print(Logger logger, Throwable th) {
        new PrettyPrinter().add(th).print(System.err);
    }
}
