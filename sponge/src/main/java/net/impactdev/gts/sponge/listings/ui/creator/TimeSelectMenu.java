package net.impactdev.gts.sponge.listings.ui.creator;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import net.impactdev.gts.sponge.listings.ui.AbstractSpongeEntryUI;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.gui.signs.SignQuery;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.utilities.Time;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongeUI;
import net.impactdev.gts.common.config.updated.ConfigKeys;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.config.updated.types.time.TimeKey;
import net.impactdev.gts.common.config.updated.types.time.TimeLanguageOptions;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.utils.Utilities;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;

import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class TimeSelectMenu {

    private final SpongeIcon lowest = this.create(ConfigKeys.LISTING_TIME_LOWEST, DyeColors.RED);
    private final SpongeIcon low = this.create(ConfigKeys.LISTING_TIME_LOW, DyeColors.ORANGE);
    private final SpongeIcon mid = this.create(ConfigKeys.LISTING_TIME_MID, DyeColors.YELLOW);
    private final SpongeIcon high = this.create(ConfigKeys.LISTING_TIME_HIGH, DyeColors.LIME);
    private final SpongeIcon highest = this.create(ConfigKeys.LISTING_TIME_HIGHEST, DyeColors.LIGHT_BLUE);

    private final Player viewer;
    private final SpongeUI display;

    private final AbstractSpongeEntryUI<?> parent;
    private final BiConsumer<AbstractSpongeEntryUI<?>, Time> callback;

    public TimeSelectMenu(Player viewer, AbstractSpongeEntryUI<?> parent, BiConsumer<AbstractSpongeEntryUI<?>, Time> callback) {
        MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        this.viewer = viewer;
        this.parent = parent;
        this.callback = callback;
        this.display = SpongeUI.builder()
                .title(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_TIME_SELECT_TITLE)))
                .dimension(InventoryDimension.of(9, 3))
                .build()
                .define(this.layout());
    }

    public void open() {
        this.display.open(this.viewer);
    }

    private SpongeLayout layout() {
        SpongeLayout.SpongeLayoutBuilder builder = SpongeLayout.builder();
        builder.dimension(9, 3).border().slot(SpongeIcon.BORDER, 15);
        builder.slot(this.lowest, 10);
        builder.slot(this.low, 11);
        builder.slot(this.mid, 12);
        builder.slot(this.high, 13);
        builder.slot(this.highest, 14);

        MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        ItemStack custom = ItemStack.builder()
                .itemType(ItemTypes.CLOCK)
                .add(Keys.DISPLAY_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.CUSTOM_TIME_TITLE)))
                .add(Keys.ITEM_LORE, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.CUSTOM_TIME_LORE)))
                .build();
        SpongeIcon icon = new SpongeIcon(custom);
        icon.addListener(clickable -> {
            AtomicReference<Time> result = new AtomicReference<>();
            SignQuery<Text, Player> query = SignQuery.<Text, Player>builder()
                    .position(new Vector3d(0, 1, 0))
                    .text(Lists.newArrayList(
                            Text.of(""),
                            Text.of("----------------"),
                            Text.of("Enter your time"),
                            Text.of("Ex: 1d5h")
                    ))
                    .response(submission -> {
                        try {
                            try {
                                result.set(new Time(submission.get(0)));
                                Impactor.getInstance().getScheduler().executeSync(() -> this.callback.accept(this.parent, result.get()));
                                return true;
                            } catch (Exception e) {
                                return false;
                            }
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .reopenOnFailure(false)
                    .build();
            this.viewer.closeInventory();
            query.sendTo(this.viewer);
        });
        builder.slot(icon, 16);

        return builder.build();
    }

    private SpongeIcon create(TimeKey key, DyeColor color) {
        MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        Time time = GTSPlugin.getInstance().getConfiguration().get(key);

        ItemStack item = ItemStack.builder()
                .itemType(ItemTypes.CONCRETE)
                .add(Keys.DISPLAY_NAME, service.parse("&a" + this.read(time)))
                .add(Keys.DYE_COLOR, color)
                .build();
        SpongeIcon icon = new SpongeIcon(item);
        icon.addListener(clickable -> {
            this.callback.accept(this.parent, time);
        });

        return icon;
    }

    private String read(Time time) {
        long seconds = TimeUnit.SECONDS.toSeconds(time.getTime()) % 60;
        long minutes = TimeUnit.SECONDS.toMinutes(time.getTime()) % 60;
        long hours = TimeUnit.SECONDS.toHours(time.getTime()) % 24;
        long days = TimeUnit.SECONDS.toDays(time.getTime()) % 7;
        long weeks = TimeUnit.SECONDS.toDays(time.getTime()) / 7;

        StringJoiner joiner = new StringJoiner(" ");
        if(weeks > 0) {
            joiner.add(select(weeks, MsgConfigKeys.WEEKS));
        }

        if(days > 0) {
            joiner.add(select(days, MsgConfigKeys.DAYS));
        }

        if(hours > 0) {
            joiner.add(select(hours, MsgConfigKeys.HOURS));
        }

        if(minutes > 0) {
            joiner.add(select(minutes, MsgConfigKeys.MINUTES));
        }

        if(seconds > 0) {
            joiner.add(select(seconds, MsgConfigKeys.SECONDS));
        }

        return joiner.toString();
    }

    private static String select(long input, ConfigKey<TimeLanguageOptions> key) {
        if(input == 1) {
            return input + " " + GTSPlugin.getInstance().getMsgConfig().get(key).getSingular();
        } else {
            return input + " " + GTSPlugin.getInstance().getMsgConfig().get(key).getPlural();
        }
    }

}
