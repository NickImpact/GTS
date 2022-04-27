/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.impactdev.gts.sponge.data;

import com.google.common.collect.Lists;
import io.leangen.geantyref.TypeToken;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class NBTTranslator implements DataTranslator<CompoundNBT> {

    private static final NBTTranslator instance = new NBTTranslator();
    private static final TypeToken<CompoundNBT> TOKEN = TypeToken.get(CompoundNBT.class);
    public static final String BOOLEAN_IDENTIFIER = "$Boolean";

    public static NBTTranslator getInstance() {
        return instance;
    }

    private NBTTranslator() { } // #NOPE

    private static CompoundNBT containerToCompound(final DataView container) {
        checkNotNull(container);
        CompoundNBT compound = new CompoundNBT();
        containerToCompound(container, compound);
        return compound;
    }

    private static void containerToCompound(final DataView container, final CompoundNBT compound) {
        // We don't need to get deep values since all nested DataViews will be found
        // from the instance of checks.
        checkNotNull(container);
        checkNotNull(compound);
        for (Map.Entry<DataQuery, Object> entry : container.values(false).entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey().asString('.');
            if (value instanceof DataView) {
                CompoundNBT inner = new CompoundNBT();
                containerToCompound(container.getView(entry.getKey()).get(), inner);
                compound.put(key, inner);
            } else if (value instanceof Boolean) {
                compound.put(key + BOOLEAN_IDENTIFIER, ByteNBT.valueOf(((Boolean) value) ? (byte) 1 : 0));
            } else {
                compound.put(key, getBaseFromObject(value));
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static INBT getBaseFromObject(Object value) {
        checkNotNull(value);
        if (value instanceof Boolean) {
            return ByteNBT.valueOf((Boolean) value ? (byte) 1 : 0);
        } else if (value instanceof Byte) {
            return ByteNBT.valueOf((Byte) value);
        } else if (value instanceof Short) {
            return ShortNBT.valueOf((Short) value);
        } else if (value instanceof Integer) {
            return IntNBT.valueOf((Integer) value);
        } else if (value instanceof Long) {
            return LongNBT.valueOf((Long) value);
        } else if (value instanceof Float) {
            return FloatNBT.valueOf((Float) value);
        } else if (value instanceof Double) {
            return DoubleNBT.valueOf((Double) value);
        } else if (value instanceof String) {
            return StringNBT.valueOf((String) value);
        } else if (value.getClass().isArray()) {
            if (value instanceof byte[]) {
                return new ByteArrayNBT((byte[]) value);
            } else if (value instanceof Byte[]) {
                byte[] array = new byte[((Byte[]) value).length];
                int counter = 0;
                for (Byte data : (Byte[]) value) {
                    array[counter++] = data;
                }
                return new ByteArrayNBT(array);
            } else if (value instanceof int[]) {
                return new IntArrayNBT((int[]) value);
            } else if (value instanceof Integer[]) {
                int[] array = new int[((Integer[]) value).length];
                int counter = 0;
                for (Integer data : (Integer[]) value) {
                    array[counter++] = data;
                }
                return new IntArrayNBT(array);
            } else if (value instanceof long[]) {
                return new LongArrayNBT((long[]) value);
            } else if (value instanceof Long[]) {
                long[] array = new long[((Long[]) value).length];
                int counter = 0;
                for (Long data : (Long[]) value) {
                    array[counter++] = data;
                }
                return new LongArrayNBT(array);
            }
        } else if (value instanceof List) {
            ListNBT list = new ListNBT();
            for (Object object : (List) value) {
                // Oh hey, we already have a translation already
                // since DataView only supports some primitive types anyways...
                list.add(getBaseFromObject(object));
            }
            return list;
        } else if (value instanceof Map) {
            CompoundNBT compound = new CompoundNBT();
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) value).entrySet()) {
                if (entry.getKey() instanceof DataQuery) {
                    if (entry.getValue() instanceof Boolean) {
                        compound.putBoolean(((DataQuery) entry.getKey()).asString('.') + BOOLEAN_IDENTIFIER, (Boolean) entry.getValue());
                    } else {
                        compound.put(((DataQuery) entry.getKey()).asString('.'), getBaseFromObject(entry.getValue()));
                    }
                } else if (entry.getKey() instanceof String) {
                    compound.put((String) entry.getKey(), getBaseFromObject(entry.getValue()));
                } else {
                    compound.put(entry.getKey().toString(), getBaseFromObject(entry.getValue()));
                }
            }
            return compound;
        } else if (value instanceof DataSerializable) {
            return containerToCompound(((DataSerializable) value).toContainer());
        } else if (value instanceof DataView) {
            return containerToCompound((DataView) value);
        }
        throw new IllegalArgumentException("Unable to translate object to NBTBase: " + value);
    }

    private static DataContainer getViewFromCompound(CompoundNBT compound) {
        checkNotNull(compound);
        DataContainer container = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);
        NBTTranslator.getInstance().addTo(compound, container);
        return container;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setInternal(INBT base, byte type, DataView view, String key) {
        checkNotNull(base);
        checkNotNull(view);
        checkNotNull(key);
        checkArgument(!key.isEmpty());
        checkArgument(type > Constants.NBT.TAG_END && type <= Constants.NBT.TAG_INT_ARRAY);
        switch (type) {
            case Constants.NBT.TAG_BYTE:
                if (key.contains(BOOLEAN_IDENTIFIER)) {
                    view.set(DataQuery.of(key.replace(BOOLEAN_IDENTIFIER, "")), (((ByteNBT) base).getAsByte() != 0));
                } else {
                    view.set(DataQuery.of(key), ((ByteNBT) base).getAsByte());
                }
                break;
            case Constants.NBT.TAG_SHORT:
                view.set(DataQuery.of(key), ((ShortNBT) base).getAsShort());
                break;
            case Constants.NBT.TAG_INT:
                view.set(DataQuery.of(key), ((IntNBT) base).getAsInt());
                break;
            case Constants.NBT.TAG_LONG:
                view.set(DataQuery.of(key), ((LongNBT) base).getAsLong());
                break;
            case Constants.NBT.TAG_FLOAT:
                view.set(DataQuery.of(key), ((FloatNBT) base).getAsFloat());
                break;
            case Constants.NBT.TAG_DOUBLE:
                view.set(DataQuery.of(key), ((DoubleNBT) base).getAsDouble());
                break;
            case Constants.NBT.TAG_BYTE_ARRAY:
                view.set(DataQuery.of(key), ((ByteArrayNBT) base).getAsByteArray());
                break;
            case Constants.NBT.TAG_STRING:
                view.set(DataQuery.of(key), base.getAsString());
                break;
            case Constants.NBT.TAG_LIST:
                ListNBT list = (ListNBT) base;
                byte listType = list.getElementType();
                int count = list.size();
                List objectList = Lists.newArrayListWithCapacity(count);
                for (INBT inbt : list) {
                    objectList.add(fromTagBase(inbt, listType));
                }
                view.set(DataQuery.of(key), objectList);
                break;
            case Constants.NBT.TAG_COMPOUND:
                DataView internalView = view.createView(DataQuery.of(key));
                CompoundNBT compound = (CompoundNBT) base;
                for (String internalKey : compound.getAllKeys()) {
                    INBT internalBase = compound.get(internalKey);
                    byte internalType = internalBase.getId();
                    // Basically.... more recursion.
                    // Reasoning: This avoids creating a new DataContainer which would
                    // then be copied in to the owning DataView anyways. We can internally
                    // set the actual data directly to the child view instead.
                    setInternal(internalBase, internalType, internalView, internalKey);
                }
                break;
            case Constants.NBT.TAG_INT_ARRAY:
                view.set(DataQuery.of(key), ((IntArrayNBT) base).getAsIntArray());
                break;
            case Constants.NBT.TAG_LONG_ARRAY:
                view.set(DataQuery.of(key), ((LongArrayNBT) base).getAsLongArray());
                break;
            default:
                throw new IllegalArgumentException("Unknown NBT type " + type);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object fromTagBase(INBT base, byte type) {
        switch (type) {
            case Constants.NBT.TAG_BYTE:
                return ((ByteNBT) base).getAsByte();
            case Constants.NBT.TAG_SHORT:
                return (((ShortNBT) base)).getAsShort();
            case Constants.NBT.TAG_INT:
                return ((IntNBT) base).getAsInt();
            case Constants.NBT.TAG_LONG:
                return ((LongNBT) base).getAsLong();
            case Constants.NBT.TAG_FLOAT:
                return ((FloatNBT) base).getAsFloat();
            case Constants.NBT.TAG_DOUBLE:
                return ((DoubleNBT) base).getAsDouble();
            case Constants.NBT.TAG_BYTE_ARRAY:
                return ((ByteArrayNBT) base).getAsByteArray();
            case Constants.NBT.TAG_STRING:
                return base.getAsString();
            case Constants.NBT.TAG_LIST:
                ListNBT list = (ListNBT) base;
                byte listType = list.getElementType();
                int count = list.size();
                List objectList = Lists.newArrayListWithCapacity(count);
                for (INBT inbt : list) {
                    objectList.add(fromTagBase(inbt, listType));
                }
                return objectList;
            case Constants.NBT.TAG_COMPOUND:
                return getViewFromCompound((CompoundNBT) base);
            case Constants.NBT.TAG_INT_ARRAY:
                return ((IntArrayNBT) base).getAsIntArray();
            case Constants.NBT.TAG_LONG_ARRAY:
                return ((LongArrayNBT) base).getAsLongArray();
            default :
                return null;
        }
    }

    public CompoundNBT translateData(DataView container) {
        return NBTTranslator.containerToCompound(container);
    }

    public void translateContainerToData(CompoundNBT node, DataView container) {
        NBTTranslator.containerToCompound(container, node);
    }

    public DataContainer translateFrom(CompoundNBT node) {
        return NBTTranslator.getViewFromCompound(node);
    }


    @Override
    public TypeToken<CompoundNBT> token() {
        return TOKEN;
    }

    @Override
    public CompoundNBT translate(DataView view) throws InvalidDataException {
        return containerToCompound(view);
    }

    @Override
    public DataContainer translate(CompoundNBT obj) throws InvalidDataException {
        return getViewFromCompound(obj);
    }

    @Override
    public DataView addTo(CompoundNBT compound, DataView container) {
        for (String key : compound.getAllKeys()) {
            INBT base = compound.get(key);
            byte type = base.getId();
            setInternal(base, type, container, key); // gotta love recursion
        }
        return container;
    }

}
