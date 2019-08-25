package me.nickimpact.gts.api.util;

@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Exception;
}