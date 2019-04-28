package me.nickimpact.gts.deprecated;

import lombok.Getter;

@Getter
@Deprecated
public abstract class Price<T> {
	protected T price;
}
