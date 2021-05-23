package net.impactdev.gts.api.commands

import com.google.common.collect.Lists
import net.impactdev.gts.api.commands.annotations.Alias
import net.impactdev.gts.api.commands.annotations.Permission

interface GTSCommandExecutor<E, S> {
    fun register()
    val aliases: List<String>?
        get() = Lists.newArrayList(*this.javaClass.getAnnotation(Alias::class.java).value())
    val arguments: Array<E>?
    val subcommands: Array<GTSCommandExecutor<E, S>?>?
    fun build(): S
    fun hasNeededAnnotations(): Boolean {
        return this.javaClass.isAnnotationPresent(Alias::class.java) && this.javaClass.isAnnotationPresent(Permission::class.java)
    }
}