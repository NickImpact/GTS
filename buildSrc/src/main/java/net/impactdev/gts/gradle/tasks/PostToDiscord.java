package net.impactdev.gts.gradle.tasks;

import net.impactdev.gts.gradle.enums.ReleaseLevel;
import net.ranktw.DiscordWebHooks.DiscordEmbed;
import net.ranktw.DiscordWebHooks.DiscordMessage;
import net.ranktw.DiscordWebHooks.DiscordWebhook;
import net.ranktw.DiscordWebHooks.embed.FieldEmbed;
import net.ranktw.DiscordWebHooks.embed.ImageEmbed;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostToDiscord extends DefaultTask {

    private static final String LINK_BUILDER = "https://discordapp.com/api/webhooks/{{webhook:id}}/{{webhook:token}}";

    public String webhookID;
    public String webhookToken;
    public String memberRole;

    public String content;
    public String version;
    public ReleaseLevel level;
    public List<File> files;

    public boolean force;

    @TaskAction
    public void run() {
        if(this.force || this.level != ReleaseLevel.SNAPSHOT) {
            if(this.webhookID != null && this.webhookToken != null && this.files != null) {
                String url = LINK_BUILDER.replace("{{webhook:id}}", this.webhookID)
                        .replace("{{webhook:token}}", this.webhookToken);

                DiscordWebhook discord = new DiscordWebhook(url);

                DiscordMessage message = new DiscordMessage.Builder()
                        .withUsername("GTS Release Notifier")
                        .withAvatarURL("https://cdn.discordapp.com/attachments/625206927152119818/785034021373607976/image0.jpg")
                        .withContent(this.memberRole + " " + this.content.replace("{{content:version}}", this.version))
                        .build();

                DiscordEmbed embed = new DiscordEmbed.Builder()
                        .withColor(Color.CYAN)
                        .withTitle("Downloads + Changelog Information")
                        .withDescription("Click any of the links below to view changelog information for this release!")
                        .withField(new FieldEmbed("Ore", "[Click Me](https://ore.spongepowered.org/NickImpact/GTS/versions/" + this.version + ")", true))
                        .withField(new FieldEmbed("Github", "[Click Me](https://github.com/NickImpact/GTS/releases/tag/" + this.version + ")", true))
                        .withImage(GifSelector.createFor(this.level))
                        .build();

                message.addEmbeds(embed);
                discord.sendMessage(message);

                for(File file : this.files) {
                    List<File> wrapper = new ArrayList<>();
                    wrapper.add(file);

                    DiscordMessage out = new DiscordMessage.Builder()
                            .withUsername("GTS Release Notifier")
                            .withAvatarURL("https://cdn.discordapp.com/attachments/625206927152119818/785034021373607976/image0.jpg")
                            .build();

                    discord.sendMessage(out, wrapper);
                }
            }
        }
    }

    private enum GifSelector {
        Patch(ReleaseLevel.PATCH, "https://pa1.narvii.com/6156/15a7f4cf7cd0b25244e4ba415e840f689da609d2_hq.gif", 56, 100),
        Minor(ReleaseLevel.MINOR, "https://media.tenor.com/images/d8013e90642bfd34a7510810336130a2/tenor.gif", 100, 100),
        Major(ReleaseLevel.MAJOR, "https://thumbs.gfycat.com/DentalFemaleGlassfrog-max-14mb.gif", 56, 100)
        ;

        private final ReleaseLevel level;
        private final String url;
        private final int height;
        private final int width;

        GifSelector(ReleaseLevel level, String url, int height, int width) {
            this.level = level;
            this.url = url;
            this.height = height;
            this.width = width;
        }

        public static ImageEmbed createFor(ReleaseLevel level) {
            GifSelector selector = Arrays.stream(values())
                    .filter(gs -> gs.level == level)
                    .findAny()
                    .orElse(Patch);

            return new ImageEmbed(selector.url, selector.height, selector.width);
        }
    }
}
