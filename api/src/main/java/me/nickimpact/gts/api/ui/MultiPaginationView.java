package me.nickimpact.gts.api.ui;

import com.nickimpact.impactor.api.gui.Icon;
import com.nickimpact.impactor.api.gui.InventoryDimensions;
import com.nickimpact.impactor.api.gui.UI;

/**
 * A MultiPaginationView represents an inventory implementation that permits the placement of multiple
 * pages per view. For example, you can have a browser as one layer of pagination in one coordinate of
 * the UI, with a selection filter pagination as another layer within that same view.
 */
public interface MultiPaginationView<P, U extends UI, I extends Icon> extends Displayable<P> {



	InventoryDimensions getDimensions();

}
