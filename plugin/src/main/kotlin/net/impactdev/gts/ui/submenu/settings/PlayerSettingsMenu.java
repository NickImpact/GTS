package net.impactdev.gts.ui.submenu.settings;

import net.impactdev.gts.api.GTSService;
import net.impactdev.gts.api.player.NotificationSetting;
import net.impactdev.gts.api.player.PlayerSettings;
import net.impactdev.gts.api.util.TriState;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.ui.SpongeMainMenu;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongeUI;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.impactdev.gts.sponge.utils.Utilities.readMessageConfigOption;

public class PlayerSettingsMenu {

    private final Player viewer;
    private final SpongeUI display;

    private PlayerSettings original;
    private PlayerSettings working;

    /** Ensures loading of settings was valid, set via {@link #queue()} */
    private final AtomicBoolean valid = new AtomicBoolean(false);

    public PlayerSettingsMenu(Player viewer) {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        this.viewer = viewer;
        this.display = SpongeUI.builder()
                .title(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_PLAYER_SETTINGS_TITLE)))
                .dimension(InventoryDimension.of(9, 5))
                .build();
        this.display.define(this.layout());
        this.set(NotificationSetting.Publish, 19, TriState.UNDEFINED);
        this.set(NotificationSetting.Sold, 21, TriState.UNDEFINED);
        this.set(NotificationSetting.Bid, 23, TriState.UNDEFINED);
        this.set(NotificationSetting.Outbid, 25, TriState.UNDEFINED);
        this.queue();

        AtomicBoolean ran = new AtomicBoolean(false);
        this.display.attachCloseListener(close -> {
            if(!ran.get()) {
                ran.set(true);
                if (!this.original.matches(this.working)) {
                    GTSService.getInstance().getPlayerSettingsManager().cache(this.viewer.getUniqueId(), this.working);
                }
            }
        });
    }

    public void open() {
        this.display.open(this.viewer);
    }

    private SpongeLayout layout() {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        SpongeLayout.SpongeLayoutBuilder layout = SpongeLayout.builder()
                .dimension(9, 4)
                .border()
                .dimension(9, 5)
                .slots(SpongeIcon.BORDER, 36, 44);

        layout.slot(this.initialize(NotificationSetting.Publish), 10);
        layout.slot(this.initialize(NotificationSetting.Sold), 12);
        layout.slot(this.initialize(NotificationSetting.Bid), 14);
        layout.slot(this.initialize(NotificationSetting.Outbid), 16);

        SpongeIcon back = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.BARRIER)
                .add(Keys.DISPLAY_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK)))
                .build()
        );
        back.addListener(clickable -> {
            this.display.close(this.viewer);
            SpongeMainMenu menu = new SpongeMainMenu(this.viewer);
            menu.open();
        });
        layout.slot(back, 40);

        return layout.build();
    }

    /**
     * Load user settings asynchronously and apply to the UI when ready
     */
    private void queue() {
        GTSService.getInstance().getPlayerSettingsManager()
                .retrieve(this.viewer.getUniqueId())
                .applyToEither(this.timeoutAfter(5, TimeUnit.SECONDS), settings -> settings)
                .thenAccept(settings -> {
                    this.original = settings;
                    this.working = this.original;
                    this.valid.set(true);

                    Impactor.getInstance().getScheduler().executeSync(() -> {
                        this.set(NotificationSetting.Publish, 19, TriState.fromBoolean(settings.getPublishListenState()));
                        this.set(NotificationSetting.Sold, 21, TriState.fromBoolean(settings.getSoldListenState()));
                        this.set(NotificationSetting.Bid, 23, TriState.fromBoolean(settings.getBidListenState()));
                        this.set(NotificationSetting.Outbid, 25, TriState.fromBoolean(settings.getOutbidListenState()));
                    });
                });
    }

    private SpongeIcon initialize(NotificationSetting setting) {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        Settings settings = Settings.locate(setting);

        String name = Utilities.readMessageConfigOption(MsgConfigKeys.UI_PLAYER_SETTINGS_SETTING_TITLE);
        name = name.replace("{{setting}}", setting.name());

        ItemStack display = ItemStack.builder()
                .itemType(settings.getItem())
                .add(Keys.DISPLAY_NAME, service.parse(name))
                .add(Keys.ITEM_LORE, service.parse(Utilities.readMessageConfigOption(settings.getDescription())))
                .build();
        return new SpongeIcon(display);
    }

    private void set(NotificationSetting setting, int slot, TriState state) {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        DyeColor color = state == TriState.TRUE ? DyeColors.LIME : state == TriState.FALSE ? DyeColors.GRAY : DyeColors.ORANGE;
        Text title = service.parse(Utilities.readMessageConfigOption(state == TriState.TRUE ? MsgConfigKeys.UI_PLAYER_SETTINGS_SETTING_ENABLED :
                state == TriState.FALSE ? MsgConfigKeys.UI_PLAYER_SETTINGS_SETTING_DISABLED : MsgConfigKeys.UI_PLAYER_SETTINGS_SETTING_LOADING));
        List<Text> lore = state == TriState.UNDEFINED ? Collections.emptyList() :
                service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_PLAYER_SETTINGS_SETTING_TOGGLE_LORE));

        ItemStack display = ItemStack.builder()
                .itemType(ItemTypes.DYE)
                .add(Keys.DYE_COLOR, color)
                .add(Keys.DISPLAY_NAME, title)
                .add(Keys.ITEM_LORE, lore)
                .build();
        SpongeIcon icon = new SpongeIcon(display);
        icon.addListener(clickable -> {
            this.working = this.working.set(setting, state.invert().asBoolean());
            this.set(setting, slot, state.invert());
        });

        this.display.setSlot(slot, icon);
    }

    private <W> CompletableFuture<W> timeoutAfter(long timeout, TimeUnit unit) {
        CompletableFuture<W> result = new CompletableFuture<>();
        Impactor.getInstance().getScheduler().asyncLater(() -> result.completeExceptionally(new TimeoutException()), timeout, unit);
        return result;
    }

    private enum Settings {
        P(NotificationSetting.Publish, ItemTypes.GOLD_INGOT, MsgConfigKeys.UI_PLAYER_SETTINGS_PUBLISH_SETTING_LORE),
        S(NotificationSetting.Sold, ItemTypes.EMERALD, MsgConfigKeys.UI_PLAYER_SETTINGS_SOLD_SETTING_LORE),
        B(NotificationSetting.Bid, ItemTypes.WRITABLE_BOOK, MsgConfigKeys.UI_PLAYER_SETTINGS_BID_SETTING_LORE),
        O(NotificationSetting.Outbid, ItemTypes.BOOK, MsgConfigKeys.UI_PLAYER_SETTINGS_OUTBID_SETTING_LORE),;

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
