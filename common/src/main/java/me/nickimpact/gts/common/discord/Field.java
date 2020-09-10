package me.nickimpact.gts.common.discord;

import com.google.gson.JsonObject;

public class Field {
	private String name;
	private String value;

	public Field(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public JsonObject getJson() {
		JsonObject json = new JsonObject();
		if (this.name != null) {
			json.addProperty("name", this.name);
		}

		if (this.value != null) {
			json.addProperty("value", this.value);
		}

		return json;
	}
}
