package me.nickimpact.gts.api.deprecated;

import lombok.Getter;

@Getter
@Deprecated
public abstract class Entry<T, U> {
	protected String id;
	protected T element;
	private MoneyPrice price;

	public Entry() {}

	public T getElement() {
		return this.element;
	}

	public abstract U getEntry();
}

