package net.impactdev.gts.common.discord;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import net.impactdev.impactor.api.json.factory.JArray;
import net.impactdev.impactor.api.json.factory.JObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Message {

	private List<Embed> embeds = Lists.newArrayList();
	private String username;
	private String avatarUrl;

	@Getter private transient final List<String> webhooks;

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
		connection.setRequestProperty("User-Agent", "GTS Minecraft Plugin");
		connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		connection.setDoOutput(true);
		String json = this.getJsonString();
		DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
		dos.write(json.getBytes(StandardCharsets.UTF_8));
		dos.flush();
		dos.close();
		return connection;
	}

	String getJsonString() {
		JObject json = new JObject();

		if (this.username != null) {
			json.add("username", this.username);
		}

		if (this.avatarUrl != null) {
			json.add("avatar_url", this.avatarUrl);
		}

		if (!this.embeds.isEmpty()) {
			JArray embeds = new JArray();

			for (Embed embed : this.embeds) {
				embeds.add(embed.getJson());
			}

			json.add("embeds", embeds);
		}

		return json.toJson().toString();
	}

}
