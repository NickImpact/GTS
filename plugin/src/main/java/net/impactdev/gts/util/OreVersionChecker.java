package net.impactdev.gts.util;

import com.google.gson.JsonObject;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.api.util.Version;
import net.impactdev.impactor.api.Impactor;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class OreVersionChecker {

    public static CompletableFuture<Version> query() {
        String url = "https://ore.spongepowered.org/api/v1/projects/gts";

        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpsURLConnection connection = (HttpsURLConnection) (new URL(url)).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "GTS Minecraft Plugin");
                connection.setDoInput(true);
                connection.setReadTimeout(5000);
                InputStream in = connection.getInputStream();

                JsonObject response = GTSPlugin.instance().gson().fromJson(new InputStreamReader(in), JsonObject.class);
                if (response.has("recommended")) {
                    return new Version(response.getAsJsonObject("recommended").get("name").getAsString());
                }

                return new Version("");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, Impactor.getInstance().getScheduler().async());
    }
}
