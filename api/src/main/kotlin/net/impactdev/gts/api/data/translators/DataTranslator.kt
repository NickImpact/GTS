package net.impactdev.gts.api.data.translators

import java.util.*

@FunctionalInterface
interface DataTranslator<T> {
    fun translate(input: T): Optional<T>?
}