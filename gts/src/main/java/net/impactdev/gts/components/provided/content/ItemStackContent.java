package net.impactdev.gts.components.provided.content;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.impactdev.gts.api.components.content.Content;
import net.impactdev.impactor.api.items.ImpactorItemStack;
import net.impactdev.impactor.api.items.builders.provided.BasicItemStackBuilder;
import net.impactdev.impactor.api.items.properties.MetaFlag;
import net.impactdev.impactor.api.items.properties.enchantments.Enchantment;
import net.impactdev.impactor.api.items.types.ItemType;
import net.impactdev.impactor.api.platform.sources.PlatformPlayer;
import net.impactdev.impactor.api.utilities.printing.PrettyPrinter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class ItemStackContent implements Content<ImpactorItemStack> {

    private static final Gson GSON = new GsonBuilder().create();

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
        return target.offer(this.content).successful();
    }

    @Override
    public boolean take(PlatformPlayer target) {
        return false;
    }

    @Override
    public int version() {
        return 3;
    }

    @Override
    public JsonObject serialize() {
        TagStringIO io = TagStringIO.get();

        CompoundBinaryTag nbt = CompoundBinaryTag.builder()
                .putString("id", this.content.type().key().asString())
                .putInt("quantity", this.content.quantity())
                .putString("title", MiniMessage.miniMessage().serialize(this.content.title()))
                .put("lore", ListBinaryTag.of(
                        BinaryTagTypes.STRING,
                        this.content.lore().stream()
                                .map(line -> StringBinaryTag.of(MiniMessage.miniMessage().serialize(line)))
                                .collect(Collectors.toList())
                ))
                .put("metadata", CompoundBinaryTag.builder()
                        .putBoolean("unbreakable", true)
                        .put("enchantments", ListBinaryTag.of(
                                BinaryTagTypes.COMPOUND,
                                this.content.enchantments().stream()
                                        .map(enchantment -> CompoundBinaryTag.builder()
                                                .putString("id", enchantment.type().asString())
                                                .putInt("level", enchantment.level())
                                                .build()
                                        )
                                        .collect(Collectors.toList())
                        ))
                        .putInt("flags", this.calculateFlagValue())
                        .put("nbt", this.content.nbt())
                        .build()
                )
                .build();

        try {
            return GSON.fromJson(io.asString(nbt), JsonObject.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize item stack content", e);
        }
    }

    public static ItemStackContent deserialize(JsonObject json) {
        String raw = json.getAsString();
        TagStringIO io = TagStringIO.get();

        try {
            CompoundBinaryTag nbt = io.asCompound(raw);
            BasicItemStackBuilder item = ImpactorItemStack.basic()
                    .type(ItemType.from(Key.key(nbt.getString("id"))))
                    .quantity(nbt.getInt("quantity"))
                    .title(MiniMessage.miniMessage().deserialize(nbt.getString("title")))
                    .lore(nbt.getList("lore", BinaryTagTypes.STRING).stream()
                            .map(tag -> MiniMessage.miniMessage().deserialize(((StringBinaryTag) tag).value()))
                            .collect(Collectors.toList())
                    );

            CompoundBinaryTag metadata = nbt.getCompound("metadata");
            if(metadata.getBoolean("unbreakable")) {
                item.unbreakable();
            }

            int flags = nbt.getInt("flags");
            for(MetaFlag flag : MetaFlag.values()) {
                if(((1 << flag.ordinal()) & flags) == 1) {
                    item.hide(flag);
                }
            }

            for(BinaryTag tag : nbt.getList("enchantments", BinaryTagTypes.COMPOUND)) {
                CompoundBinaryTag compound = (CompoundBinaryTag) tag;
                Enchantment enchantment = Enchantment.create(Key.key(compound.getString("id")), compound.getInt("level"));
                item.enchantment(enchantment);
            }

            ImpactorItemStack stack = item.build();
            return new ItemStackContent(stack);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize item stack content", e);
        }
    }

    @Override
    public void print(PrettyPrinter printer) {

    }

    private int calculateFlagValue() {
        int value = 0;
        for(MetaFlag flag : this.content.flags()) {
            value |= (1 << flag.ordinal());
        }

        return value;
    }
}
