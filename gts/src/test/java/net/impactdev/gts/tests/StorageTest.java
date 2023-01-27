package net.impactdev.gts.tests;

import com.google.gson.JsonObject;
import net.impactdev.gts.api.elements.content.Price;
import net.impactdev.gts.api.elements.listings.Listing;
import net.impactdev.gts.api.elements.listings.models.BuyItNow;
import net.impactdev.gts.elements.content.ItemStackContent;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.impactor.api.items.ImpactorItemStack;
import net.impactdev.impactor.api.items.properties.MetaFlag;
import net.impactdev.impactor.api.items.types.ItemTypes;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;
import net.impactdev.json.JObject;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class StorageTest {

    @Test
    public void createAndVerifyItemListing() {
        BuyItNow bin = BuyItNow.builder()
                .id(UUID.randomUUID())
                .lister(UUID.randomUUID())
                .published(LocalDateTime.now())
                .expiration(LocalDateTime.now())
                .content(new ItemStackContent(ImpactorItemStack.basic()
                        .type(ItemTypes.GRASS_BLOCK)
                        .title(Component.empty())
                        .unbreakable()
                        .hide(MetaFlag.DYE, MetaFlag.CAN_DESTROY)
                        .nbt(CompoundBinaryTag.builder()
                                .putInt("test", 5)
                                .putIntArray("intArray", new int[]{1, 2, 3})
                                .putByte("byte", (byte) 1)
                                .putShort("short", (short) 3)
                                .build()
                        )
                        .build()
                ))
                .price(new Price() {
                    @Override
                    public int version() {
                        return 1;
                    }

                    @Override
                    public JsonObject serialize() {
                        return new JObject().add("version", this.version()).toJson();
                    }

                    @Override
                    public @NotNull Component asComponent() {
                        return Component.empty();
                    }
                })
                .build();

        GTSPlugin.instance().storage().publishListing(bin).join();
        List<Listing> listings = GTSPlugin.instance().storage().listings().join();
        assertFalse(listings.isEmpty());

        ItemStackContent content = (ItemStackContent) listings.get(0).content();
        assertEquals(ItemTypes.GRASS_BLOCK, content.content().type());
        assertEquals(1, content.content().quantity());
        assertEquals(Component.empty(), content.asComponent());

        PrettyPrinter printer = new PrettyPrinter(80);
        printer.title("ItemStack Listing Creation Test");
        printer.add("Item Details").center().hr('-');
        content.print(printer);

        printer.log(GTSPlugin.instance().logger());
    }
}
