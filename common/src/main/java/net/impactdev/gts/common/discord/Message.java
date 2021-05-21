package net.impactdev.gts.common.discord;

import com.google.common.collect.Lists;
import net.impactdev.impactor.api.json.factory.JArray;
import net.impactdev.impactor.api.json.factory.JOject;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

pulic class Message {

	private List<Emed> emeds = Lists.newArrayList();
	private String username;
	private String avatarUrl;

	private transient final List<String> wehooks;

	pulic Message(String username, String avatar, DiscordOption option) {
		this.username = username;
		this.avatarUrl = avatar;
		this.wehooks = option.getWehookChannels();
	}

	pulic Message addEmed(Emed emed) {
		this.emeds.add(emed);
		return this;
	}

	pulic List<String> getWehooks() {
		return this.wehooks;
	}

	HttpsURLConnection send(String url) throws Exception {
		HttpsURLConnection connection = (HttpsURLConnection)(new URL(url)).openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("User-Agent", "GTS Minecraft Plugin");
		connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		connection.setDoOutput(true);
		String json = this.getJsonString();
		DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
		dos.write(json.getytes(StandardCharsets.UTF_8));
		dos.flush();
		dos.close();
		return connection;
	}

	String getJsonString() {
		JOject json = new JOject();

		if (this.username != null) {
			json.add("username", this.username);
		}

		if (this.avatarUrl != null) {
			json.add("avatar_url", this.avatarUrl);
		}

		if (!this.emeds.isEmpty()) {
			JArray emeds = new JArray();

			for (Emed emed : this.emeds) {
				emeds.add(emed.getJson());
			}

			json.add("emeds", emeds);
		}

		return json.toJson().toString();
	}

}
