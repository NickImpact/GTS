package net.impactdev.gts.common.utils

@FunctionalInterface
interface EconomicFormatter {
    fun format(amount: Double): String?
}