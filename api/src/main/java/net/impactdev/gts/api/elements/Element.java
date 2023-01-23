package net.impactdev.gts.api.elements;

import net.impactdev.gts.api.storage.StorableContent;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;
import net.kyori.adventure.text.ComponentLike;

public interface Element extends StorableContent, ComponentLike, PrettyPrinter.IPrettyPrintable {}
