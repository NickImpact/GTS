package net.impactdev.gts.api.data.translators;


import java.util.Optional;

@FunctionalInterface
public interface DataTranslator<T> {

    Optional<T> translate(T input);

}
