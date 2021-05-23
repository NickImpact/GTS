package net.impactdev.gts.common.discord

import com.google.common.collect.Lists
import com.google.gson.JsonObject
import net.impactdev.impactor.api.json.factory.JArray
import net.impactdev.impactor.api.json.factory.JObject
import java.awt.Color
import java.time.LocalDateTime

class Embed {
    private var fields: MutableList<Field> = Lists.newArrayList()
    private var title: String? = null
    private var timestamp: LocalDateTime? = null
    private var thumbnail: String? = null
    private var color: Int

    constructor(var1: Int) {
        color = var1
    }

    constructor(var1: Color) {
        color = var1.rgb and 16777215
    }

    private constructor(builder: Builder) {
        title = builder.title
        fields = builder.fields
        timestamp = builder.timestamp
        thumbnail = builder.thumbnail
        color = builder.color
    }

    fun addField(field: Field): Embed {
        fields.add(field)
        return this
    }

    val json: JsonObject
        get() {
            val json = JObject()
            json.add("title", title)
            json.add("color", color)
            json.add("timestamp", timestamp.toString())
            if (thumbnail != null) {
                json.add("thumbnail", JObject().add("url", thumbnail))
            }
            val fields = JArray()
            for (field in this.fields) {
                fields.add(field.json)
            }
            json.add("fields", fields)
            return json.toJson()
        }

    class Builder {
        var title: String? = null
        var color = 0
        var timestamp = LocalDateTime.now()
        var thumbnail: String? = null
        val fields: MutableList<Field> = Lists.newArrayList()
        fun title(title: String?): Builder {
            this.title = title
            return this
        }

        fun color(color: Int): Builder {
            this.color = color and 16777215
            return this
        }

        fun timestamp(timestamp: LocalDateTime): Builder {
            this.timestamp = timestamp
            return this
        }

        fun thumbnail(thumbnail: String?): Builder {
            this.thumbnail = thumbnail
            return this
        }

        fun field(field: Field): Builder {
            fields.add(field)
            return this
        }

        fun build(): Embed {
            return Embed(this)
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}