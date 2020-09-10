package me.nickimpact.gts.common.discord;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nickimpact.impactor.api.utilities.Builder;
import lombok.Getter;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

public class Message {

	private List<Embed> embeds = Lists.newArrayList();
	private String username;
	private String avatarUrl;

	@Getter private transient List<String> webhooks = Lists.newArrayList();

	public Message(String username, String avatar, DiscordOption option) {
		this.username = username;
		this.avatarUrl = avatar;
		this.webhooks = option.getWebhookChannels();
	}

	public Message addEmbed(Embed embed) {
		this.embeds.add(embed);
		return this;
	}

	HttpsURLConnection send(String url) throws Exception {
		HttpsURLConnection connection = (HttpsURLConnection)(new URL(url)).openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:49.0) Gecko/20100101 Firefox/49.0");
		connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		connection.setDoOutput(true);
		connection.setDoInput(true);
		String json = this.getJsonString();
		connection.setRequestProperty("Content-length", String.valueOf(json.length()));
		DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
		dos.write(json.getBytes(StandardCharsets.UTF_8));
		dos.flush();
		dos.close();
		return connection;
	}

	String getJsonString() {
		JsonObject var1 = new JsonObject();
		if (this.username != null) {
			var1.addProperty("username", this.username);
		}

		if (this.avatarUrl != null) {
			var1.addProperty("avatar_url", this.avatarUrl);
		}

		if (!this.embeds.isEmpty()) {
			JsonArray jArray = new JsonArray();

			for (Embed embed : this.embeds) {
				jArray.add(embed.getJson());
			}

			var1.add("embeds", jArray);
		}

		return var1.toString();
	}

}
