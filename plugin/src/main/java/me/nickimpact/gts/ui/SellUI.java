package me.nickimpact.gts.ui;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.gui.v2.Icon;
import com.nickimpact.impactor.gui.v2.Layout;
import com.nickimpact.impactor.gui.v2.Page;
import com.nickimpact.impactor.gui.v2.PageDisplayable;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.api.listings.entries.Entry;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

public class SellUI implements PageDisplayable {

    private Player player;
    private Page display;

    public SellUI(Player player) {
        int height = 3;
        int size = GTS.getInstance().getService().getEntryRegistry().getRegistry().getTypings().size();
        if(size > 7) {
            height = 5;
        }

        this.player = player;
        this.display = this.create(height);
        this.display.define(this.fetchOptions(), InventoryDimension.of(7, 1), 1, 1);
    }

    @Override
    public Page getDisplay() {
        return this.display;
    }

    private Page create(int height) {
        Page.Builder pb = Page.builder()
                .property(InventoryDimension.of(9, height))
                .layout(this.layout(height))
                .property(InventoryTitle.of(Text.of(TextColors.RED, "Select What to Sell...")));

        if(height == 5) {
            // Apply paging options
        }

        return pb.build(GTS.getInstance());
    }

    private Layout layout(int height) {
        return Layout.builder().dimension(9, height).border().row(Icon.BORDER, height == 5 ? 2 : 0).build();
    }

    private List<Icon> fetchOptions() {
        List<Icon> icons = Lists.newArrayList();

        EntryRegistry er = GTS.getInstance().getService().getEntryRegistry();
        for(Class<? extends Entry> clazz : er.getRegistry().getTypings().values()) {
            Icon icon = Icon.from(er.getReps().get(clazz));
            icon.addListener(clickable -> {
                this.display.close(player);
                er.getUis().get(clazz).createFor(player).open(player);
            });
            icons.add(icon);
        }

        return icons;
    }
}
