package net.impactdev.gts.common.discord;

import net.impactdev.impactor.api.json.factory.JObject;

public class Field {

	private String name;
	private String value;
	private boolean inline;

	public Field(String name, String value, boolean inline) {
		this.name = name;
		this.value = value;
		this.inline = inline;
	}

	public JObject getJson() {
		JObject json = new JObject();
		if (this.name != null) {
			json.add("name", this.name);
		}

		if (this.value != null) {
			json.add("value", this.value);
		}

		return json.consume(d -> {
			if(this.inline) {
				d.add("inline", true);
			}
		});
	}
}
