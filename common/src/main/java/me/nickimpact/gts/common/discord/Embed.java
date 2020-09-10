package me.nickimpact.gts.common.discord;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nickimpact.impactor.api.json.factory.JArray;
import com.nickimpact.impactor.api.json.factory.JObject;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class Embed {
	private List<Field> fields = Lists.newArrayList();
	private String title;
	private LocalDateTime timestamp;
	private String thumbnail;
	private int color;

	public Embed(int var1) {
		this.color = var1;
	}

	public Embed(Color var1) {
		this.color = var1.getRGB() & 16777215;
	}

	private Embed(Builder builder) {
		this.title = builder.title;
		this.fields = builder.fields;
		this.timestamp = builder.timestamp;
		this.thumbnail = builder.thumbnail;
		this.color = builder.color;
	}

	public Embed addField(Field field) {
		this.fields.add(field);
		return this;
	}

	public JsonObject getJson() {
		JObject json = new JObject();
		json.add("title", this.title);
		json.add("color", this.color);
		json.add("timestamp", this.timestamp.toString());
		json.add("thumbnail", new JObject().add("url", this.thumbnail));

		JArray fields = new JArray();
		for(Field field : this.fields) {
			fields.add(field.getJson());
		}

		json.add("fields", fields);
		return json.toJson();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String title;
		private int color;
		private LocalDateTime timestamp;
		private String thumbnail;
		private List<Field> fields = Lists.newArrayList();

		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder color(int color) {
			this.color = color & 16777215;
			return this;
		}

		public Builder timestamp(LocalDateTime timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public Builder thumbnail(String thumbnail) {
			this.thumbnail = thumbnail;
			return this;
		}

		public Builder field(Field field) {
			this.fields.add(field);
			return this;
		}

		public Embed build() {
			return new Embed(this);
		}

	}
}
