package net.impactdev.gts.ui.icons;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.gts.ui.icons.deserializers.IconTemplateDeserializerRegistry;
import net.impactdev.gts.ui.icons.functions.IconFunction;
import net.impactdev.impactor.api.items.ImpactorItemStack;
import net.impactdev.impactor.api.items.types.ItemType;
import net.impactdev.impactor.api.text.TextProcessor;
import net.impactdev.impactor.api.ui.containers.Icon;
import net.impactdev.impactor.api.ui.containers.Layout;
import net.impactdev.impactor.api.ui.containers.layouts.ChestLayout;
import net.impactdev.impactor.api.utility.Context;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;
import java.util.Map;

public final class IconTemplate {

    private final ItemType type;

    private final String title;
    private final List<String> lore;
    private final CompoundBinaryTag nbt;

    private final SlotResolver slots;
    private final IconFunction function;

    IconTemplate(ItemType type, String title, List<String> lore, CompoundBinaryTag nbt, SlotResolver slots, IconFunction function) {
        this.type = type;
        this.title = title;
        this.lore = lore;
        this.nbt = nbt;
        this.slots = slots;
        this.function = function;
    }

    public static IconTemplate fromConfiguration(ConfigurationNode node) {
        try {
            return IconTemplateDeserializerRegistry.deserializer(node.node("type").getString("fallback"))
                    .resolve(node)
                    .generate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ImpactorItemStack createItemStack(Context context) {
        TextProcessor processor = GTSPlugin.instance().translations().processor();

        return ImpactorItemStack.basic()
                .type(this.type)
                .title(!this.title.isEmpty() ? processor.parse(this.title, context) : Component.empty())
                .lore(!this.lore.isEmpty() ? processor.parse(this.lore, context) : Lists.newArrayList())
                .nbt(this.nbt)
                .build();
    }

    public Icon create(Context context) {
        Icon.IconBuilder builder = Icon.builder().display(() -> this.createItemStack(context));

        if(this.function != null) {
            builder.listener(ctx -> {
                this.function.apply(ctx);

                return false;
            });
        }

        return builder.build();
    }

    public SlotResolver slots() {
        return this.slots;
    }

    public static final class SlotResolver {

        private final Map<SlotPlacementType, List<Integer>> placements;

        private SlotResolver(Map<SlotPlacementType, List<Integer>> placements) {
            this.placements = placements;
        }

        public static SlotResolver slots(ConfigurationNode node) {
            Map<SlotPlacementType, List<Integer>> slots = Maps.newHashMap();

            try {
                if(node.hasChild("slots")) {
                    slots.put(SlotPlacementType.SLOTS, node.node("slots").getList(Integer.class));
                }

                if(node.hasChild("rows")) {
                    slots.put(SlotPlacementType.ROWS, node.node("rows").getList(Integer.class));
                }

                if(node.hasChild("columns")) {
                    slots.put(SlotPlacementType.COLUMNS, node.node("columns").getList(Integer.class));
                }

                return new SlotResolver(slots);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to parse slot configuration", e);
            }
        }

        public Map<SlotPlacementType, List<Integer>> placements() {
            return this.placements;
        }
    }

    public enum SlotPlacementType {

        SLOTS(Layout.LayoutBuilder::slot),
        ROWS(ChestLayout.ChestLayoutBuilder::row),
        COLUMNS(ChestLayout.ChestLayoutBuilder::column);

        private final LayoutAppender appender;

        SlotPlacementType(LayoutAppender appender) {
            this.appender = appender;
        }

        public void append(ChestLayout.ChestLayoutBuilder builder, Icon icon, List<Integer> placements) {
            for(int field : placements) {
                this.appender.apply(builder, icon, field);
            }
        }

        @FunctionalInterface
        private interface LayoutAppender {

            void apply(ChestLayout.ChestLayoutBuilder builder, Icon icon, int slot);

        }

    }

}
