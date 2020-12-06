package net.impactdev.gts.api.util;

@FunctionalInterface
public interface TriFunction<T, U, V, R> {

	/**
	 * Applies this function to the given arguments.
	 *
	 * @param t the first function argument
	 * @param u the second function argument
	 * @return the function result
	 */
	R apply(T t, U u, V v);

}
