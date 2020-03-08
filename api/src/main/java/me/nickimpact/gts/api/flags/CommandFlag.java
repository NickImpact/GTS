package me.nickimpact.gts.api.flags;

public interface CommandFlag<T> {

	String getKey();

	T getValue();

}
