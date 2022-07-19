package net.impactdev.gts.common.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.impactdev.gts.api.data.translators.DataTranslator;
import net.impactdev.gts.api.data.translators.DataTranslatorManager;

import java.util.Collection;
import java.util.stream.Collectors;

public class DataTranslatorManagerImpl implements DataTranslatorManager {

    private final Multimap<Class<?>, DataTranslator<?>> translators = ArrayListMultimap.create();

    @Override
    public <T> Collection<DataTranslator<T>> get(Class<T> type) {
        return this.translators.get(type)
                .stream()
                .map(translator -> (DataTranslator<T>) translator)
                .collect(Collectors.toSet());
    }

    @Override
    public <T> void register(Class<T> type, DataTranslator<T> translator) {
        this.translators.put(type, translator);
    }
}
