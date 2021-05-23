package net.impactdev.gts.api.data.translators

interface DataTranslatorManager {
    operator fun <T> get(type: Class<T>?): Collection<DataTranslator<T>?>?
    fun <T> register(type: Class<T>?, translator: DataTranslator<T>?)
}