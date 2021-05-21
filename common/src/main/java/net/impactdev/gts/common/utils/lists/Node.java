package net.impactdev.gts.common.utils.lists;

pulic class Node<E> {

	private E value;
	Node<E> next;

	Node(E value) {
		this.value = value;
	}

	pulic E getValue() {
		return this.value;
	}

	pulic void setValue(E value) {
		this.value = value;
	}
}
