package net.impactdev.gts.listings.data;

import net.impactdev.gts.api.commands.CommandGenerator;
import net.impactdev.gts.common.plugin.GTSPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryTransformation;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.text.Text;

import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpongeItemSellExecutor implements CommandGenerator.EntryGenerator<ChosenItemEntry> {

    @Override
    public String[] getAliases() {
        return new String[] {
                "item",
                "items"
        };
    }

    @Override
    public ChosenItemEntry create(UUID source, Queue<String> args, Context context) throws Exception {
        Optional<Integer> slot = this.next(args, Integer::parseInt).map(x -> Math.max(0, Math.min(x - 1, 35)));
        Optional<Integer> amount = this.next(args, Integer::parseInt);
        Player player = Sponge.getServer().getPlayer(source).get();

        if(slot.isPresent()) {
            Inventory inventory = player.getInventory()
                    .transform(InventoryTransformation.builder()
                            .append(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class))
                            .append(QueryOperationTypes.INVENTORY_TYPE.of(GridInventory.class))
                            .build()
                    );

            Iterable<Slot> slots = inventory.slots();
            for (Slot s : slots) {
                if (s.getProperty(SlotIndex.class, "slotindex").map(i -> Objects.equals(i.getValue(), slot.get())).orElse(false)) {
                    AtomicBoolean isAmount = new AtomicBoolean();
                    Optional<ItemStack> result = s.peek()
                            .map(ItemStack::getQuantity)
                            .flatMap(quantity -> {
                                if(amount.filter(value -> value <= quantity).isPresent()) {
                                    return amount;
                                }

                                isAmount.set(true);
                                return Optional.empty();
                            })
                            .flatMap(s::peek);

                    return result.map(stack -> new ChosenItemEntry(stack.createSnapshot(), slot.get()))
                            .orElseThrow(() -> {
                                if(isAmount.get()) {
                                    return new IllegalStateException("Stack does not carry that amount of items!");
                                }

                                return new IllegalStateException("Unable to locate an item at the target slot!");
                            });
                }
            }

            throw new IllegalStateException("Unable to locate an item at the target slot!");
        } else {
            int index = player.toContainer().get(DataQuery.of("UnsafeData", "SelectedItemSlot"))
                    .map(value -> (int) value)
                    .orElse(-1);
            if(index == -1) {
                throw new IllegalStateException("Failed to locate current hand position");
            }
            Optional<ItemStack> hand = player.getItemInHand(HandTypes.MAIN_HAND);
            return hand.map(item -> new ChosenItemEntry(item.createSnapshot(), index))
                    .orElseThrow(() -> new IllegalStateException("No item in your hand"));
        }
    }
}
