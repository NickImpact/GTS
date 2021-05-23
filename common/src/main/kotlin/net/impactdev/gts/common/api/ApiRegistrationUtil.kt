package net.impactdev.gts.common.api

import net.impactdev.gts.api.GTSService
import net.impactdev.gts.api.GTSServiceProvider
import java.lang.reflect.Method

object ApiRegistrationUtil {
    private val REGISTER: Method? = null
    private val UNREGISTER: Method? = null
    fun register(service: GTSService?) {
        try {
            REGISTER!!.invoke(null, service)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unregister() {
        try {
            UNREGISTER!!.invoke(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {
        try {
            REGISTER =
                GTSServiceProvider::class.java.getDeclaredMethod("register", GTSService::class.java)
            REGISTER.setAccessible(true)
            UNREGISTER = GTSServiceProvider::class.java.getDeclaredMethod("unregister")
            UNREGISTER.setAccessible(true)
        } catch (e: NoSuchMethodException) {
            throw ExceptionInInitializerError(e)
        }
    }
}