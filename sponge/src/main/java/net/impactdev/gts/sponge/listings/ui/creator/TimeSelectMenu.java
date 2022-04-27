package net.impactdev.gts.sponge.listings.ui.creator;

import net.impactdev.gts.sponge.listings.ui.AbstractSpongeEntryUI;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.ui.containers.ImpactorUI;
import net.impactdev.impactor.api.ui.containers.icons.DisplayProvider;
import net.impactdev.impactor.api.ui.containers.icons.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.Layout;
import net.impactdev.impactor.api.utilities.Time;
import net.impactdev.gts.common.config.ConfigKeys;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.config.types.time.TimeKey;
import net.impactdev.gts.common.config.types.time.TimeLanguageOptions;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.utils.Utilities;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class TimeSelectMenu {

    private final Icon<ItemStack> lowest = this.create(ConfigKeys.LISTING_TIME_LOWEST, ItemTypes.RED_CONCRETE.get());
    private final Icon<ItemStack> low = this.create(ConfigKeys.LISTING_TIME_LOW, ItemTypes.ORANGE_CONCRETE.get());
    private final Icon<ItemStack> mid = this.create(ConfigKeys.LISTING_TIME_MID, ItemTypes.YELLOW_CONCRETE.get());
    private final Icon<ItemStack> high = this.create(ConfigKeys.LISTING_TIME_HIGH, ItemTypes.LIME_CONCRETE.get());
    private final Icon<ItemStack> highest = this.create(ConfigKeys.LISTING_TIME_HIGHEST, ItemTypes.LIGHT_BLUE_CONCRETE.get());

    private final Player viewer;
    private final ImpactorUI display;

    private final AbstractSpongeEntryUI<?> parent;
    private final BiConsumer<AbstractSpongeEntryUI<?>, Time> callback;

    public TimeSelectMenu(Player viewer, AbstractSpongeEntryUI<?> parent, BiConsumer<AbstractSpongeEntryUI<?>, Time> callback) {
        MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
        this.viewer = viewer;
        this.parent = parent;
        this.callback = callback;
        this.display = ImpactorUI.builder()
                .title(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_TIME_SELECT_TITLE)))
                .layout(this.layout())
                .build();
    }

    public void open() {
        this.display.open(PlatformPlayer.from(this.viewer));
    }

    private Layout layout() {
        Layout.LayoutBuilder builder = Layout.builder();
        Icon<ItemStack> border = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                        .itemType(ItemTypes.BLACK_STAINED_GLASS_PANE)
                        .add(Keys.CUSTOM_NAME, Component.empty())
                        .build()
                ))
                .build();

        builder.size(3).border(border).slot(border, 15);
        builder.slot(this.lowest, 10);
        builder.slot(this.low, 11);
        builder.slot(this.mid, 12);
        builder.slot(this.high, 13);
        builder.slot(this.highest, 14);

        MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
        ItemStack custom = ItemStack.builder()
                .itemType(ItemTypes.CLOCK)
                .add(Keys.CUSTOM_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.CUSTOM_TIME_TITLE)))
                .add(Keys.LORE, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.CUSTOM_TIME_LORE)))
                .build();

        Icon<ItemStack> icon = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(custom))
                .listener(context -> {
//                    SignQuery<Text, Player> query = SignQuery.<Text, Player>builder()
//                            .position(new Vector3d(0, 1, 0))
//                            .text(Lists.newArrayList(
//                                    Text.of(""),
//                                    Text.of("----------------"),
//                                    Text.of("Enter your time"),
//                                    Text.of("Ex: 1d5h")
//                            ))
//                            .response(submission -> {
//                                try {
//                                    Time time = new Time(submission.get(0));
//
//                                    long min = GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.LISTING_MIN_TIME).getTime();
//                                    long max = GTSPlugin.getInstance().getConfiguration().get(ConfigKeys.LISTING_MAX_TIME).getTime();
//                                    if(time.getTime() >= Math.max(0, min) && time.getTime() <= Math.max(0, max)) {
//                                        Impactor.getInstance().getScheduler().executeSync(() -> this.callback.accept(this.parent, time));
//                                        return true;
//                                    }
//
//                                    Impactor.getInstance().getScheduler().executeSync(() -> this.callback.accept(this.parent, null));
//                                    return false;
//                                } catch (Exception e) {
//                                    return false;
//                                }
//                            })
//                            .reopenOnFailure(false)
//                            .build();
//                    this.viewer.closeInventory();
//                    query.sendTo(this.viewer);

                    return false;
                })
                .build();

        builder.slot(icon, 16);
        return builder.build();
    }

    private Icon<ItemStack> create(TimeKey key, ItemType type) {
        MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
        Time time = GTSPlugin.instance().configuration().main().get(key);

        ItemStack item = ItemStack.builder()
                .itemType(type)
                .add(Keys.DISPLAY_NAME, service.parse("&a" + this.read(time)))
                .build();

        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(item))
                .listener(context -> {
                    this.callback.accept(this.parent, time);
                    return false;
                })
                .build();
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
            return input + " " + GTSPlugin.instance().configuration().language().get(key).getSingular();
        } else {
            return input + " " + GTSPlugin.instance().configuration().language().get(key).getPlural();
        }
    }

}
