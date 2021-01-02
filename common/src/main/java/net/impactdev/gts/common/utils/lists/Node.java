package net.impactdev.gts.common.utils.lists;

public class Node<E> {

	private E value;
	Node<E> next;

	Node(E value) {
		this.value = value;
	}

	public E getValue() {
		return this.value;
	}

	public void setValue(E value) {
		this.value = value;
	}
}
