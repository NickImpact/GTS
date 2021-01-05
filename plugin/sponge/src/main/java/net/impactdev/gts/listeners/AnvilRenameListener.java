package net.impactdev.gts.listeners;

import net.impactdev.gts.api.util.PrettyPrinter;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.listings.SpongeItemEntry;
import net.impactdev.gts.listings.data.NBTTranslator;
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
                mc = new net.minecraft.item.ItemStack(nbt);

                event.getResult().setCustom(((ItemStack) (Object) mc).createSnapshot());
            }
        }
    }
}
