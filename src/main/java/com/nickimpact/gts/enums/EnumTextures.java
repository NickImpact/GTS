package com.nickimpact.gts.enums;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public enum EnumTextures {

	Summer(""),
	Winter(""),
	Present("NmNlZjlhYTE0ZTg4NDc3M2VhYzEzNGE0ZWU4OTcyMDYzZjQ2NmRlNjc4MzYzY2Y3YjFhMjFhODViNyJ9fX0="),
	Crate("ZjYyNGM5MjdjZmVhMzEzNTU0Mjc5OTNkOGI3OTcxMmU4NmY5NGQ1OTUzNDMzZjg0ODg0OWEzOWE2ODc5In19fQ==");

	public String value;

	private EnumTextures(String texture) {
		this.value = texture;
	}
}
