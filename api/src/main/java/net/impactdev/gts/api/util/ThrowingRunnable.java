package net.impactdev.gts.api.util;

@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Exception;
}