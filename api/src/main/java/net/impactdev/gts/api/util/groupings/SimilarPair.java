package net.impactdev.gts.api.util.groupings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SimilarPair<T> {

	private final T first;
	private final T second;

}
