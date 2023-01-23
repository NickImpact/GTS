package net.impactdev.gts.ui.templates.designs;

import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.gts.ui.icons.IconTemplate;
import net.impactdev.gts.ui.templates.Template;
import net.impactdev.gts.util.GTSKeys;
import net.impactdev.impactor.api.platform.sources.PlatformPlayer;
import net.impactdev.impactor.api.text.TextProcessor;
import net.impactdev.impactor.api.ui.containers.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.ChestLayout;
import net.impactdev.impactor.api.ui.containers.views.ChestView;
import net.impactdev.impactor.api.utility.Context;

import java.util.List;

public class ChestTemplate implements Template<ChestView> {

    private final String title;
    private final int rows;
    private final List<IconTemplate> icons;

    private ChestTemplate(String title, int rows, List<IconTemplate> icons) {
        this.title = title;
        this.rows = rows;
        this.icons = icons;
    }

    public static ChestTemplate create(String title, int rows, List<IconTemplate> icons) {
        return new ChestTemplate(title, rows, icons);
    }

    public ChestView generate(PlatformPlayer viewer) {
        ChestView.ChestViewBuilder builder = ChestView.builder();
        TextProcessor processor = GTSPlugin.instance().translations().processor();
        Context context = Context.empty().append(PlatformPlayer.class, viewer);

        builder.provider(GTSKeys.gts("gts")).title(processor.parse(this.title, context));

        ChestLayout.ChestLayoutBuilder layout = ChestLayout.builder().size(this.rows);
        for(IconTemplate template : this.icons) {
            Icon icon = template.create(context);
            template.slots().placements().forEach((type, slots) -> type.append(layout, icon, slots));
        }
        builder.layout(layout.build());

        return builder.build();
    }

}
