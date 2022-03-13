/* This design originates from Nucleus, which is licensed under MIT */
package net.impactdev.gts.gradle.tasks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.impactdev.gts.gradle.enums.ReleaseLevel;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskAction;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UploadToOre extends DefaultTask {

    private static final Function<String, String> LINK_BUILDER = in -> "https://ore.spongepowered.org/api/" + in;

    public String apiKey;
    public boolean force;
    public Provider<String> notes;
    public ReleaseLevel level;
    public File file;
    public String pluginID = "";

    @TaskAction
    public void run() {
        if(this.force || !this.level.isSnapshot()) {
            if(this.apiKey != null && this.file != null) {
                try {
                    URL auth = new URL(LINK_BUILDER.apply("v2/authenticate"));
                    URI upload = new URI(LINK_BUILDER.apply("v2/projects/" + this.pluginID + "/versions"));
                    URL destroy = new URL(LINK_BUILDER.apply("v2/sessions/current"));

                    Gson gson = new Gson();
                    this.getLogger().info("Starting Upload...");

                    this.getLogger().info("Obtaining authorization...");
                    HttpsURLConnection connection = (HttpsURLConnection) auth.openConnection();

                    String key;
                    try {
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Authorization", "OreApi apikey=" + this.apiKey);
                        connection.setRequestProperty("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
                        connection.setRequestProperty("User-Agent", "GTS/Gradle");

                        int status = connection.getResponseCode();
                        if(status != 200) {
                            throw new Exception("Error getting session: " + status);
                        }

                        String response = this.returnStringFromInputStream(connection.getInputStream());
                        JsonObject json = gson.fromJson(response, JsonObject.class);
                        key = json.get("session").getAsString();
                    } finally {
                        connection.disconnect();
                    }

                    String json = gson.toJson(new FileUploadData(this.notes.get()));

                    HttpPost post = new HttpPost(upload);
                    String multipartBoundary = "somegibberishusedasaboundary";
                    post.setEntity(MultipartEntityBuilder.create()
                            .addTextBody("plugin-info", json, ContentType.APPLICATION_JSON)
                            .addBinaryBody("plugin-file", this.file)
                            .setBoundary(multipartBoundary)
                            .build()
                    );
                    post.addHeader("Authorization", "OreApi session=" + key);
                    post.addHeader("Content-Type", ContentType.MULTIPART_FORM_DATA
                            .withParameters(new BasicNameValuePair("boundary", multipartBoundary))
                            .toString()
                    );
                    post.addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());
                    post.addHeader("User-Agent", "GTS/Gradle");

                    CloseableHttpResponse client = HttpClients.createDefault().execute(post);
                    int status = client.getStatusLine().getStatusCode();
                    if(status != 201) {
                        this.destroySession(destroy, key);
                        this.getLogger().error(this.returnStringFromInputStream(client.getEntity().getContent()));
                        throw new GradleException("Failed to upload:\n" +
                                "Status Code: " + status + "\n" +
                                "Status Phrase: " + client.getStatusLine().getReasonPhrase()
                        );
                    }

                    JsonObject result = gson.fromJson(this.returnStringFromInputStream(client.getEntity().getContent()), JsonObject.class);
                    String fileInfoName = result.getAsJsonObject("file_info").get("name").getAsString();
                    this.getLogger().info("Successfully uploaded: " + fileInfoName);
                    this.getLogger().debug(result.toString());

                    this.destroySession(destroy, key);
                } catch (Exception e) {
                    throw new GradleException(e.getMessage());
                }
            }
        } else {
            this.getLogger().info("File: " + this.file.getName());
            this.getLogger().info("Release Level: " + this.level);
            this.getLogger().info("API Key: " + Optional.ofNullable(this.apiKey).orElse("???"));
        }
    }

    private void destroySession(URL url, String key) throws Exception {
        HttpsURLConnection post = (HttpsURLConnection) url.openConnection();
        try {
            post.setRequestMethod("DELETE");
            post.setRequestProperty("Authorization", "OreApi session=" + key);
            post.setRequestProperty("User-Agent", "GTS/Gradle");
            int status = post.getResponseCode();
            this.getLogger().info("Deleted session: " + status);
        } finally {
            post.disconnect();
        }
    }

    private String returnStringFromInputStream(InputStream in) {
        return new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining(System.lineSeparator()));
    }

    private static class FileUploadData {
        private final String description;
        private final boolean create_forum_post = true;

        public FileUploadData(String description) {
            this.description = description;
        }
    }
}
