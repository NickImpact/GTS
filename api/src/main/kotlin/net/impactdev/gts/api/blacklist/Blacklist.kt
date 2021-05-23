package net.impactdev.gts.api.blacklist

import com.google.common.collect.Multimap

interface Blacklist {
    val blacklist: Multimap<Class<*>?, String?>?
    fun append(registrar: Class<*>?, key: String?)
    fun isBlacklisted(registrar: Class<*>?, query: String?): Boolean
    fun clear()
}