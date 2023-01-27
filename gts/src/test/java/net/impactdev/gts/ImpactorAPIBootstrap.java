package net.impactdev.gts;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.impactdev.impactor.api.logging.SystemLogger;
import net.impactdev.impactor.api.platform.Platform;
import net.impactdev.impactor.api.platform.PlatformComponent;
import net.impactdev.impactor.api.platform.PlatformInfo;
import net.impactdev.impactor.api.platform.PlatformType;
import net.impactdev.impactor.api.platform.performance.PerformanceMonitor;
import net.impactdev.impactor.api.platform.plugins.PluginMetadata;
import net.impactdev.impactor.api.plugin.ImpactorPlugin;
import net.impactdev.impactor.api.providers.FactoryProvider;
import net.impactdev.impactor.api.providers.ServiceProvider;
import net.impactdev.impactor.api.scheduler.AbstractJavaScheduler;
import net.impactdev.impactor.api.scheduler.SchedulerAdapter;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;
import net.impactdev.impactor.core.modules.ImpactorModule;
import net.impactdev.impactor.core.platform.ImpactorPlatform;
import net.impactdev.impactor.core.plugin.BaseImpactorPlugin;
import net.impactdev.impactor.core.plugin.ImpactorBootstrapper;
import net.impactdev.impactor.minecraft.items.ItemsModule;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;

public final class ImpactorAPIBootstrap {

    public static ImpactorPlugin construct() {
        return new TestImpactorBootstrap().createPlugin();
    }

    public static class TestImpactorBootstrap extends ImpactorBootstrapper {

        public TestImpactorBootstrap() {
            super(new SystemLogger());
        }

        @Override
        protected BaseImpactorPlugin createPlugin() {
            return new BaseImpactorPlugin(this) {
                @Override
                protected Set<Class<? extends ImpactorModule>> modules() {
                    return Sets.newHashSet(
                            TestPlatformModule.class,
                            ItemsModule.class
                    );
                }
            };
        }

        public static class TestPlatformModule implements ImpactorModule {

            @Override
            public void factories(FactoryProvider provider) {
                provider.register(SchedulerAdapter.class, new AbstractJavaScheduler(new SystemLogger()) {
                    @Override
                    public Executor sync() {
                        return null;
                    }
                });
            }

            @Override
            public void services(ServiceProvider provider) {
                provider.register(Platform.class, new ImpactorPlatform(new TestPlatformInfo()) {
                    @Override
                    public PlatformInfo info() {
                        return super.info();
                    }

                    @Override
                    public PerformanceMonitor performance() {
                        return super.performance();
                    }
                });
            }
        }

        public static class TestPlatformInfo implements PlatformInfo {

            @Override
            public PlatformType type() {
                return PlatformType.JUNIT_TESTING;
            }

            @Override
            public Set<PlatformComponent> components() {
                return Sets.newHashSet();
            }

            @Override
            public List<PluginMetadata> plugins() {
                return Lists.newArrayList();
            }

            @Override
            public Optional<PluginMetadata> plugin(String id) {
                return Optional.empty();
            }

            @Override
            public void print(PrettyPrinter printer) {

            }
        }
    }
}
