package net.impactdev.gts.common.utils;

import net.kyori.adventure.text.Component;

@FunctionalInterface
public interface EconomicFormatter {

    Component format(double amount);

}
