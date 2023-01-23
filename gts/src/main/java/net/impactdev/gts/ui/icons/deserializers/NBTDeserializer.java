package net.impactdev.gts.ui.icons.deserializers;

import net.impactdev.gts.ui.icons.TemplateProperties;
import net.impactdev.impactor.api.items.types.ItemType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minecraft.nbt.NbtUtils;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class NBTDeserializer extends AbstractSerializer {

    @Override
    @SuppressWarnings("PatternValidation")
    public TemplateProperties resolve(ConfigurationNode node) throws Exception {
        TemplateProperties properties = super.resolve(node);
        properties.type = ItemType.from(Key.key(node.node("key").getString("minecraft:stone")));

        ConfigurationNode root = node.node("nbt");
        if(root.virtual()) {
            properties.nbt = CompoundBinaryTag.empty();
        }

        CompoundBinaryTag result = CompoundBinaryTag.empty();
        List<String> keys = root.childrenMap().keySet().stream().map(Object::toString).collect(Collectors.toList());
        for(String key : keys) {
            ConfigurationNode child = root.node(key);
            if(root.isMap()) {
                result = result.put(key, parse(child));
            } else if(root.isList()) {
                ListBinaryTag array = ListBinaryTag.empty();
                root.childrenList().forEach(c -> array.add(parse(c)));
                result = result.put(key, array);
            } else {
                throw new IllegalStateException("Leaf nodes must have a specified typing");
            }
        }

        properties.nbt = result;
        return properties;
    }

    private BinaryTag parse(ConfigurationNode node) {
        if(node.isMap()) {
            // This could be either a leaf node or another mapping set. We need to first
            // check if any secondary maps exist within the structure. If not, we can safely
            // state this is a leaf node

            boolean leaf = node.childrenMap().values().stream().noneMatch(child -> child.isMap() || child.isList());
            if(leaf) {
                return TagWriter.create(node);
            } else {
                AtomicReference<CompoundBinaryTag> compound = new AtomicReference<>(CompoundBinaryTag.empty());
                node.childrenMap().forEach((key, child) -> compound.set(compound.get().put(key.toString(), parse(child))));
                return compound.get();
            }
        } else if (node.isList()) {
            AtomicReference<ListBinaryTag> list = new AtomicReference<>(ListBinaryTag.empty());
            node.childrenList().forEach(child -> list.set(list.get().add(parse(child))));
            return list.get();
        }

        throw new IllegalArgumentException("Node must either be a map or list");
    }

    private enum TagWriter {
        STRING(node -> StringBinaryTag.of(node.getString(""))),
        BYTE(node -> ByteBinaryTag.of((byte) node.getInt())),
        UUID(node -> {
            String input = node.getString("random");

            UUID result;
            if(input.equals("random")) {
                result = java.util.UUID.randomUUID();
            } else {
                result = java.util.UUID.fromString(input);
            }

            return IntArrayBinaryTag.of(NbtUtils.createUUID(result).getAsIntArray());
        })
        ;

        private final Function<ConfigurationNode, BinaryTag> processor;

        TagWriter(final Function<ConfigurationNode, BinaryTag> processor) {
            this.processor = processor;
        }

        static BinaryTag create(ConfigurationNode node) {
            String type = node.node("type").getString();
            if(type == null) {
                throw new IllegalArgumentException("Invalid leaf node NBT definition");
            }

            return Arrays.stream(values())
                    .filter(writer -> writer.name().equalsIgnoreCase(type))
                    .map(writer -> writer.processor.apply(node.node("value")))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid leaf node NBT definition"));
        }
    }

}
