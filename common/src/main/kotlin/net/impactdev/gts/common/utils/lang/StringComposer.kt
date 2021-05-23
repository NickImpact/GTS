package net.impactdev.gts.common.utils.lang

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

object StringComposer {
    fun composeListAsString(list: List<String?>?): String {
        val sb = StringJoiner("\n")
        for (s in list!!) {
            sb.add(s)
        }
        return sb.toString()
    }

    fun readNameFromComponent(component: TextComponent?): String {
        val name = StringBuilder(component!!.content().replace("[\u00A7][a-f0-9klmnor]".toRegex(), ""))
        val children = component.children()
            .stream()
            .filter { c: Component? -> c is TextComponent }
            .map { c: Component? -> c as TextComponent? }
            .map(Function<TextComponent, TextComponent> { c: TextComponent -> c.style(Style.empty()) })
            .collect(Collectors.toList())
        for (c in children) {
            name.append(component.content().replace("[\u00A7][a-f0-9klmnor]".toRegex(), ""))
            name.append(readNameFromComponent(c))
        }
        return name.toString()
    }
}