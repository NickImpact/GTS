package net.impactdev.gts.elements.content;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import jdk.nashorn.internal.scripts.JO;
import net.impactdev.gts.api.elements.content.Content;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.gts.util.GTSKeys;
import net.impactdev.gts.util.nbt.NBTPrinter;
import net.impactdev.impactor.api.items.ImpactorItemStack;
import net.impactdev.impactor.api.items.builders.provided.BasicItemStackBuilder;
import net.impactdev.impactor.api.items.properties.MetaFlag;
import net.impactdev.impactor.api.items.properties.enchantments.Enchantment;
import net.impactdev.impactor.api.items.types.ItemType;
import net.impactdev.impactor.api.platform.sources.PlatformPlayer;
import net.impactdev.impactor.api.utility.ExceptionPrinter;
import net.impactdev.impactor.api.utility.printing.JsonPrinter;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;
import net.impactdev.json.JArray;
import net.impactdev.json.JObject;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ItemStackContent implements Content<ImpactorItemStack> {

    public static final Key KEY = GTSKeys.gts("item");
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
        JObject json = new JObject()
                .add("key", KEY.asString())
                .add("version", this.version())
                .add("id", this.content.type().key().asString())
                .add("quantity", this.content.quantity())
                .add("title", MiniMessage.miniMessage().serialize(this.content.title()))
                .add("lore", new JArray().consume(a -> {
                    this.content.lore().forEach(component -> {
                        a.add(MiniMessage.miniMessage().serialize(component));
                    });
                }))
                .add("metadata", new JObject().consume(o -> {
                    o.add("unbreakable", this.content.unbreakable())
                        .add("enchantments", new JArray().consume(a -> {
                            this.content.enchantments().forEach(enchantment -> {
                                a.add(new JObject()
                                        .add("id", enchantment.type().asString())
                                        .add("level", enchantment.level())
                                );
                            });
                        }))
                        .add("flags", this.calculateFlagValue())
                        .consume(target -> {
                            try {
                                TagStringIO io = TagStringIO.get();
                                target.add("nbt", GSON.fromJson(io.asString(this.content.nbt()), JsonObject.class));
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to serialize item stack NBT", e);
                            }
                        });
                }));

        return json.toJson();
    }

    @SuppressWarnings("ALL")
    public static ItemStackContent deserialize(JsonObject json) {
        TagStringIO io = TagStringIO.get();

        try {
            JsonObject metadata = json.getAsJsonObject("metadata");
            JsonObject nbt = metadata.getAsJsonObject("nbt");
            List<Component> lore = Lists.newArrayList();
            json.getAsJsonArray("lore").forEach(element -> {
                lore.add(MiniMessage.miniMessage().deserialize(element.getAsString()));
            });

            List<Enchantment> enchantments = Lists.newArrayList();
            metadata.getAsJsonArray("enchantments").forEach(element -> {
                JsonObject object = element.getAsJsonObject();
                Enchantment enchantment = Enchantment.create(
                        Key.key(object.get("id").getAsString()),
                        object.get("level").getAsInt()
                );
                enchantments.add(enchantment);
            });

            List<MetaFlag> flags = Lists.newArrayList();
            int flag = metadata.get("flags").getAsInt();
            for(MetaFlag mf : MetaFlag.values()) {
                if(((flag >> mf.ordinal()) & 0x1) == 1) {
                    flags.add(mf);
                }
            }

            ImpactorItemStack stack = ImpactorItemStack.basic()
                    .type(ItemType.from(Key.key(json.get("id").getAsString())))
                    .title(MiniMessage.miniMessage().deserialize(json.get("title").getAsString()))
                    .lore(lore)
                    .quantity(json.get("quantity").getAsInt())
                    .enchantments(enchantments)
                    .unbreakable(metadata.get("unbreakable").getAsBoolean())
                    .hide(flags)
                    .nbt(io.asCompound(nbt.toString()))
                    .build();

            return new ItemStackContent(stack);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize item stack content", e);
        }
    }

    @Override
    public void print(PrettyPrinter printer) {
        printer.add("Item Type: %s", this.content.type().key().asString())
                .add("Quantity: %d", (Object) this.content.quantity())
                .consume(p -> {
                    Component title = this.content.title();
                    if(title.equals(Component.empty())) {
                        p.add("Title: N/A");
                    } else {
                        p.add(PlainTextComponentSerializer.plainText().serialize(title));
                    }
                })
                .consume(p -> {
                    if(!this.content.lore().isEmpty()) {
                        p.add("Lore: [")
                            .consume(child -> this.content.lore().forEach(component -> {
                                child.add(PlainTextComponentSerializer.plainText().serialize(component), 2);
                            }))
                            .add("]");
                    }
                })
                .consume(p -> {
                    if(!this.content.enchantments().isEmpty()) {
                        p.add("Enchantments: [");
                        this.content.enchantments().forEach(enchantment -> {
                            p.add(String.format(
                                    "%s %s (%d)",
                                    enchantment.type().value(),
                                    RomanNumber.toRoman(enchantment.level()),
                                    enchantment.level()
                            ), 2);
                        });
                        p.add("]");
                    }
                })
                .add("Metadata: {")
                .add(String.format("Unbreakable: %b", this.content.unbreakable()), 2)
                .consume(p -> {
                    if(!this.content.flags().isEmpty()) {
                        p.add("Flags: [", 2);
                        for(MetaFlag flag : this.content.flags()) {
                            p.add(flag.name(), 4);
                        }
                        p.add("]", 2);
                    }

                })
                .consume(p -> {
                    CompoundBinaryTag nbt = this.content.nbt();
                    if(!nbt.keySet().isEmpty()) {
                        NBTPrinter.print(p, nbt, 2);
                    }
                })
                .add("}");
    }

    private int calculateFlagValue() {
        int value = 0;
        for(MetaFlag flag : this.content.flags()) {
            value |= (1 << flag.ordinal());
        }

        return value;
    }

    private static final class RomanNumber {

        private final static TreeMap<Integer, String> map = new TreeMap<Integer, String>();

        static {

            map.put(1000, "M");
            map.put(900, "CM");
            map.put(500, "D");
            map.put(400, "CD");
            map.put(100, "C");
            map.put(90, "XC");
            map.put(50, "L");
            map.put(40, "XL");
            map.put(10, "X");
            map.put(9, "IX");
            map.put(5, "V");
            map.put(4, "IV");
            map.put(1, "I");

        }

        public static String toRoman(int number) {
            int l =  map.floorKey(number);
            if ( number == l ) {
                return map.get(number);
            }
            return map.get(l) + toRoman(number-l);
        }

    }
}
