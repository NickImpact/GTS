package me.nickimpact.gts.discord;

import com.google.gson.JsonObject;

public class Field {
	private String name;
	private String value;

	public Field(String var1, String var2) {
		this.name = var1;
		this.value = var2;
	}

	public JsonObject getJson() {
		JsonObject var1 = new JsonObject();
		if (this.name != null) {
			var1.addProperty("name", this.name);
		}

		if (this.value != null) {
			var1.addProperty("value", this.value);
		}

		return var1;
	}
}
