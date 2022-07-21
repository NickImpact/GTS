package net.impactdev.gts.common.dependencies;

import net.impactdev.impactor.api.dependencies.Dependency;
import net.impactdev.impactor.api.dependencies.relocation.Relocation;

public class GTSDependencies {

    public static final Dependency MXPARSER = Dependency.builder()
            .name("MxParser")
            .group("org{}mariuszgromada{}math")
            .artifact("MathParser.org-mXparser")
            .version("4.4.2")
            .checksum("z+nZN08mJQ8UniReVzNorIApq3QhAUws6ZtNrtWR8dA=")
            .relocation(Relocation.of("org{}mariuszgromada{}math{}mxparser", "mxparser"))
            .build();

}
