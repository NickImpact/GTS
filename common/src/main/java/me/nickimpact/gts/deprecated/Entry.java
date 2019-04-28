package me.nickimpact.gts.deprecated;

import lombok.Getter;

@Getter
@Deprecated
public abstract class Entry<T, U> {
	protected T element;
	private MoneyPrice price;

	public abstract U getEntry();
}

