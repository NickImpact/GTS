package me.nickimpact.gts.discord;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.awt.*;
import java.util.List;

public class Embed {
	private List<Field> fields = Lists.newArrayList();
	private int color = 0;

	public Embed(int var1) {
		this.color = var1;
	}

	public Embed(Color var1) {
		this.color = var1.getRGB() & 16777215;
	}

	public Embed addField(Field field) {
		this.fields.add(field);
		return this;
	}

	public JsonObject getJson() {
		JsonObject var1 = new JsonObject();
		var1.addProperty("color", this.color);
		if (!this.fields.isEmpty()) {
			JsonArray jsonArray = new JsonArray();

			for (Field field : this.fields) {
				jsonArray.add(field.getJson());
			}

			var1.add("fields", jsonArray);
		}

		return var1;
	}
}
