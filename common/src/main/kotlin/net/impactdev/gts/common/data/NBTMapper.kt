package net.impactdev.gts.common.data

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.impactdev.gts.api.util.PrettyPrinter
import net.impactdev.gts.common.plugin.GTSPlugin
import net.impactdev.impactor.api.json.factory.JArray
import net.impactdev.impactor.api.json.factory.JObject
import net.minecraft.nbt.*
import net.minecraftforge.common.util.Constants
import java.util.*
import java.util.function.Function

class NBTMapper {
    @JvmOverloads
    fun from(nbt: NBTTagCompound, print: Boolean = false): JObject {
        val result = JObject()
        val test = PrettyPrinter(80)
        test.add("NBT Mapping Track - Write").center()
        test.hr()
        for (key in nbt.keySet) {
            val id = nbt.getTagId(key).toInt()
            test.kv(key, id)
            when (id) {
                Constants.NBT.TAG_BYTE -> this.append(result, key, nbt.getByte(key))
                Constants.NBT.TAG_SHORT -> this.append(result, key, nbt.getShort(key))
                Constants.NBT.TAG_INT -> this.append(result, key, nbt.getInteger(key))
                Constants.NBT.TAG_LONG -> this.append(result, key, nbt.getLong(key))
                Constants.NBT.TAG_FLOAT -> this.append(result, key, nbt.getFloat(key))
                Constants.NBT.TAG_DOUBLE -> this.append(result, key, nbt.getDouble(key))
                Constants.NBT.TAG_BYTE_ARRAY -> this.append(result, key, nbt.getByteArray(key))
                Constants.NBT.TAG_STRING -> this.append(result, key, nbt.getString(key))
                Constants.NBT.TAG_LIST -> this.append(result, key, nbt.getTag(key) as NBTTagList)
                Constants.NBT.TAG_COMPOUND -> this.append(result, key, nbt.getCompoundTag(key))
                Constants.NBT.TAG_INT_ARRAY -> this.append(result, key, nbt.getIntArray(key))
                Constants.NBT.TAG_LONG_ARRAY -> try {
                    val data = NBTTagLongArray::class.java.getDeclaredField("data")
                    data.isAccessible = true
                    this.append(result, key, data[nbt.getTag(key)] as LongArray)
                } catch (e: Exception) {
                    throw RuntimeException("Failed to read long array", e)
                }
            }
        }
        if (print) {
            test.add()
            test.add("Result:")
            test.add(result.toJson())
            test.log(GTSPlugin.Companion.getInstance().getPluginLogger(), PrettyPrinter.Level.DEBUG)
        }
        return result
    }

    private fun append(json: JObject, key: String, value: Byte) {
        getLowestParent(json, key).add(key, Mapper.BYTE.getApplier().apply(value))
    }

    private fun append(json: JObject, key: String, value: Short) {
        getLowestParent(json, key).add(key, Mapper.SHORT.getApplier().apply(value))
    }

    private fun append(json: JObject, key: String, value: Int) {
        getLowestParent(json, key).add(key, Mapper.INTEGER.getApplier().apply(value))
    }

    private fun append(json: JObject, key: String, value: Long) {
        getLowestParent(json, key).add(key, Mapper.LONG.getApplier().apply(value))
    }

    private fun append(json: JObject, key: String, value: Float) {
        getLowestParent(json, key).add(key, Mapper.FLOAT.getApplier().apply(value))
    }

    private fun append(json: JObject, key: String, value: Double) {
        getLowestParent(json, key).add(key, Mapper.DOUBLE.getApplier().apply(value))
    }

    private fun append(json: JObject, key: String, value: ByteArray) {
        getLowestParent(json, key).add(key, Mapper.BYTE_ARRAY.getApplier().apply(value))
    }

    private fun append(json: JObject, key: String, value: String) {
        getLowestParent(json, key).add(key, Mapper.STRING.getApplier().apply(value))
    }

    private fun append(json: JObject, key: String, value: NBTTagList) {
        getLowestParent(json, key).add(key, Mapper.LIST.getApplier().apply(value))
    }

    private fun append(json: JObject, key: String, value: NBTTagCompound) {
        getLowestParent(json, key).add(key, Mapper.COMPOUND.getApplier().apply(value))
    }

    private fun append(json: JObject, key: String, value: IntArray) {
        getLowestParent(json, key).add(key, Mapper.INT_ARRAY.getApplier().apply(value))
    }

    private fun append(json: JObject, key: String, value: LongArray) {
        getLowestParent(json, key).add(key, Mapper.INT_ARRAY.getApplier().apply(value))
    }

    fun read(json: JsonObject): NBTTagCompound {
        val nbt = NBTTagCompound()
        for ((key, value1) in json.entrySet()) {
            val data = value1.asJsonObject
            val type = data["type"].asString
            val value = data["value"]
            if (type == "compound") {
                nbt.setTag(key, read(value.asJsonObject))
            } else if (type == "list") {
                nbt.setTag(key, `read$list`(value.asJsonArray))
            } else {
                when (type) {
                    "byte" -> nbt.setByte(key, value.asByte)
                    "short" -> nbt.setShort(key, value.asShort)
                    "int" -> nbt.setInteger(key, value.asInt)
                    "long" -> nbt.setLong(key, value.asLong)
                    "float" -> nbt.setFloat(key, value.asFloat)
                    "double" -> nbt.setDouble(key, value.asDouble)
                    "byte[]" -> {
                        val array = value.asJsonArray
                        val values = ByteArray(array.size())
                        var index = 0
                        for (element in array) {
                            values[index++] = element.asByte
                        }
                        nbt.setByteArray(key, values)
                    }
                    "string" -> nbt.setString(key, value.asString)
                    "int[]" -> {
                        val array2 = value.asJsonArray
                        val values2 = IntArray(array2.size())
                        var i2 = 0
                        for (element in array2) {
                            values2[i2++] = element.asInt
                        }
                        nbt.setIntArray(key, values2)
                    }
                    "long[]" -> {
                        val array3 = value.asJsonArray
                        val values3 = LongArray(array3.size())
                        var i3 = 0
                        for (element in array3) {
                            values3[i3++] = element.asLong
                        }
                        nbt.setTag(key, NBTTagLongArray(values3))
                    }
                }
            }
        }
        return nbt
    }

    private fun `read$list`(array: JsonArray): NBTTagList {
        val result = NBTTagList()
        for (element in array) {
            val `object` = element.asJsonObject
            val type = `object`["type"].asString
            val value = `object`["value"]
            if (type == "compound") {
                result.appendTag(read(value.asJsonObject))
            } else if (type == "list") {
                result.appendTag(`read$list`(value.asJsonArray))
            } else {
                when (type) {
                    "byte" -> result.appendTag(NBTTagByte(value.asByte))
                    "short" -> result.appendTag(NBTTagShort(value.asShort))
                    "int" -> result.appendTag(NBTTagInt(value.asInt))
                    "long" -> result.appendTag(NBTTagLong(value.asLong))
                    "float" -> result.appendTag(NBTTagFloat(value.asFloat))
                    "double" -> result.appendTag(NBTTagDouble(value.asDouble))
                    "byte[]" -> {
                        val a = value.asJsonArray
                        val values = ByteArray(a.size())
                        var index = 0
                        for (e in a) {
                            values[index++] = e.asByte
                        }
                        result.appendTag(NBTTagByteArray(values))
                    }
                    "string" -> result.appendTag(NBTTagString(value.asString))
                    "int[]" -> {
                        val array2 = value.asJsonArray
                        val values2 = IntArray(array2.size())
                        var i2 = 0
                        for (e in array2) {
                            values2[i2++] = e.asInt
                        }
                        result.appendTag(NBTTagIntArray(values2))
                    }
                    "long[]" -> {
                        val array3 = value.asJsonArray
                        val values3 = LongArray(array3.size())
                        var i3 = 0
                        for (e in array3) {
                            values3[i3++] = e.asLong
                        }
                        result.appendTag(NBTTagLongArray(values3))
                    }
                }
            }
        }
        return result
    }

    private fun getLowestParent(json: JObject, key: String): JsonObject {
        var result = json.toJson()
        val query = key.split("\\.".toRegex()).toTypedArray()
        var index = 0
        while (index < query.size - 2) {
            if (result.isJsonObject) {
                val `object` = result.asJsonObject
                if (`object`.has(query[index])) {
                    result = Optional.ofNullable(`object`[query[index]])
                        .filter { obj: JsonElement -> obj.isJsonObject }
                        .map { obj: JsonElement -> obj.asJsonObject }
                        .orElseThrow { RuntimeException("Unable to find valid parent for key: $key") }
                } else {
                    `object`.add(query[index], JsonObject().also { result = it })
                }
            }
            index++
        }
        return result
    }

    private enum class Mapper(private val applier: Function<Any, JObject>) {
        BYTE(Function { value: Any ->
            JObject().add("type", "byte").add(
                "value", verify(
                    Byte::class.java, value
                )
            )
        }),
        SHORT(Function { value: Any ->
            JObject().add("type", "short").add(
                "value", verify(
                    Short::class.java, value
                )
            )
        }),
        INTEGER(Function { value: Any ->
            JObject().add("type", "int").add(
                "value", verify(
                    Int::class.java, value
                )
            )
        }),
        LONG(Function { value: Any ->
            JObject().add("type", "long").add(
                "value", verify(
                    Long::class.java, value
                )
            )
        }),
        FLOAT(Function { value: Any ->
            JObject().add("type", "float").add(
                "value", verify(
                    Float::class.java, value
                )
            )
        }),
        DOUBLE(Function { value: Any ->
            JObject().add("type", "double").add(
                "value", verify(
                    Double::class.java, value
                )
            )
        }),
        BYTE_ARRAY(Function { value: Any ->
            JObject().add("type", "byte[]").add("value", byteArray(value as ByteArray))
        }),
        STRING(
            Function { value: Any ->
                JObject().add("type", "string").add(
                    "value", verify(
                        String::class.java, value
                    )
                )
            }),
        LIST(Function { value: Any ->
            val array = JArray()
            val list = value as NBTTagList
            val listType = list.tagType.toByte()
            val count = list.tagCount()
            for (i in 0 until count) {
                array.add(fromTagBase(list[i], listType))
            }
            JObject().add("type", "list").add("value", array)
        }),
        COMPOUND(Function { value: Any ->
            JObject().add("type", "compound").add("value", NBTMapper().from(value as NBTTagCompound, false))
        }),
        INT_ARRAY(
            Function { value: Any -> JObject().add("type", "int[]").add("value", intArray(value as IntArray)) }),
        LONG_ARRAY(
            Function { value: Any -> JObject().add("type", "long[]").add("value", longArray(value as LongArray)) });

        fun getApplier(): Function<Any, JsonObject> {
            return applier.andThen { obj: JObject -> obj.toJson() }
        }

        companion object {
            @kotlin.Throws(RuntimeException::class)
            private fun <T> verify(expected: Class<T>, value: Any): T {
                return Optional.ofNullable(value)
                    .filter { x: Any -> expected.isAssignableFrom(x.javaClass) }
                    .map { obj: Any? -> expected.cast(obj) }
                    .orElseThrow {
                        RuntimeException(
                            String.format(
                                "Invalid Typing (%s vs %s)",
                                expected.name,
                                value.javaClass.name
                            )
                        )
                    }
            }

            @kotlin.Throws(RuntimeException::class)
            private fun byteArray(value: ByteArray): JArray {
                val array = JArray()
                for (b in value) {
                    array.add(b)
                }
                return array
            }

            @kotlin.Throws(RuntimeException::class)
            private fun intArray(value: IntArray): JArray {
                val array = JArray()
                for (i in value) {
                    array.add(i)
                }
                return array
            }

            @kotlin.Throws(RuntimeException::class)
            private fun longArray(value: LongArray): JArray {
                val array = JArray()
                for (i in value) {
                    array.add(i)
                }
                return array
            }

            private fun fromTagBase(base: NBTBase, type: Byte): JObject? {
                return when (type) {
                    Constants.NBT.TAG_BYTE -> BYTE.applier.apply((base as NBTTagByte).byte)
                    Constants.NBT.TAG_SHORT -> SHORT.applier.apply((base as NBTTagShort).short)
                    Constants.NBT.TAG_INT -> INTEGER.applier.apply((base as NBTTagInt).int)
                    Constants.NBT.TAG_LONG -> LONG.applier.apply((base as NBTTagLong).int)
                    Constants.NBT.TAG_FLOAT -> FLOAT.applier.apply((base as NBTTagFloat).float)
                    Constants.NBT.TAG_DOUBLE -> DOUBLE.applier.apply((base as NBTTagDouble).double)
                    Constants.NBT.TAG_BYTE_ARRAY -> BYTE_ARRAY.applier.apply((base as NBTTagByteArray).byteArray)
                    Constants.NBT.TAG_STRING -> STRING.applier.apply((base as NBTTagString).string)
                    Constants.NBT.TAG_LIST -> LIST.applier.apply(base)
                    Constants.NBT.TAG_COMPOUND -> COMPOUND.applier.apply(base)
                    Constants.NBT.TAG_INT_ARRAY -> INT_ARRAY.applier.apply((base as NBTTagIntArray).intArray)
                    Constants.NBT.TAG_LONG_ARRAY -> {
                        return try {
                            val data = NBTTagLongArray::class.java.getDeclaredField("data")
                            data.isAccessible = true
                            LONG_ARRAY.applier.apply(data[base])
                        } catch (e: Exception) {
                            throw RuntimeException("Failed to read long array", e)
                        }
                        null
                    }
                    else -> null
                }
            }
        }
    }
}