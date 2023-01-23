package net.impactdev.gts.ui.views.listings;

import net.impactdev.gts.api.elements.listings.models.BuyItNow;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.gts.ui.templates.configs.ListingViewConfigKeys;
import net.impactdev.gts.ui.templates.designs.SectionedPaginationTemplate;
import net.impactdev.gts.ui.templates.registry.TemplateRegistration;
import net.impactdev.gts.util.GTSKeys;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.ui.containers.views.pagination.sectioned.SectionedPagination;
import net.impactdev.impactor.api.ui.containers.views.pagination.sectioned.builders.SectionBuilder;
import net.impactdev.impactor.api.ui.containers.views.pagination.sectioned.builders.SectionedPaginationBuilder;
import net.impactdev.impactor.api.ui.containers.views.pagination.updaters.PageUpdater;
import net.impactdev.impactor.api.utility.Context;
import net.kyori.adventure.key.Key;

import java.util.stream.Collectors;

public final class BINView extends ListingsView {
    @Override
    protected Key key() {
        return GTSKeys.gts("bin-listings");
    }

    public static TemplateRegistration<SectionedPagination> template() {
        return TemplateRegistration.builder(SectionedPagination.class)
                .config(Config.builder()
                        .path(GTSPlugin.instance()
                                .configurationDirectory()
                                .resolve("views")
                                .resolve("bin-listings.conf")
                        )
                        .provider(ListingViewConfigKeys.class)
                        .build()
                )
                .deserializer(BINViewTemplate::new)
                .build();
    }

    private static final class BINViewTemplate extends SectionedPaginationTemplate {

        private final ListingViewConfigKeys.PaginationSection listings;
        private final ListingViewConfigKeys.PaginationSection filters;

        BINViewTemplate(Config config) {
            super(config.get(ListingViewConfigKeys.TITLE), config.get(ListingViewConfigKeys.BACKGROUND));

            this.listings = config.get(ListingViewConfigKeys.LISTINGS);
            this.filters = config.get(ListingViewConfigKeys.FILTERS);
        }

        @Override
        protected SectionedPagination finalize(SectionedPaginationBuilder builder, Context context) {
            SectionBuilder listings = builder.section()
                    .dimensions(this.listings.area())
                    .offset(this.listings.offset());

            this.listings.updaters().forEach((type, template) -> {
                template.slots().placements().forEach((placement, slots) -> {
                    slots.forEach(slot -> {
                        switch (placement) {
                            case SLOTS:
                                listings.updater(PageUpdater.builder()
                                        .type(type)
                                        .slot(slot)
                                        .provider(target -> {
                                            Context relative = Context.empty().with(context);
                                            relative.append(Integer.class, target);

                                            return template.createItemStack(relative);
                                        })
                                        .build()
                                );

                                break;
                            case ROWS:
                                int start = 9 * (slot - 1);
                                for(int i = 0; i < 9; i++) {
                                    listings.updater(PageUpdater.builder()
                                            .type(type)
                                            .slot(start + i)
                                            .provider(target -> {
                                                Context relative = Context.empty().with(context);
                                                relative.append(Integer.class, target);

                                                return template.createItemStack(relative);
                                            })
                                            .build()
                                    );
                                }

                                break;
                            case COLUMNS:
                                for(int i = (slot - 1); i < (9 * 6); i += 9) {
                                    listings.updater(PageUpdater.builder()
                                            .type(type)
                                            .slot(i)
                                            .provider(target -> {
                                                Context relative = Context.empty().with(context);
                                                relative.append(Integer.class, target);

                                                return template.createItemStack(relative);
                                            })
                                            .build()
                                    );
                                }

                                break;
                        }
                    });
                });
            });
            listings.contents(GTSPlugin.instance().listings()
                    .fetch(listing -> listing instanceof BuyItNow)
                    .stream()
                    .map(listing -> listing.asIcon().listener(ctx -> {
                        // TODO - Open selected listing view
                        return false;
                    }))
                    .collect(Collectors.toList())
            );
            listings.complete();

            return builder.build();
        }
    }
}
