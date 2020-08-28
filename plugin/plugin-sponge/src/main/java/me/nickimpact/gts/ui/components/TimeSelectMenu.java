package me.nickimpact.gts.ui.components;

import com.nickimpact.impactor.api.Impactor;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.services.text.MessageService;
import com.nickimpact.impactor.api.utilities.Time;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import me.nickimpact.gts.common.config.updated.ConfigKeys;
import me.nickimpact.gts.common.config.MsgConfigKeys;
import me.nickimpact.gts.common.config.updated.types.time.TimeKey;
import me.nickimpact.gts.common.config.updated.types.time.TimeLanguageOptions;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.util.Utilities;
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

@SuppressWarnings("unchecked")
public class TimeSelectMenu {

    private static final SpongeIcon lowest = create(ConfigKeys.LISTING_TIME_LOWEST, DyeColors.RED);
    private static final SpongeIcon low = create(ConfigKeys.LISTING_TIME_LOW, DyeColors.ORANGE);
    private static final SpongeIcon mid = create(ConfigKeys.LISTING_TIME_MID, DyeColors.YELLOW);
    private static final SpongeIcon high = create(ConfigKeys.LISTING_TIME_HIGH, DyeColors.LIME);
    private static final SpongeIcon highest = create(ConfigKeys.LISTING_TIME_HIGHEST, DyeColors.LIGHT_BLUE);

    private static final SpongeLayout LAYOUT = layout();

    private final Player viewer;
    private final SpongeUI display;

    public TimeSelectMenu(Player viewer) {
        MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        this.viewer = viewer;
        this.display = SpongeUI.builder()
                .title(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_TIME_SELECT_TITLE)))
                .dimension(InventoryDimension.of(9, 3))
                .build()
                .define(LAYOUT);
    }

    public void open() {
        this.display.open(this.viewer);
    }

    private static SpongeLayout layout() {
        SpongeLayout.SpongeLayoutBuilder builder = SpongeLayout.builder();
        builder.dimension(9, 3).border().slot(SpongeIcon.BORDER, 15);
        builder.slot(lowest, 10);
        builder.slot(low, 11);
        builder.slot(mid, 12);
        builder.slot(high, 13);
        builder.slot(highest, 14);

        return builder.build();
    }

    private static SpongeIcon create(TimeKey key, DyeColor color) {
        MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        Time time = GTSPlugin.getInstance().getConfiguration().get(key);

        ItemStack item = ItemStack.builder()
                .itemType(ItemTypes.CONCRETE)
                .add(Keys.DISPLAY_NAME, service.parse("&a" + read(time)))
                .add(Keys.DYE_COLOR, color)
                .build();
        SpongeIcon icon = new SpongeIcon(item);
        icon.addListener(clickable -> {
            // TODO - Send response back to Listing Creator
        });

        return icon;
    }

    private static String read(Time time) {
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
