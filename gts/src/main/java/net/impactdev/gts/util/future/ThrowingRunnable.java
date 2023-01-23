package net.impactdev.gts.util.future;

@FunctionalInterface
public interface ThrowingRunnable {

    void run() throws Exception;

}
