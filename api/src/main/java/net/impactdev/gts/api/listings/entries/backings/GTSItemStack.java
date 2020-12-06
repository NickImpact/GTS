package net.impactdev.gts.api.listings.entries.backings;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.impactdev.gts.api.data.Storable;
import net.impactdev.impactor.api.json.factory.JArray;
import net.impactdev.impactor.api.json.factory.JObject;
import net.impactdev.impactor.api.utilities.Builder;
import net.kyori.text.TextComponent;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class GTSItemStack implements Storable {

    private final String type;
    private final int quantity;
    private final Meta meta;
    private final Data data;

    private GTSItemStack(GTSItemStackBuilder builder) {
        this.type = builder.type;
        this.quantity = builder.quantity;
        this.meta = builder.meta;
        this.data = builder.data;
    }

    public String getItemType() {
        return this.type;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public Meta getMeta() {
        return this.meta;
    }

    public Optional<Data> getData() {
        return Optional.ofNullable(this.data);
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public JObject serialize() {
        JObject parent = new JObject();
        parent.add("type", this.getItemType());
        parent.add("quantity", this.getQuantity());
        parent.add("meta", this.meta.serialize());
        parent.consume(d -> {
            this.getData().ifPresent(data -> {
                d.add("data", data.serialize());
            });
        });
        parent.add("version", this.getVersion());

        return parent;
    }

    public static class GTSItemStackBuilder implements Builder<GTSItemStack, GTSItemStackBuilder> {

        private String type;
        private int quantity = 1;
        private Meta meta = new Meta();
        private Data data;

        public GTSItemStackBuilder type(String type) {
            this.type = type;
            return this;
        }

        public GTSItemStackBuilder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public GTSItemStackBuilder display(TextComponent name) {
            this.meta.display = name;
            return this;
        }

        public GTSItemStackBuilder lore(List<TextComponent> lore) {
            this.meta.lore = lore;
            return this;
        }

        public GTSItemStackBuilder enchantment(String id, int level) {
            if(this.meta.enchantments == null) {
                this.meta.enchantments = Lists.newArrayList();
            }

            this.meta.enchantments.add(new Enchantment(id, level));
            return this;
        }

        public GTSItemStackBuilder flags(String... flags) {
            if(this.meta.flags == null) {
                this.meta.flags = Lists.newArrayList();
            }

            for (String flag : flags) {
                Flag f = Flag.create(flag);
                if(f != null) {
                    this.meta.flags.add(f);
                }
            }
            return this;
        }

        public GTSItemStackBuilder unbreakable(boolean state) {
            this.meta.unbreakable = state;
            return this;
        }

        public GTSItemStackBuilder data(String id, byte value) {
            this.data = new Data(id, value);
            return this;
        }

        @Override
        public GTSItemStackBuilder from(GTSItemStack parent) {
            this.type = parent.getItemType();
            this.quantity = parent.getQuantity();
            this.meta = parent.getMeta();
            this.data = parent.getData().orElse(null);

            return this;
        }

        @Override
        public GTSItemStack build() {
            Preconditions.checkNotNull(this.type, "Item type was not specified");
            return new GTSItemStack(this);
        }

    }

    private static class Meta implements Storable {

        private TextComponent display;
        private List<TextComponent> lore;
        private List<Enchantment> enchantments;
        private List<Flag> flags;
        private boolean unbreakable;

        public Optional<TextComponent> getDisplayName() {
            return Optional.ofNullable(this.display);
        }

        public Optional<List<TextComponent>> getLore() {
            return Optional.ofNullable(this.lore);
        }

        public List<Enchantment> getEnchantments() {
            return this.enchantments;
        }

        public List<Flag> getFlags() {
            return this.flags;
        }

        public boolean isUnbreakable() {
            return this.unbreakable;
        }

        @Override
        public int getVersion() {
            return 1;
        }

        @Override
        public JObject serialize() {
            JObject parent = new JObject();

            JArray enchants = new JArray();
            for(Enchantment enchantment : this.getEnchantments()) {
                enchants.add(enchantment.serialize());
            }
            parent.add("enchantments", enchants);

            JArray flags = new JArray();
            for(Flag flag : this.getFlags()) {
                flags.add(flag.serialize());
            }
            parent.add("flags", flags);
            parent.add("unbreakable", this.isUnbreakable());

            parent.add("version", this.getVersion());
            return parent;
        }
    }

    private static class Enchantment implements Storable {

        private final String id;
        private final int level;

        public Enchantment(String id, int level) {
            this.id = id;
            this.level = level;
        }

        public String getID() {
            return this.id;
        }

        public int getLevel() {
            return this.level;
        }

        @Override
        public int getVersion() {
            return 1;
        }

        @Override
        public JObject serialize() {
            return new JObject()
                    .add("id", this.getID())
                    .add("level", this.getLevel())
                    .add("version", this.getVersion());
        }
    }

    private static class Flag implements Storable {

        private final String flag;

        private Flag(String key) {
            this.flag = key;
        }

        static Flag create(String key) {
            if(Arrays.stream(Flags.values()).anyMatch(f -> f.name().equals(key))) {
                return new Flag(key);
            }

            return null;
        }

        @Override
        public int getVersion() {
            return 1;
        }

        @Override
        public JObject serialize() {
            return new JObject()
                    .add("key", this.flag)
                    .add("version", this.getVersion());
        }

        public enum Flags {

            HIDE_ENCHANTS,
            HIDE_ATTRIBUTES,
            HIDE_CAN_PLACE,
            HIDE_CAN_DESTROY,
            HIDE_MISCELLANEOUS,
            HIDE_UNBREAKABLE,

        }

    }

    public static class Data implements Storable {

        private final String id;
        private final byte value;

        public Data(String id, byte value) {
            this.id = id;
            this.value = value;
        }

        public String getID() {
            return this.id;
        }

        public byte getValue() {
            return this.value;
        }

        @Override
        public int getVersion() {
            return 1;
        }

        @Override
        public JObject serialize() {
            return new JObject()
                    .add("id", this.id)
                    .add("value", this.value);
        }
    }

}
