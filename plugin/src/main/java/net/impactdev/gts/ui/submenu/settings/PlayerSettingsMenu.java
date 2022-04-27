package net.impactdev.gts.ui.submenu.settings;

import net.impactdev.gts.SpongeGTSPlugin;
import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.player.NotificationSetting;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.gts.api.util.TriState;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.utils.SpongeMenuOpener;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.sponge.utils.items.ProvidedIcons;
import net.impactdev.gts.ui.SpongeMainMenu;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.ui.containers.ImpactorUI;
import net.impactdev.impactor.api.ui.containers.icons.DisplayProvider;
import net.impactdev.impactor.api.ui.containers.icons.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.Layout;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;


public class PlayerSettingsMenu implements SpongeMenuOpener {

    private final ImpactorUI display;

    private PlayerSettings original;
    private PlayerSettings working;

    /** Ensures loading of settings was valid, set via {@link #queue(ServerPlayer)} */
    private final AtomicBoolean valid = new AtomicBoolean(false);

    public PlayerSettingsMenu(ServerPlayer viewer) {
        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
        AtomicBoolean ran = new AtomicBoolean(false);

        this.display = ImpactorUI.builder()
                .title(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_PLAYER_SETTINGS_TITLE)))
                .layout(this.layout(viewer))
                .onClose(context -> {
                    if(!ran.get()) {
                        ran.set(true);
                        if (!this.original.matches(this.working)) {
                            GTSService.getInstance().getPlayerSettingsManager().cache(viewer.uniqueId(), this.working);
                        }
                    }
                    return true;
                })
                .build();

        this.set(NotificationSetting.Publish, 19, TriState.UNDEFINED);
        this.set(NotificationSetting.Sold, 21, TriState.UNDEFINED);
        this.set(NotificationSetting.Bid, 23, TriState.UNDEFINED);
        this.set(NotificationSetting.Outbid, 25, TriState.UNDEFINED);
        this.queue(viewer);
    }

    public void open(ServerPlayer viewer) {
        this.open(() -> this.display.open(PlatformPlayer.from(viewer)));
    }

    private Layout layout(ServerPlayer viewer) {
        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);

        Layout.LayoutBuilder layout = Layout.builder()
                .size(4)
                .border(ProvidedIcons.BORDER)
                .size(5)
                .slots(ProvidedIcons.BORDER, 36, 44);

        layout.slot(this.initialize(NotificationSetting.Publish), 10);
        layout.slot(this.initialize(NotificationSetting.Sold), 12);
        layout.slot(this.initialize(NotificationSetting.Bid), 14);
        layout.slot(this.initialize(NotificationSetting.Outbid), 16);

        ItemStack b = ItemStack.builder()
                .itemType(ItemTypes.BARRIER)
                .add(Keys.CUSTOM_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK)))
                .build();

        Icon<ItemStack> back = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(b))
                .listener(context -> {
                    SpongeMainMenu menu = new SpongeMainMenu(viewer);
                    menu.open();
                    return false;
                })
                .build();
        layout.slot(back, 40);
        return layout.build();
    }

    /**
     * Load user settings asynchronously and apply to the UI when ready
     */
    private void queue(ServerPlayer viewer) {
        GTSService.getInstance().getPlayerSettingsManager()
                .retrieve(viewer.uniqueId())
                .applyToEither(this.timeoutAfter(5, TimeUnit.SECONDS), settings -> settings)
                .thenAccept(settings -> {
                    this.original = settings;
                    this.working = this.original;
                    this.valid.set(true);

                    Sponge.server().scheduler().submit(Task.builder()
                            .execute(() -> {
                                this.set(NotificationSetting.Publish, 19, TriState.fromBoolean(settings.getPublishListenState()));
                                this.set(NotificationSetting.Sold, 21, TriState.fromBoolean(settings.getSoldListenState()));
                                this.set(NotificationSetting.Bid, 23, TriState.fromBoolean(settings.getBidListenState()));
                                this.set(NotificationSetting.Outbid, 25, TriState.fromBoolean(settings.getOutbidListenState()));
                            })
                            .plugin(GTSPlugin.instance().as(SpongeGTSPlugin.class).container())
                            .build());
                });
    }

    private Icon<ItemStack> initialize(NotificationSetting setting) {
        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
        Settings settings = Settings.locate(setting);

        String name = Utilities.readMessageConfigOption(MsgConfigKeys.UI_PLAYER_SETTINGS_SETTING_TITLE);
        name = name.replace("{{setting}}", setting.name());

        ItemStack display = ItemStack.builder()
                .itemType(settings.getItem())
                .add(Keys.CUSTOM_NAME, service.parse(name))
                .add(Keys.LORE, service.parse(Utilities.readMessageConfigOption(settings.getDescription())))
                .build();
        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(display))
                .build();
    }

    private void set(NotificationSetting setting, int slot, TriState state) {
        final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);

        ItemType color = state == TriState.TRUE ? ItemTypes.LIME_DYE.get() : state == TriState.FALSE ? ItemTypes.GRAY_DYE.get() : ItemTypes.ORANGE_DYE.get();
        Component title = service.parse(Utilities.readMessageConfigOption(state == TriState.TRUE ? MsgConfigKeys.UI_PLAYER_SETTINGS_SETTING_ENABLED :
                state == TriState.FALSE ? MsgConfigKeys.UI_PLAYER_SETTINGS_SETTING_DISABLED : MsgConfigKeys.UI_PLAYER_SETTINGS_SETTING_LOADING));
        List<Component> lore = state == TriState.UNDEFINED ? Collections.emptyList() :
                service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_PLAYER_SETTINGS_SETTING_TOGGLE_LORE));

        ItemStack display = ItemStack.builder()
                .itemType(color)
                .add(Keys.CUSTOM_NAME, title)
                .add(Keys.LORE, lore)
                .build();
        Icon<ItemStack> icon = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(display))
                .listener(context -> {
                    this.working = this.working.set(setting, state.invert().asBoolean());
                    this.set(setting, slot, state.invert());

                    return false;
                })
                .build();

        this.display.set(icon, slot);
    }

    private <W> CompletableFuture<W> timeoutAfter(long timeout, TimeUnit unit) {
        CompletableFuture<W> result = new CompletableFuture<>();
        Impactor.getInstance().getScheduler().asyncLater(() -> result.completeExceptionally(new TimeoutException()), timeout, unit);
        return result;
    }

    private enum Settings {
        P(NotificationSetting.Publish, ItemTypes.GOLD_INGOT.get(), MsgConfigKeys.UI_PLAYER_SETTINGS_PUBLISH_SETTING_LORE),
        S(NotificationSetting.Sold, ItemTypes.EMERALD.get(), MsgConfigKeys.UI_PLAYER_SETTINGS_SOLD_SETTING_LORE),
        B(NotificationSetting.Bid, ItemTypes.WRITABLE_BOOK.get(), MsgConfigKeys.UI_PLAYER_SETTINGS_BID_SETTING_LORE),
        O(NotificationSetting.Outbid, ItemTypes.BOOK.get(), MsgConfigKeys.UI_PLAYER_SETTINGS_OUTBID_SETTING_LORE),;

        private final NotificationSetting setting;
        private final ItemType item;
        private final ConfigKey<List<String>> description;

        Settings(NotificationSetting setting, ItemType item, ConfigKey<List<String>> description) {
            this.setting = setting;
            this.item = item;
            this.description = description;
        }

        public NotificationSetting getSetting() {
            return this.setting;
        }

        public ItemType getItem() {
            return this.item;
        }

        public ConfigKey<List<String>> getDescription() {
            return this.description;
        }

        public static Settings locate(NotificationSetting setting) {
            return Arrays.stream(values()).filter(x -> x.getSetting() == setting)
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid setting supplied"));
        }
    }

}
