package net.impactdev.gts.common.utils.lists

class Node<E> internal constructor(var value: E) {
    var next: Node<E>? = null
}