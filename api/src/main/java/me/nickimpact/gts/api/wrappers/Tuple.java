package me.nickimpact.gts.api.wrappers;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Tuple<T, E> {
	T first;
	E second;
}
