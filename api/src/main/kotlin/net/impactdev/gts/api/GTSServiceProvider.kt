package net.impactdev.gts.api

class GTSServiceProvider private constructor() {
    companion object {
        private var instance: GTSService? = null
        fun get(): GTSService {
            checkNotNull(instance) { "The GTS API is not loaded" }
            return instance!!
        }

        fun register(service: GTSService?) {
            instance = service
        }

        fun unregister() {
            instance = null
        }
    }

    init {
        throw UnsupportedOperationException("This class cannot be instantiated")
    }
}