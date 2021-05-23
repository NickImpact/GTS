/* This design originates from Nucleus, which is licensed under MIT */
package net.impactdev.gts.gradle.tasks

import com.google.gson.Gson
import com.google.gson.JsonObject
import net.impactdev.gts.gradle.enums.ReleaseLevel
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI
import java.net.URL
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import javax.net.ssl.HttpsURLConnection

class UploadToOre : DefaultTask() {
    private val multipartBoundary = "somegibberishusedasaboundary"
    var apiKey: String? = null
    var force = false
    var notes: Provider<String>? = null
    var level: ReleaseLevel? = null
    var file: File? = null
    var pluginID = ""
    @TaskAction
    fun run() {
        if (force || !level!!.isSnapshot) {
            if (apiKey != null && file != null) {
                try {
                    val auth = URL(LINK_BUILDER.apply("v2/authenticate"))
                    val upload = URI(LINK_BUILDER.apply("v2/projects/" + pluginID + "/versions"))
                    val destroy = URL(LINK_BUILDER.apply("v2/sessions/current"))
                    val gson = Gson()
                    logger.info("Starting Upload...")
                    logger.info("Obtaining authorization...")
                    val connection = auth.openConnection() as HttpsURLConnection
                    val key: String
                    try {
                        connection.requestMethod = "POST"
                        connection.setRequestProperty("Authorization", "OreApi apikey=" + apiKey)
                        connection.setRequestProperty("Content-Type", ContentType.APPLICATION_JSON.mimeType)
                        connection.setRequestProperty("User-Agent", "GTS/Gradle")
                        val status = connection.responseCode
                        if (status != 200) {
                            throw Exception("Error getting session: $status")
                        }
                        val response = returnStringFromInputStream(connection.inputStream)
                        val json = gson.fromJson(response, JsonObject::class.java)
                        key = json["session"].asString
                    } finally {
                        connection.disconnect()
                    }
                    val json = gson.toJson(FileUploadData(notes!!.get()))
                    val post = HttpPost(upload)
                    post.entity = MultipartEntityBuilder.create()
                        .addTextBody("plugin-info", json, ContentType.APPLICATION_JSON)
                        .addBinaryBody("plugin-file", file)
                        .setBoundary(multipartBoundary)
                        .build()
                    post.addHeader("Authorization", "OreApi session=$key")
                    post.addHeader(
                        "Content-Type", ContentType.MULTIPART_FORM_DATA
                            .withParameters(BasicNameValuePair("boundary", multipartBoundary))
                            .toString()
                    )
                    post.addHeader("Accept", ContentType.APPLICATION_JSON.mimeType)
                    post.addHeader("User-Agent", "GTS/Gradle")
                    val client = HttpClients.createDefault().execute(post)
                    val status = client.statusLine.statusCode
                    if (status != 201) {
                        destroySession(destroy, key)
                        logger.error(returnStringFromInputStream(client.entity.content))
                        throw GradleException(
                            """
    Failed to upload:
    Status Code: $status
    Status Phrase: ${client.statusLine.reasonPhrase}
    """.trimIndent()
                        )
                    }
                    val result =
                        gson.fromJson(returnStringFromInputStream(client.entity.content), JsonObject::class.java)
                    val fileInfoName = result.getAsJsonObject("file_info")["name"].asString
                    logger.info("Successfully uploaded: $fileInfoName")
                    logger.debug(result.toString())
                    destroySession(destroy, key)
                } catch (e: Exception) {
                    throw GradleException(e.message)
                }
            }
        } else {
            logger.info("File: " + file!!.name)
            logger.info("Release Level: " + level)
            logger.info("API Key: " + Optional.ofNullable(apiKey).orElse("???"))
        }
    }

    @Throws(Exception::class)
    private fun destroySession(url: URL, key: String) {
        val post = url.openConnection() as HttpsURLConnection
        try {
            post.requestMethod = "DELETE"
            post.setRequestProperty("Authorization", "OreApi session=$key")
            post.setRequestProperty("User-Agent", "GTS/Gradle")
            val status = post.responseCode
            logger.info("Deleted session: $status")
        } finally {
            post.disconnect()
        }
    }

    private fun returnStringFromInputStream(`in`: InputStream): String {
        return BufferedReader(InputStreamReader(`in`)).lines().collect(Collectors.joining(System.lineSeparator()))
    }

    private class FileUploadData(private val description: String) {
        private val create_forum_post = true
    }

    companion object {
        private val LINK_BUILDER = Function { `in`: String -> "https://ore.spongepowered.org/api/$`in`" }
    }
}