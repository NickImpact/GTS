package net.impactdev.gts.gradle.tasks

import net.impactdev.gts.gradle.enums.ReleaseLevel
import net.ranktw.DiscordWebHooks.DiscordEmbed
import net.ranktw.DiscordWebHooks.DiscordMessage
import net.ranktw.DiscordWebHooks.DiscordWebhook
import net.ranktw.DiscordWebHooks.embed.FieldEmbed
import net.ranktw.DiscordWebHooks.embed.ImageEmbed
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.awt.Color
import java.io.File
import java.util.*

class PostToDiscord : DefaultTask() {
    var webhookID: String? = null
    var webhookToken: String? = null
    var memberRole: String? = null
    var content: String? = null
    var version: String? = null
    var level: ReleaseLevel? = null
    var files: List<File>? = null
    var force = false
    @TaskAction
    fun run() {
        if (force || level != ReleaseLevel.SNAPSHOT) {
            if (webhookID != null && webhookToken != null && files != null) {
                val url = LINK_BUILDER.replace("{{webhook:id}}", webhookID!!)
                    .replace("{{webhook:token}}", webhookToken!!)
                val discord = DiscordWebhook(url)
                val message = DiscordMessage.Builder()
                    .withUsername("GTS Release Notifier")
                    .withAvatarURL("https://cdn.discordapp.com/attachments/625206927152119818/785034021373607976/image0.jpg")
                    .withContent(memberRole + " " + content.replace("{{content:version}}", version))
                    .build()
                val embed = DiscordEmbed.Builder()
                    .withColor(Color.CYAN)
                    .withTitle("Downloads + Changelog Information")
                    .withDescription("Click any of the links below to view changelog information for this release!")
                    .withField(
                        FieldEmbed(
                            "Ore",
                            "[Click Me](https://ore.spongepowered.org/NickImpact/GTS/versions/" + version + ")",
                            true
                        )
                    )
                    .withField(
                        FieldEmbed(
                            "Github",
                            "[Click Me](https://github.com/NickImpact/GTS/releases/tag/" + version + ")",
                            true
                        )
                    )
                    .withImage(GifSelector.createFor(level))
                    .build()
                message.addEmbeds(embed)
                discord.sendMessage(message)
                for (file in files!!) {
                    try {
                        Thread.sleep(3000)
                        val wrapper: MutableList<File> = ArrayList()
                        wrapper.add(file)
                        val out = DiscordMessage.Builder()
                            .withUsername("GTS Release Notifier")
                            .withAvatarURL("https://cdn.discordapp.com/attachments/625206927152119818/785034021373607976/image0.jpg")
                            .build()
                        discord.sendMessage(out, wrapper)
                    } catch (e: Exception) {
                        throw RuntimeException("Failure during file upload", e)
                    }
                }
            }
        }
    }

    private enum class GifSelector(
        private val level: ReleaseLevel,
        private val url: String,
        private val height: Int,
        private val width: Int
    ) {
        Patch(
            ReleaseLevel.PATCH,
            "https://pa1.narvii.com/6156/15a7f4cf7cd0b25244e4ba415e840f689da609d2_hq.gif",
            56,
            100
        ),
        Minor(
            ReleaseLevel.MINOR,
            "https://media.tenor.com/images/d8013e90642bfd34a7510810336130a2/tenor.gif",
            100,
            100
        ),
        Major(ReleaseLevel.MAJOR, "https://thumbs.gfycat.com/DentalFemaleGlassfrog-max-14mb.gif", 56, 100);

        companion object {
            fun createFor(level: ReleaseLevel?): ImageEmbed {
                val selector = Arrays.stream(values())
                    .filter { gs: GifSelector -> gs.level == level }
                    .findAny()
                    .orElse(Patch)
                return ImageEmbed(selector.url, selector.height, selector.width)
            }
        }
    }

    companion object {
        private const val LINK_BUILDER = "https://discordapp.com/api/webhooks/{{webhook:id}}/{{webhook:token}}"
    }
}