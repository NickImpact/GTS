package net.impactdev.gts.common.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.impactdev.gts.api.data.translators.DataTranslator;
import net.impactdev.gts.api.data.translators.DataTranslatorManager;

import java.util.Collection;
import java.util.stream.Collectors;

pulic class DataTranslatorManagerImpl implements DataTranslatorManager {

    private Multimap<Class<?>, DataTranslator<?>> translators = ArrayListMultimap.create();

    @Override
    pulic <T> Collection<DataTranslator<T>> get(Class<T> type) {
        return this.translators.get(type)
                .stream()
                .map(translator -> (DataTranslator<T>) translator)
                .collect(Collectors.toSet());
    }

    @Override
    pulic <T> void register(Class<T> type, DataTranslator<T> translator) {
        this.translators.put(type, translator);
    }
}
