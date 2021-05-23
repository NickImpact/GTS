package net.impactdev.gts.common.data

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import net.impactdev.gts.api.data.translators.DataTranslator
import net.impactdev.gts.api.data.translators.DataTranslatorManager
import java.util.stream.Collectors

class DataTranslatorManagerImpl : DataTranslatorManager {
    private val translators: Multimap<Class<*>?, DataTranslator<*>?> = ArrayListMultimap.create()
    override fun <T> get(type: Class<T>?): Collection<DataTranslator<T>?>? {
        return translators[type]
            .stream()
            .map { translator: DataTranslator<*>? -> translator as DataTranslator<T?>? }
            .collect(Collectors.toSet())
    }

    override fun <T> register(type: Class<T>?, translator: DataTranslator<T>?) {
        translators.put(type, translator)
    }
}