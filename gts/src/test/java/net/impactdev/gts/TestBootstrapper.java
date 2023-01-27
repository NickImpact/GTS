package net.impactdev.gts;

import net.impactdev.gts.plugin.AbstractGTSPlugin;
import net.impactdev.gts.plugin.bootstrapper.GTSBootstrapper;
import net.impactdev.impactor.api.logging.SystemLogger;
import net.impactdev.impactor.api.plugin.ImpactorPlugin;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class TestBootstrapper extends GTSBootstrapper implements BeforeAllCallback, AfterAllCallback, ExtensionContext.Store.CloseableResource {

    private static final Path root = Paths.get("src").resolve("test");
    private static final Lock LOCK = new ReentrantLock();
    private static volatile boolean initialized;

    public TestBootstrapper() {
        super(
                new SystemLogger(),
                root.resolve("config"),
                root.resolve("data")
        );
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        LOCK.lock();
        try {
            if(!initialized) {
                initialized = true;
                context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).put("GTS", this);

                // Initialization
                ImpactorPlugin impactor = ImpactorAPIBootstrap.construct();
                AbstractGTSPlugin plugin = new TestPlugin(this);

                impactor.construct();
                plugin.load();
            }
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        FileUtils.deleteDirectory(this.configDir().toFile());
        FileUtils.deleteDirectory(this.dataDir().toFile());
    }

    @Override
    public void close() throws Throwable {}

}
