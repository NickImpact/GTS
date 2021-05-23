package net.impactdev.gts.common.discord

import net.impactdev.impactor.api.json.factory.JObject

class Field(private val name: String?, private val value: String?, private val inline: Boolean) {
    val json: JObject
        get() {
            val json = JObject()
            if (name != null) {
                json.add("name", name)
            }
            if (value != null) {
                json.add("value", value)
            }
            return json.consume { d: JObject ->
                if (inline) {
                    d.add("inline", true)
                }
            }
        }
}