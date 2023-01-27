package net.impactdev.gts.util.nbt;

import net.impactdev.impactor.api.utility.printing.PrettyPrinter;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.ShortBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;

import java.util.Arrays;

public final class NBTPrinter {

    public static void print(PrettyPrinter printer, CompoundBinaryTag nbt, int indent) {
        printer.add("NBT: {", indent);
        nbt.forEach(entry -> printCompoundElement(printer, entry.getKey(), entry.getValue(), indent + 2));
        printer.add("}", indent);
    }

    private static void printCompoundElement(PrettyPrinter printer, String key, BinaryTag tag, int indent) {
        if(tag instanceof ByteBinaryTag) {
            printer.add(String.format("%s: %d", key, ((ByteBinaryTag) tag).value()), indent);
        } else if(tag instanceof ShortBinaryTag) {
            printer.add(String.format("%s: %d", key, ((ShortBinaryTag) tag).value()), indent);
        } else if(tag instanceof IntBinaryTag) {
            printer.add(String.format("%s: %d", key, ((IntBinaryTag) tag).value()), indent);
        } else if(tag instanceof LongBinaryTag) {
            printer.add(String.format("%s: %d", key, ((LongBinaryTag) tag).value()), indent);
        } else if(tag instanceof FloatBinaryTag) {
            printer.add(String.format("%s: %f", key, ((FloatBinaryTag) tag).value()), indent);
        } else if(tag instanceof DoubleBinaryTag) {
            printer.add(String.format("%s: %f", key, ((DoubleBinaryTag) tag).value()), indent);
        } else if(tag instanceof ByteArrayBinaryTag) {
            printer.add(String.format("%s: %s", key, Arrays.toString(((ByteArrayBinaryTag) tag).value())), indent);
        } else if(tag instanceof StringBinaryTag) {
            printer.add(String.format("%s: %s", key, ((StringBinaryTag) tag).value()), indent);
        } else if(tag instanceof IntArrayBinaryTag) {
            printer.add(String.format("%s: %s", key, Arrays.toString(((IntArrayBinaryTag) tag).value())), indent);
        } else if(tag instanceof LongArrayBinaryTag) {
            printer.add(String.format("%s: %s", key, Arrays.toString(((LongArrayBinaryTag) tag).value())), indent);
        } else if(tag instanceof ListBinaryTag) {
            ListBinaryTag list = (ListBinaryTag) tag;

            printer.add(String.format("%s: [", key), indent);
            list.forEach(child -> printListElement(printer, child, indent + 2));
            printer.add("]", indent);
        } else if(tag instanceof CompoundBinaryTag) {
            CompoundBinaryTag compound = (CompoundBinaryTag) tag;

            printer.add(String.format("%s: {", key), indent);
            compound.forEach(entry -> printCompoundElement(printer, entry.getKey(), entry.getValue(), indent + 2));
            printer.add("}", indent);
        }
    }

    private static void printListElement(PrettyPrinter printer, BinaryTag tag, int indent) {
        if(tag instanceof ByteBinaryTag) {
            printer.add(String.format("%d", ((ByteBinaryTag) tag).value()), indent);
        } else if(tag instanceof ShortBinaryTag) {
            printer.add(String.format("%d", ((ShortBinaryTag) tag).value()), indent);
        } else if(tag instanceof IntBinaryTag) {
            printer.add(String.format("%d", ((IntBinaryTag) tag).value()), indent);
        } else if(tag instanceof LongBinaryTag) {
            printer.add(String.format("%d", ((LongBinaryTag) tag).value()), indent);
        } else if(tag instanceof FloatBinaryTag) {
            printer.add(String.format("%f", ((FloatBinaryTag) tag).value()), indent);
        } else if(tag instanceof DoubleBinaryTag) {
            printer.add(String.format("%f", ((DoubleBinaryTag) tag).value()), indent);
        } else if(tag instanceof ByteArrayBinaryTag) {
            printer.add(String.format("%s", Arrays.toString(((ByteArrayBinaryTag) tag).value())), indent);
        } else if(tag instanceof StringBinaryTag) {
            printer.add(String.format("%s", ((StringBinaryTag) tag).value()), indent);
        } else if(tag instanceof IntArrayBinaryTag) {
            printer.add(String.format("%s", Arrays.toString(((IntArrayBinaryTag) tag).value())), indent);
        } else if(tag instanceof LongArrayBinaryTag) {
            printer.add(String.format("%s", Arrays.toString(((LongArrayBinaryTag) tag).value())), indent);
        } else if(tag instanceof ListBinaryTag) {
            ListBinaryTag list = (ListBinaryTag) tag;

            printer.add("[", indent);
            list.forEach(child -> printListElement(printer, child, indent + 2));
            printer.add("]", indent);
        } else if(tag instanceof CompoundBinaryTag) {
            CompoundBinaryTag compound = (CompoundBinaryTag) tag;

            printer.add("{", indent);
            compound.forEach(entry -> printCompoundElement(printer, entry.getKey(), entry.getValue(), indent + 2));
            printer.add("}", indent);
        }
    }

}
