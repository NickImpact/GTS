package me.nickimpact.gts.api.deprecated;

import lombok.Getter;

@Getter
@Deprecated
public abstract class Price<T> {
	protected String id;
	protected T price;
}
