package net.impactdev.gts.sponge.pricing.provided;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.data.registry.GTSKeyMarker;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.json.factory.JObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.EconomyService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@GTSKeyMarker("currency")
public class MonetaryEntry extends SpongeEntry<BigDecimal> {

    private static EconomyService economy;
    private BigDecimal amount;

    public MonetaryEntry(double amount) {
        this.amount = new BigDecimal(amount);
    }

    public static void setEconomy(EconomyService economy) {
        MonetaryEntry.economy = economy;
    }

    @Override
    public BigDecimal getOrCreateElement() {
        return this.amount;
    }

    @Override
    public TextComponent getName() {
        return Component.text()
                .append(economy.defaultCurrency().format(this.getOrCreateElement()))
                .build();
    }

    @Override
    public TextComponent getDescription() {
        return null;
    }

    @Override
    public Display<ItemStack> getDisplay(UUID viewer) {
        return () -> ItemStack.builder()
                .itemType(ItemTypes.GOLD_INGOT)
                .add(Keys.CUSTOM_NAME, this.getName())
                .build();
    }

    @Override
    public boolean give(UUID receiver) {
        economy.findOrCreateAccount(receiver).get().deposit(economy.defaultCurrency(), this.amount);
        return true;
    }

    @Override
    public boolean take(UUID depositor) {
        return false;
    }

    @Override
    public Optional<String> getThumbnailURL() {
        return Optional.empty();
    }

    @Override
    public List<String> getDetails() {
        return Lists.newArrayList();
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public JObject serialize() {
        return new JObject()
                .add("version", this.getVersion())
                .add("value", this.amount);
    }
}
