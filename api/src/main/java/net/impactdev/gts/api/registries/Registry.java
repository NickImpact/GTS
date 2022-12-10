package net.impactdev.gts.api.registries;

public interface Registry<K, V> {

    void init() throws Exception;

    boolean register(K key, V value);

    interface Lockable<K, V> extends Registry<K, V> {

        void lock();

        boolean locked();

    }

}
