package net.impactdev.gts.ui.templates.designs;

import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.gts.ui.icons.IconTemplate;
import net.impactdev.gts.ui.templates.Template;
import net.impactdev.gts.util.GTSKeys;
import net.impactdev.impactor.api.platform.sources.PlatformPlayer;
import net.impactdev.impactor.api.text.TextProcessor;
import net.impactdev.impactor.api.ui.containers.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.ChestLayout;
import net.impactdev.impactor.api.ui.containers.views.pagination.sectioned.SectionedPagination;
import net.impactdev.impactor.api.ui.containers.views.pagination.sectioned.builders.SectionedPaginationBuilder;
import net.impactdev.impactor.api.utility.Context;

import java.util.List;

public abstract class SectionedPaginationTemplate implements Template<SectionedPagination> {

    private final String title;
    private final List<IconTemplate> background;

    protected SectionedPaginationTemplate(final String title, final List<IconTemplate> background) {
        this.title = title;
        this.background = background;
    }

    protected abstract SectionedPagination finalize(SectionedPaginationBuilder builder, Context context);

    @Override
    public SectionedPagination generate(PlatformPlayer viewer) {
        SectionedPaginationBuilder builder = SectionedPagination.builder();
        TextProcessor processor = GTSPlugin.instance().translations().processor();
        Context context = Context.empty().append(PlatformPlayer.class, viewer);

        builder.provider(GTSKeys.gts("gts"));
        builder.title(processor.parse(this.title, context));
        builder.viewer(viewer);

        ChestLayout.ChestLayoutBuilder layout = ChestLayout.builder().size(6);
        for(IconTemplate template : this.background) {
            Icon icon = template.create(context);
            template.slots().placements().forEach((type, slots) -> type.append(layout, icon, slots));
        }
        builder.layout(layout.build());

        return this.finalize(builder, context);
    }
}
