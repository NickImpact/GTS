package com.nickimpact.gts.api.exceptions;

import lombok.Getter;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class TokenAlreadyRegisteredException extends RuntimeException {

	@Getter private String key;

	public TokenAlreadyRegisteredException(String key) {
		this.key = key;
	}

	@Override
	public String getMessage() {
		return String.format("Token key \"%s\" already registered...", key);
	}
}
