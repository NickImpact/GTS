package net.impactdev.gts.common.plugin

import com.google.common.collect.ImmutableList
import com.google.gson.Gson
import net.impactdev.gts.api.environment.Environment
import net.impactdev.gts.api.extension.ExtensionManager
import net.impactdev.gts.api.storage.GTSStorage
import net.impactdev.gts.common.messaging.InternalMessagingService
import net.impactdev.gts.common.plugin.bootstrap.GTSBootstrap
import net.impactdev.impactor.api.Impactor
import net.impactdev.impactor.api.plugin.ImpactorPlugin
import net.impactdev.impactor.api.plugin.components.Configurable
import net.impactdev.impactor.api.plugin.components.Depending
import net.impactdev.impactor.api.plugin.components.Translatable
import net.impactdev.impactor.api.storage.StorageType
import java.io.InputStream
import java.util.*

interface GTSPlugin : ImpactorPlugin, Configurable, Depending, Translatable {
    fun <T : GTSPlugin?> `as`(type: Class<T>?): T

    /**
     *
     *
     * @return
     */
    val bootstrap: GTSBootstrap

    /**
     * Fetches information regarding the environment
     *
     * @return
     */
    val environment: Environment?

    /**
     *
     * @return
     */
    val gson: Gson

    /**
     *
     * @return
     */
    val storage: GTSStorage
    val extensionManager: ExtensionManager?
    val messagingService: InternalMessagingService?
    val multiServerCompatibleStorageOptions: ImmutableList<StorageType?>
    fun getPlayerDisplayName(id: UUID?): String
    fun getResourceStream(path: String?): InputStream {
        return this.javaClass.classLoader.getResourceAsStream(path)
    }

    companion object {
        @kotlin.jvm.JvmStatic
		val instance: GTSPlugin
            get() = Impactor.getInstance().registry.get(GTSPlugin::class.java)
    }
}