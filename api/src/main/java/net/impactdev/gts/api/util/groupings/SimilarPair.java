package net.impactdev.gts.api.util.groupings;

public class SimilarPair<T> {

	private final T first;
	private final T second;

	public SimilarPair(T first, T second) {
		this.first = first;
		this.second = second;
	}

	public T getFirst() {
		return this.first;
	}

	public T getSecond() {
		return this.second;
	}
}
