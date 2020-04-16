package me.nickimpact.gts.api.util.groupings;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SimilarPair<T> {

	private T first;
	private T second;

}
