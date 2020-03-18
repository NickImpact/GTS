package me.nickimpact.gts.api.util;

/**
 * A builder follows the concepts of the typical Builder design.
 *
 * @param <T>
 * @param <B>
 */
public interface Builder<T, B> {

	B from(T input);

	T build();

}
