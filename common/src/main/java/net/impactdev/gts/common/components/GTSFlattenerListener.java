package net.impactdev.gts.common.components;

import net.kyori.adventure.text.flattener.FlattenerListener;
import org.jetbrains.annotations.NotNull;

public class GTSFlattenerListener implements FlattenerListener {

    private final StringBuilder result = new StringBuilder();

    @Override
    public void component(@NotNull String text) {
        this.result.append(text);
    }

    public String result() {
        return result.toString();
    }

}
