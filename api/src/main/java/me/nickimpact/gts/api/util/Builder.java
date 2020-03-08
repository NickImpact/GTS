package me.nickimpact.gts.api.util;

/**
 * A builder follows the concepts of the typical Builder design.
 *
 * @param <T>
 * @param <F>
 */
public interface Builder<T, F> {

	F from(T input);

	T build();

}
