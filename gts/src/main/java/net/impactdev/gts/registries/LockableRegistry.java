package net.impactdev.gts.registries;

import net.impactdev.gts.api.exceptions.RegistryLockedException;
import net.impactdev.gts.api.registries.Registry;

public abstract class LockableRegistry<K, V> implements Registry.Lockable<K, V> {

    private boolean locked;

    @Override
    public boolean register(K key, V value) {
        if(this.locked()) {
            throw new RegistryLockedException();
        }

        return this.register$child(key, value);
    }

    protected abstract boolean register$child(K key, V value);

    @Override
    public void lock() {
        this.locked = true;
    }

    @Override
    public boolean locked() {
        return this.locked;
    }

}
