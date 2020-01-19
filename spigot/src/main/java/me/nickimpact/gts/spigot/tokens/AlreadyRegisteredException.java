package me.nickimpact.gts.spigot.tokens;

public class AlreadyRegisteredException extends RuntimeException {
	public AlreadyRegisteredException(String token) {
		super("Token already registered: " + token);
	}
}
