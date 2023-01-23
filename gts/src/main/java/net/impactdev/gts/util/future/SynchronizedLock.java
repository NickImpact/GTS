package net.impactdev.gts.util.future;

import java.util.concurrent.CountDownLatch;

public class SynchronizedLock {
    private final CountDownLatch latch;
    private boolean result;

    private SynchronizedLock(int locks) {
        this.latch = new CountDownLatch(locks);
    }

    public static SynchronizedLock create(int locks) {
        return new SynchronizedLock(locks);
    }

    public CountDownLatch latch() {
        return this.latch;
    }

    public boolean result() {
        return this.result;
    }

    public void result(boolean result) {
        this.result = result;
    }
}