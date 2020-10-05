package net.impactdev.gts.common.utils.lists;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CircularLinkedList<E> {
	private Node<E> head;
	private Node<E> tail;

	/** Tracks the current node being traversed */
	private Node<E> current;

	@SafeVarargs
	public static <E> CircularLinkedList<E> of(E... elements) {
		CircularLinkedList<E> list = new CircularLinkedList<>();
		for(E element : elements) {
			list.append(element);
		}
		return list;
	}

	public static <E> CircularLinkedList<E> of(Collection<E> elements) {
		CircularLinkedList<E> list = new CircularLinkedList<>();
		for(E element : elements) {
			list.append(element);
		}
		return list;
	}

	public void append(E value) {
		Node<E> node = new Node<>(value);

		if(this.head == null) {
			this.head = node;
		} else {
			this.tail.next = node;
		}

		this.tail = node;
		this.tail.next = this.head;
	}

	public Optional<E> getCurrent() {
		return Optional.ofNullable(this.current.getValue());
	}

	public Optional<E> next() {
		if(this.current == null) {
			this.current = this.head;
		} else {
			if(this.current == this.tail) {
				this.current = this.head;
			} else {
				this.current = this.current.next;
			}
		}

		return Optional.ofNullable(this.current.getValue());
	}

	public List<E> getFramesNonCircular() {
		List<E> output = Lists.newArrayList();
		output.add(this.head.getValue());

		Node<E> next = this.head.next;
		while(!Objects.equals(next.getValue(), this.head.getValue())) {
			output.add(next.getValue());
			next = next.next;
		}

		return output;
	}

	public void reset() {
		this.current = this.head;
	}

	public CircularLinkedList<E> copy() {
		return CircularLinkedList.of(this.getFramesNonCircular());
	}
}
