package net.impactdev.gts.common.blacklist

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import net.impactdev.gts.api.blacklist.Blacklist

class BlacklistImpl : Blacklist {
    private val blacklist: Multimap<Class<*>, String> = ArrayListMultimap.create()
    override fun getBlacklist(): Multimap<Class<*>, String> {
        return blacklist
    }

    override fun append(registrar: Class<*>, key: String) {
        blacklist.put(registrar, key)
    }

    override fun isBlacklisted(registrar: Class<*>, query: String): Boolean {
        return blacklist[registrar].contains(query)
    }

    override fun clear() {
        blacklist.clear()
    }
}