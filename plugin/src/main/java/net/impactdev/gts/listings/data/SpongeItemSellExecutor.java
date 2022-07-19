package net.impactdev.gts.listings.data;

import net.impactdev.gts.api.commands.CommandGenerator;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.PrimaryPlayerInventory;
import org.spongepowered.api.item.inventory.type.GridInventory;

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
        ServerPlayer player = Sponge.server().player(source).get();

        if(slot.isPresent()) {
            PrimaryPlayerInventory parent = player.inventory().primary();
            Inventory transform = parent.hotbar().union(parent.storage());

            Iterable<Slot> slots = transform.slots();
            for (Slot s : slots) {
                if (s.get(Keys.SLOT_INDEX).map(i -> Objects.equals(i, slot.get())).orElse(false)) {
                    AtomicBoolean isAmount = new AtomicBoolean();
                    Optional<ItemStack> result = Optional.of(s.peek())
                            .filter(item -> !item.isEmpty())
                            .map(ItemStack::quantity)
                            .flatMap(quantity -> {
                                if(amount.filter(value -> value <= quantity).isPresent()) {
                                    return amount;
                                }

                                isAmount.set(true);
                                return Optional.empty();
                            })
                            .flatMap(value -> Optional.of(ItemStack.builder()
                                    .from(s.peek())
                                    .quantity(value)
                                    .build()
                            ));

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

            ItemStack hand = player.itemInHand(HandTypes.MAIN_HAND);
            return Optional.of(hand)
                    .filter(item -> !item.isEmpty())
                    .map(item -> new ChosenItemEntry(item.createSnapshot(), index))
                    .orElseThrow(() -> new IllegalStateException("No item in your hand"));
        }
    }
}
