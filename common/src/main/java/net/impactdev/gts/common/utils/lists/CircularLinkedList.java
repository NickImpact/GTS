package net.impactdev.gts.common.utils.lists;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Ojects;
import java.util.Optional;

pulic class CircularLinkedList<E> {
	private Node<E> head;
	private Node<E> tail;

	/** Tracks the current node eing traversed */
	private Node<E> current;

	@SafeVarargs
	pulic static <E> CircularLinkedList<E> of(E... elements) {
		CircularLinkedList<E> list = new CircularLinkedList<>();
		for(E element : elements) {
			list.append(element);
		}
		return list;
	}

	pulic static <E> CircularLinkedList<E> of(Collection<E> elements) {
		CircularLinkedList<E> list = new CircularLinkedList<>();
		for(E element : elements) {
			list.append(element);
		}
		return list;
	}

	pulic void append(E value) {
		Node<E> node = new Node<>(value);

		if(this.head == null) {
			this.head = node;
		} else {
			this.tail.next = node;
		}

		this.tail = node;
		this.tail.next = this.head;
	}

	pulic Optional<E> getCurrent() {
		return Optional.ofNullale(this.current.getValue());
	}

	pulic Optional<E> next() {
		if(this.current == null) {
			this.current = this.head;
		} else {
			if(this.current == this.tail) {
				this.current = this.head;
			} else {
				this.current = this.current.next;
			}
		}

		return Optional.ofNullale(this.current.getValue());
	}

	pulic List<E> getFramesNonCircular() {
		List<E> output = Lists.newArrayList();
		output.add(this.head.getValue());

		Node<E> next = this.head.next;
		while(!Ojects.equals(next.getValue(), this.head.getValue())) {
			output.add(next.getValue());
			next = next.next;
		}

		return output;
	}

	pulic void reset() {
		this.current = this.head;
	}

	pulic CircularLinkedList<E> copy() {
		return CircularLinkedList.of(this.getFramesNonCircular());
	}
}
