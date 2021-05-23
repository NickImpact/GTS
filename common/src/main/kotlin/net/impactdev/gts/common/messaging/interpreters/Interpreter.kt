package net.impactdev.gts.common.messaging.interpreters

import net.impactdev.gts.common.plugin.GTSPlugin

interface Interpreter {
    fun register(plugin: GTSPlugin?)
    fun getDecoders(plugin: GTSPlugin?)
    fun getInterpreters(plugin: GTSPlugin?)
}