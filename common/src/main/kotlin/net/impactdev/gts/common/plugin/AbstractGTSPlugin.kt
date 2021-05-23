package net.impactdev.gts.common.plugin

import net.impactdev.gts.api.storage.GTSStorage
import net.impactdev.gts.common.messaging.InternalMessagingService
import net.impactdev.gts.common.messaging.MessagingFactory
import net.impactdev.impactor.api.configuration.Config

abstract class AbstractGTSPlugin : GTSPlugin {
    private val config: Config? = null
    private val msgConfig: Config? = null
    private override val storage: GTSStorage? = null
    override var messagingService: InternalMessagingService? = null
        private set

    fun preInit() {
        ApiRegistrationUtil.register(GTSAPIProvider())
        //GTSService.getInstance().getRegistry().register(Blacklist.class, null);
    }

    fun init() {
        messagingService = messagingFactory.instance
    }

    fun postInit() {}
    fun started() {}
    fun shutdown() {}
    abstract val messagingFactory: MessagingFactory<*>
}