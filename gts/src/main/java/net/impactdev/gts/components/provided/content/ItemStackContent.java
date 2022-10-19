package net.impactdev.gts.components.provided.content;

import com.google.gson.JsonObject;
import net.impactdev.gts.api.components.content.Content;
import net.impactdev.impactor.api.items.ImpactorItemStack;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class ItemStackContent implements Content<ImpactorItemStack> {

    private final ImpactorItemStack content;

    public ItemStackContent(ImpactorItemStack content) {
        this.content = content;
    }

    @Override
    public ImpactorItemStack content() {
        return this.content;
    }

    @Override
    public @NotNull Component asComponent() {
        return this.content.title();
    }

    @Override
    public ImpactorItemStack display() {
        return this.content;
    }

    @Override
    public boolean reward(PlatformPlayer target) {
//        return target.offer(this.content).received();
        return false;
    }

    @Override
    public boolean take(PlatformPlayer target) {
        // TODO - We need to find a way to include vanilla in Impactor API and apply mappings
        return false;
    }

    @Override
    public int version() {
        return 3;
    }

    @Override
    public JsonObject serialize() {
        return null;
    }

    @Override
    public void print(PrettyPrinter printer) {

    }
}
