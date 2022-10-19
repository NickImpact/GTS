package net.impactdev.gts.locale;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.impactdev.gts.configuration.GTSConfigKeys;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.utilities.ExceptionPrinter;
import net.kyori.adventure.audience.Audience;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class TranslationRepository {

    private static final String TRANSLATIONS_INFO_ENDPOINT = "https://metadata.luckperms.net/data/translations";
    private static final String TRANSLATIONS_DOWNLOAD_ENDPOINT = "https://metadata.luckperms.net/translation/";
    private static final long MAX_BUNDLE_SIZE = 1048576L; // 1mb
    private static final long CACHE_MAX_AGE = TimeUnit.HOURS.toMillis(23);

    private final Client client = new Client();
    private final Gson normal = new GsonBuilder().create();
    private final Gson pretty = new GsonBuilder().setPrettyPrinting().create();

    public List<LanguageInfo> available() throws Exception {
        return this.getTranslationsMetadata().languages;
    }

    public void scheduleRefresh() {
        if(!GTSConfigKeys.AUTO_INSTALL_TRANSLATIONS.parse()) {
            return;
        }

        Impactor.instance().scheduler().executeAsync(() -> {

            try {
                this.refresh();
            } catch (Exception e) {

            }
        });
    }

    private void refresh() throws Exception {
        long last = 0;
        long since = System.currentTimeMillis() - last;

        if(since <= CACHE_MAX_AGE) {
            return;
        }

        MetadataResponse metadata = this.getTranslationsMetadata();
        if(since <= metadata.cacheMaxAge) {
            return;
        }

        this.downloadAndInstall(this.available(), null);
    }

    private void clear(Path directory, Predicate<Path> filter) {
        try (Stream<Path> paths = Files.list(directory).filter(filter)) {
            paths.forEach(path -> {
                try {
                    Files.delete(path);
                } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}
    }

    /**
     * Downloads and installs translations for the given languages.
     *
     * @param languages The languages we should download and install
     * @param audience The audience that should be informed of the status of the installation
     */
    private void downloadAndInstall(List<LanguageInfo> languages, @Nullable Audience audience) {
        TranslationManager parent = GTSPlugin.instance().translations();
        Path target = parent.getRepositoryDirectory();

        this.clear(target, TranslationManager::isConfigurationFile);
        for(LanguageInfo language : languages) {
            if(audience != null) {
                // TODO - Send message to audience
            }

            Request request = new Request.Builder()
                    .header("User-Agent", Client.USER_AGENT)
                    .url(TRANSLATIONS_DOWNLOAD_ENDPOINT + language.id)
                    .build();
            try (Response response = this.client.makeRequest(request)) {
                try (ResponseBody body = response.body()) {
                    if(body == null) {
                        throw new IOException("Could not locate response");
                    }

                    Path file = target.resolve(language.locale + ".conf");
                    try (InputStream stream = new LimitedInputStream(body.byteStream(), MAX_BUNDLE_SIZE)) {
                        Files.copy(stream, file, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            } catch (Exception e) {
                if(audience != null) {
                    // TODO - Send message to audience about failure
                }

                GTSPlugin.instance().logger().warn("Failed to download translations");
                ExceptionPrinter.print(GTSPlugin.instance(), e);
            }
        }

        parent.reload();
    }

    private void writeLastRefreshTime() {
        Path statusFile = GTSPlugin.instance().translations().getRepositoryStatusFile();

        try (BufferedWriter writer = Files.newBufferedWriter(statusFile, StandardCharsets.UTF_8)) {
            JsonObject status = new JsonObject();
            status.add("lastRefresh", new JsonPrimitive(System.currentTimeMillis()));
            this.pretty.toJson(status, writer);
        } catch (IOException e) {
            // ignore
        }
    }

    private long readLastRefreshTime() {
        Path statusFile = GTSPlugin.instance().translations().getRepositoryStatusFile();

        if (Files.exists(statusFile)) {
            try (BufferedReader reader = Files.newBufferedReader(statusFile, StandardCharsets.UTF_8)) {
                JsonObject status = this.normal.fromJson(reader, JsonObject.class);
                if (status.has("lastRefresh")) {
                    return status.get("lastRefresh").getAsLong();
                }
            } catch (Exception e) {
                // ignore
            }
        }

        return 0L;
    }

    private MetadataResponse getTranslationsMetadata() throws Exception {
        Request request = new Request.Builder()
                .header("User-Agent", Client.USER_AGENT)
                .url(TRANSLATIONS_INFO_ENDPOINT)
                .build();

        JsonObject jsonResponse;
        try (Response response = this.client.makeRequest(request)) {
            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) {
                    throw new RuntimeException("No response");
                }

                try (InputStream inputStream = new LimitedInputStream(responseBody.byteStream(), MAX_BUNDLE_SIZE)) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                        jsonResponse = this.normal.fromJson(reader, JsonObject.class);
                    }
                }
            }
        }

        List<LanguageInfo> languages = new ArrayList<>();
        for (Map.Entry<String, JsonElement> language : jsonResponse.get("languages").getAsJsonObject().entrySet()) {
            languages.add(new LanguageInfo(language.getKey(), language.getValue().getAsJsonObject()));
        }
        languages.removeIf(language -> language.progress() <= 0);

        if (languages.size() >= 100) {
            // just a precaution: if more than 100 languages have been
            // returned then the metadata server is doing something silly
            throw new IOException("More than 100 languages - cancelling download");
        }

        long cacheMaxAge = jsonResponse.get("cacheMaxAge").getAsLong();

        return new MetadataResponse(cacheMaxAge, languages);
    }

    private static final class MetadataResponse {
        private final long cacheMaxAge;
        private final List<LanguageInfo> languages;

        MetadataResponse(long cacheMaxAge, List<LanguageInfo> languages) {
            this.cacheMaxAge = cacheMaxAge;
            this.languages = languages;
        }
    }

    public static final class LanguageInfo {
        private final String id;
        private final String name;
        private final Locale locale;
        private final int progress;
        private final List<String> contributors;

        LanguageInfo(String id, JsonObject data) {
            this.id = id;
            this.name = data.get("name").getAsString();
            this.locale = Objects.requireNonNull(TranslationManager.parseLocale(data.get("localeTag").getAsString()));
            this.progress = data.get("progress").getAsInt();
            this.contributors = new ArrayList<>();
            for (JsonElement contributor : data.get("contributors").getAsJsonArray()) {
                this.contributors.add(contributor.getAsJsonObject().get("name").getAsString());
            }
        }

        public String id() {
            return this.id;
        }

        public String name() {
            return this.name;
        }

        public Locale locale() {
            return this.locale;
        }

        public int progress() {
            return this.progress;
        }

        public List<String> contributors() {
            return this.contributors;
        }
    }

    private static final class LimitedInputStream extends FilterInputStream implements Closeable {
        private final long limit;
        private long count;

        public LimitedInputStream(InputStream inputStream, long limit) {
            super(inputStream);
            this.limit = limit;
        }

        private void checkLimit() throws IOException {
            if (this.count > this.limit) {
                throw new IOException("Limit exceeded");
            }
        }

        @Override
        public int read() throws IOException {
            int res = super.read();
            if (res != -1) {
                this.count++;
                checkLimit();
            }
            return res;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int res = super.read(b, off, len);
            if (res > 0) {
                this.count += res;
                checkLimit();
            }
            return res;
        }
    }

    private static final class Client {

        public static final String USER_AGENT = "gts";

        private final OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(15, TimeUnit.SECONDS)
                .build();

        Response makeRequest(Request request) throws Exception {
            Response response = this.client.newCall(request).execute();
            if(!response.isSuccessful()) {
                response.close();
                throw new RuntimeException("Request was unsuccessful: " + response.code() + " - " + response.message());
            }

            return response;
        }

    }

}
