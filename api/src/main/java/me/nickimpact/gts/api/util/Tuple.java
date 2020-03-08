package me.nickimpact.gts.api.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Tuple<T, E> {
	T first;
	E second;
}
