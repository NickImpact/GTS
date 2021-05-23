package net.impactdev.gts.common.utils.lists

import com.google.common.collect.Lists
import java.util.*

class CircularLinkedList<E> {
    private var head: Node<E>? = null
    private var tail: Node<E>? = null

    /** Tracks the current node being traversed  */
    private var current: Node<E>? = null
    fun append(value: E) {
        val node = Node(value)
        if (head == null) {
            head = node
        } else {
            tail!!.next = node
        }
        tail = node
        tail.next = head
    }

    fun getCurrent(): Optional<E> {
        return Optional.ofNullable(current.getValue())
    }

    operator fun next(): Optional<E> {
        if (current == null) {
            current = head
        } else {
            if (current === tail) {
                current = head
            } else {
                current = current.next
            }
        }
        return Optional.ofNullable(current.getValue())
    }

    val framesNonCircular: List<E?>
        get() {
            val output: MutableList<E?> = Lists.newArrayList()
            output.add(head.getValue())
            var next = head!!.next
            while (next.value != head.getValue()) {
                output.add(next.value)
                next = next!!.next
            }
            return output
        }

    fun reset() {
        current = head
    }

    fun copy(): CircularLinkedList<E> {
        return of(framesNonCircular)
    }

    companion object {
        @SafeVarargs
        fun <E> of(vararg elements: E): CircularLinkedList<E> {
            val list = CircularLinkedList<E>()
            for (element in elements) {
                list.append(element)
            }
            return list
        }

        fun <E> of(elements: Collection<E>): CircularLinkedList<E> {
            val list = CircularLinkedList<E>()
            for (element in elements) {
                list.append(element)
            }
            return list
        }
    }
}