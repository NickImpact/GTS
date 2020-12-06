package net.impactdev.gts.listeners;

import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.UpdateAnvilEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class AnvilRenameListener {

    @Listener
    public void onAnvilRename(UpdateAnvilEvent event) {
        ItemStackSnapshot result = event.getResult().getFinal();
        if(!result.isEmpty()) {
            if(!event.getItemName().isEmpty()) {
                net.minecraft.item.ItemStack mc = (net.minecraft.item.ItemStack) (Object) (result.createStack());
                NBTTagCompound nbt = new NBTTagCompound();
                mc.writeToNBT(nbt);
                nbt.getCompoundTag("tag").setBoolean("GTS-Anvil", true);
                mc.deserializeNBT(nbt);

                event.getResult().setCustom(((ItemStack) (Object) mc).createSnapshot());
            }
        }
    }
}
