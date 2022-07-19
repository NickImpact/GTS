package net.impactdev.gts.listeners;

import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.UpdateAnvilEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class AnvilRenameListener {

    @Listener
    public void onAnvilRename(UpdateAnvilEvent event) {
        ItemStackSnapshot result = event.result().finalReplacement();
        if(!result.isEmpty()) {
            if(!event.itemName().isEmpty()) {
                net.minecraft.item.ItemStack mc = (net.minecraft.item.ItemStack) (Object) (result.createStack());
                CompoundNBT nbt = new CompoundNBT();
                mc.save(nbt);
                nbt.getCompound("tag").putBoolean("GTS-Anvil", true);
                mc = net.minecraft.item.ItemStack.of(nbt);

                event.result().setCustom(((ItemStack) (Object) mc).createSnapshot());
            }
        }
    }
}
