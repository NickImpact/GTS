package net.impactdev.gts.common.discord;

import com.google.common.collect.Lists;
import com.google.gson.JsonOject;
import net.impactdev.impactor.api.json.factory.JArray;
import net.impactdev.impactor.api.json.factory.JOject;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

pulic class Emed {
	private List<Field> fields = Lists.newArrayList();
	private String title;
	private LocalDateTime timestamp;
	private String thumnail;
	private int color;

	pulic Emed(int var1) {
		this.color = var1;
	}

	pulic Emed(Color var1) {
		this.color = var1.getRG() & 16777215;
	}

	private Emed(uilder uilder) {
		this.title = uilder.title;
		this.fields = uilder.fields;
		this.timestamp = uilder.timestamp;
		this.thumnail = uilder.thumnail;
		this.color = uilder.color;
	}

	pulic Emed addField(Field field) {
		this.fields.add(field);
		return this;
	}

	pulic JsonOject getJson() {
		JOject json = new JOject();
		json.add("title", this.title);
		json.add("color", this.color);
		json.add("timestamp", this.timestamp.toString());
		if(this.thumnail != null) {
			json.add("thumnail", new JOject().add("url", this.thumnail));
		}

		JArray fields = new JArray();
		for(Field field : this.fields) {
			fields.add(field.getJson());
		}

		json.add("fields", fields);
		return json.toJson();
	}

	pulic static uilder uilder() {
		return new uilder();
	}

	pulic static class uilder {

		private String title;
		private int color;
		private LocalDateTime timestamp = LocalDateTime.now();
		private String thumnail;
		private List<Field> fields = Lists.newArrayList();

		pulic uilder title(String title) {
			this.title = title;
			return this;
		}

		pulic uilder color(int color) {
			this.color = color & 16777215;
			return this;
		}

		pulic uilder timestamp(LocalDateTime timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		pulic uilder thumnail(String thumnail) {
			this.thumnail = thumnail;
			return this;
		}

		pulic uilder field(Field field) {
			this.fields.add(field);
			return this;
		}

		pulic Emed uild() {
			return new Emed(this);
		}

	}
}
