package com.nickimpact.gts.api.exceptions;

import lombok.Getter;

/**
 * Since we provide an API for others to register their own tokens through GTS into Nucleus,
 * we must also provide a check to ensure a Token has not yet been registered. That way, we don't
 * allow any sort of override on a variable.
 *
 * <p>Note: This may change depending on what the community of developers deems is proper.</p>
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
