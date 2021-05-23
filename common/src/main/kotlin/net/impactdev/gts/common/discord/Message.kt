package net.impactdev.gts.common.discord

import com.google.common.collect.Lists
import net.impactdev.impactor.api.json.factory.JArray
import net.impactdev.impactor.api.json.factory.JObject
import java.io.DataOutputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

class Message(private val username: String?, private val avatarUrl: String?, option: DiscordOption) {
    private val embeds: MutableList<Embed?> = Lists.newArrayList()

    @Transient
    val webhooks: List<String?>?
    fun addEmbed(embed: Embed?): Message {
        embeds.add(embed)
        return this
    }

    @kotlin.Throws(Exception::class)
    fun send(url: String?): HttpsURLConnection {
        val connection = URL(url).openConnection() as HttpsURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("User-Agent", "GTS Minecraft Plugin")
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connection.doOutput = true
        val json = jsonString
        val dos = DataOutputStream(connection.outputStream)
        dos.write(json.toByteArray(StandardCharsets.UTF_8))
        dos.flush()
        dos.close()
        return connection
    }

    val jsonString: String
        get() {
            val json = JObject()
            if (username != null) {
                json.add("username", username)
            }
            if (avatarUrl != null) {
                json.add("avatar_url", avatarUrl)
            }
            if (!embeds.isEmpty()) {
                val embeds = JArray()
                for (embed in this.embeds) {
                    embeds.add(embed.getJson())
                }
                json.add("embeds", embeds)
            }
            return json.toJson().toString()
        }

    init {
        webhooks = option.webhookChannels
    }
}