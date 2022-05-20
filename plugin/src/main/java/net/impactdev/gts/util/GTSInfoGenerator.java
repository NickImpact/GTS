package net.impactdev.gts.util;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.impactdev.gts.messaging.types.PluginMessageMessenger;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.extensions.Extension;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.common.utils.exceptions.ExceptionWriter;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.services.text.MessageService;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.plugin.PluginContainer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

public class GTSInfoGenerator {

    private static Path path;

    private List<String> processor = Lists.newArrayList();
    private int max = -1;

    public GTSInfoGenerator() {
        if(path == null) {
            path = GTSPlugin.instance().bootstrap().dataDirectory();
        }
    }

    public CompletableFuture<String> create(Audience audience) {
        MessageService service = Utilities.PARSER;

        return CompletableFuture.supplyAsync(() -> {
            try {
                File file = new File(path.toFile(), this.createName());
                file.getParentFile().mkdirs();
                file.createNewFile();

                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);

                this.compose();
                this.write(bw);

                bw.flush();
                bw.close();
                fw.close();

                return file.getName();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, Impactor.getInstance().getScheduler().async())
                .exceptionally(e -> {
                    audience.sendMessage(service.parse("{{gts:error}} An error occurred during processing, please check console for more info"));
                    ExceptionWriter.write(e);
                    return null;
                });

    }

    private void compose() {
        this.append(this.separator());
        this.append(this.header());
        this.append(this.separator());
        this.append("Environment");
        this.append(this.separator());
        this.append(this.environment());
        this.append(this.separator());
        this.append("Loaded Extensions");
        this.append(this.separator());
        this.append(this.extensions());
        this.append(this.separator());
        this.append("Connection Information");
        this.append(this.separator());
        this.append(this.connections());
        this.append(this.separator());
        this.append("Listings Information");
        this.append(this.separator());
        this.append(this.listings());
        this.append(this.separator());
    }

    private void write(BufferedWriter bw) throws IOException {
        for(String s : this.processor) {
            if(s.equals(this.SEPARATOR)) {
                StringBuilder x = new StringBuilder("+");
                for(int i = 0; i < this.max + 2; i++) {
                    x.append("-");
                }
                bw.write(x.toString() + "+");
            } else {
                StringBuilder x = new StringBuilder("| ");
                x.append(s);
                for(int i = s.length(); i < this.max; i++) {
                    x.append(" ");
                }
                bw.write(x.toString() + " |");
            }

            bw.write('\n');
        }
    }

    private void append(String string) {
        this.max = Math.max(this.max, string.length());
        this.processor.add(string);
    }

    private void append(List<String> collection) {
        for(String s : collection) {
            this.append(s);
        }
    }

    private String createName() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH_mm_ss");
        LocalDateTime time = LocalDateTime.now();
        return "gts-info-" + time.format(formatter) + ".txt";
    }

    private List<String> header() {
        return Lists.newArrayList(
                "GTS Diagnostic Information"
        );
    }

    private List<String> environment() {
        Platform platform = Sponge.platform();
        PluginContainer game = platform.container(Platform.Component.GAME);
        PluginContainer api = platform.container(Platform.Component.API);
        PluginContainer impl = platform.container(Platform.Component.IMPLEMENTATION);

        String pattern = "%s: %s %s";

        return Lists.newArrayList(
                String.format(pattern, "Minecraft Version", game.metadata().name(), game.metadata().version()),
                String.format(pattern, "Sponge API Version", api.metadata().name(), api.metadata().version()),
                String.format(pattern, "Sponge Version", impl.metadata().name(), impl.metadata().version()),
                String.format(pattern, "GTS Version", GTSPlugin.instance().metadata().version(), "(Git: @git_commit@)")
        );
    }

    private List<String> extensions() {
        List<String> results = Lists.newArrayList();
        for(Extension extension : GTSService.getInstance().getAllExtensions()) {
            results.add(String.format("%s: %s", extension.metadata().name(), extension.metadata().version()));
        }

        return results;
    }

    private List<String> connections() {
        List<String> results = Lists.newArrayList();
        results.add("Messaging Service: " + GTSPlugin.instance().messagingService().getName());

        boolean not = !(GTSPlugin.instance().messagingService().getMessenger() instanceof PluginMessageMessenger);
        if(not || Sponge.server().onlinePlayers().size() > 0) {
            try {
                GTSPlugin.instance().messagingService().sendPing()
                        .thenAccept(pong -> results.add("  - Response Time: " + pong.getResponseTime() + " ms"))
                        .get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                results.add("  - Response Time: Timed Out");
            }
        } else {
            results.add("  - No players online, unable to send ping request");
        }

        results.add("Storage Type: " + GTSPlugin.instance().configuration().main().get(ConfigKeys.STORAGE_METHOD));

        try {
            GTSPlugin.instance().storage()
                    .getMeta()
                    .get(5, TimeUnit.SECONDS)
                    .forEach((key, value) -> {
                        results.add("  - " + key + ": " + value);
                    });
        } catch (Exception ignored) {}

        return results;
    }

    private List<String> listings() {
        ListingManager<?, ?, ?> manager = Impactor.getInstance().getRegistry().get(ListingManager.class);
        final List<String> output = Lists.newArrayList();
        Gson writer = new GsonBuilder().setPrettyPrinting().create();
        manager.fetchListings().thenAccept(listings -> {
            output.add("Stored Listings: " + listings.size());
            output.add("Buy It Now: " + listings.stream().filter(x -> x instanceof BuyItNow).count());
            output.add("Auction: " + listings.stream().filter(x -> x instanceof Auction).count());

            if(listings.size() > 0) {
                output.add("");
                output.add("Listing Data:");
                for (Listing listing : listings) {
                    JsonObject json = listing.serialize().toJson();

                    String raw = writer.toJson(json);
                    output.addAll(Arrays.asList(raw.split("\n")));
                    output.add("");
                }
            }
        }).join();

        return output;
    }

    private final String SEPARATOR = "{{separator}}";

    private String separator() {
        return this.SEPARATOR;
    }

}
