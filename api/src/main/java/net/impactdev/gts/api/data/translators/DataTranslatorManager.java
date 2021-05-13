package net.impactdev.gts.api.data.translators;

import java.util.Collection;

public interface DataTranslatorManager {

    <T> Collection<DataTranslator<T>> get(Class<T> type);

    <T> void register(Class<T> type, DataTranslator<T> translator);

}
