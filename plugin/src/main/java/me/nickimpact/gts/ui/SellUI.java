package me.nickimpact.gts.ui;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.gui.v2.*;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.utils.ItemUtils;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

public class SellUI implements PageDisplayable {

    private Player player;
    private Page display;

    public SellUI(Player player) {
	    this.player = player;

	    int height = 3;
	    List<Icon> options = this.fetchOptions();
	    int size = options.size();
        if(size > 7) {
            height = 5;
        }

        this.display = this.create(height);
	    this.display.define(options, InventoryDimension.of(7, 1), 1, 1);
	    if(height == 3) {
	    	for(int i = 10; i < 17; i++) {
			    this.display.getViews().get(0).setSlot(i, Icon.EMPTY);
		    }

        	int i = 0;
			for(int slot : Locations.getForSize(size).locations) {
				if(i >= options.size()) break;

				this.display.getViews().get(0).setSlot(slot + 9, options.get(i++));
			}
        }
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
        for(EntryClassification classification : er.getClassifications()) {
	        if(player.hasPermission("gts.command.sell." + classification.getPrimaryIdentifier().toLowerCase())) {
		        Icon icon = Icon.from(ItemStack.builder()
				        .itemType(ItemUtils.getOrDefaultFromRegistryID(classification.getItemRep()))
				        .add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, classification.getPrimaryIdentifier()))
				        .build()
		        );
		        icon.addListener(clickable -> {
			        this.display.close(player);
			        classification.getUi().createFor(player).open(player);
		        });
		        icons.add(icon);
	        }
        }

        return icons;
    }

    private enum Locations {
    	ZERO    (),
    	ONE     (4),
	    TWO     (3, 5),
	    THREE   (2, 4, 6),
	    FOUR    (1, 3, 5, 7),
	    FIVE    (0, 2, 4, 6, 8),
	    SIX     (1, 2, 3, 5, 6, 7),
	    SEVEN   (1, 2, 3, 4, 5, 6, 7);

	    private int[] locations;

    	Locations(int... slots) {
    		this.locations = slots;
	    }

	    public static Locations getForSize(int size) {
    		switch(size) {
			    case 0:
			    	return ZERO;
			    case 1:
			    	return ONE;
			    case 2:
			    	return TWO;
			    case 3:
			    	return THREE;
			    case 4:
			    	return FOUR;
			    case 5:
			    	return FIVE;
			    case 6:
			    	return SIX;
			    case 7:
			    default:
			    	return SEVEN;
		    }
	    }
    }
}
