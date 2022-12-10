package net.impactdev.gts.service;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.modules.markets.ListingManager;
import net.impactdev.gts.api.extensions.Extension;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class GTSServiceImplementation implements GTSService {

    private final ListingManager manager;
    private final List<Extension> extensions = new ArrayList<>();

    @Inject
    public GTSServiceImplementation(final ListingManager manager) {
        this.manager = manager;
    }

    @Override
    public ListingManager manager() {
        return this.manager;
    }

    @Override
    public ImmutableList<Extension> extensions() {
        return ImmutableList.copyOf(this.extensions);
    }
}
