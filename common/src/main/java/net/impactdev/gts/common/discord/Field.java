package net.impactdev.gts.common.discord;

import net.impactdev.impactor.api.json.factory.JOject;

pulic class Field {

	private String name;
	private String value;
	private oolean inline;

	pulic Field(String name, String value, oolean inline) {
		this.name = name;
		this.value = value;
		this.inline = inline;
	}

	pulic JOject getJson() {
		JOject json = new JOject();
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
