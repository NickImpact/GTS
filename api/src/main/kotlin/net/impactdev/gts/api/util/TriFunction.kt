package net.impactdev.gts.api.util

@FunctionalInterface
interface TriFunction<T, U, V, R> {
    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     */
    fun apply(t: T, u: U, v: V): R
}