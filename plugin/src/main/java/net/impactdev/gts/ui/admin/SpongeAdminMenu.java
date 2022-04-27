package net.impactdev.gts.ui.admin;

import net.impactdev.gts.sponge.utils.items.ProvidedIcons;
import net.impactdev.gts.ui.submenu.SpongeListingMenu;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.ui.containers.ImpactorUI;
import net.impactdev.impactor.api.ui.containers.icons.DisplayProvider;
import net.impactdev.impactor.api.ui.containers.icons.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.Layout;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

public class SpongeAdminMenu {

    private final static Layout LAYOUT = generate();

    private final PlatformPlayer viewer;
    private final ImpactorUI view;

    public SpongeAdminMenu(ServerPlayer viewer) {
        this.viewer = PlatformPlayer.from(viewer);
        this.view = ImpactorUI.builder()
                .provider(Key.key("gts", "admin"))
                .title(Utilities.parse(MsgConfigKeys.UI_ADMIN_MAIN_TITLE, PlaceholderSources.empty()))
                .layout(LAYOUT)
                .build();
    }

    public void open() {
        this.view.open(this.viewer);
    }

    private static Layout generate() {
        MessageService service = Utilities.PARSER;

        Layout.LayoutBuilder builder = Layout.builder()
                .size(6)
                .border(ProvidedIcons.BORDER)
                .slots(ProvidedIcons.BORDER, 19, 20, 24, 25, 28, 29, 30, 31, 32, 33, 34);

        final Icon<ItemStack> darkBlue = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                    .itemType(ItemTypes.BLUE_STAINED_GLASS_PANE)
                    .add(Keys.CUSTOM_NAME, Component.empty())
                    .build()
                ))
                .build();

        final Icon<ItemStack> lightBlue = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                        .itemType(ItemTypes.LIGHT_BLUE_STAINED_GLASS_PANE)
                        .add(Keys.CUSTOM_NAME, Component.empty())
                        .build()
                ))
                .build();

        Icon<ItemStack> info = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                        .itemType(ItemTypes.COMMAND_BLOCK)
                        .add(Keys.CUSTOM_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ADMIN_MAIN_INFO_TITLE)))
                        .add(Keys.LORE, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ADMIN_MAIN_INFO_LORE)))
                        .build()
                ))
                .build();

        builder.slot(info, 13);

        Icon<ItemStack> manager = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                        .itemType(ItemTypes.ANVIL)
                        .add(Keys.CUSTOM_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ADMIN_MAIN_MANAGER)))
                        .build()
                ))
                .listener(context -> {
                    new SpongeListingMenu(context.require(ServerPlayer.class), true).open();
                    return false;
                })
                .build();

        Icon<ItemStack> deliver = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                        .itemType(ItemTypes.CHEST_MINECART)
                        .add(Keys.CUSTOM_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ADMIN_MAIN_DELIVERIES)))
                        .build()
                ))
                .listener(context -> {
                    new SpongeAdminDeliveryMenu(context.require(ServerPlayer.class)).open();
                    context.require(ServerPlayer.class).sendMessage(service.parse("{{gts:error}} Delivery creation coming soon"));
                    return false;
                })
                .build();

        Icon<ItemStack> disabler = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                        .itemType(ItemTypes.REPEATING_COMMAND_BLOCK)
                        .add(Keys.CUSTOM_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_ADMIN_MAIN_DISABLER)))
                        .build()
                ))
                .listener(context -> {
                    // TODO - Disable running tasks, commands, etc
                    return false;
                })
                .build();

        builder.slots(darkBlue, 10, 11, 15, 16);
        builder.slots(lightBlue, 3, 4, 5, 12, 14, 21, 22, 23);

        builder.slot(manager, 38);
        builder.slot(deliver, 40);
        builder.slot(disabler, 42);

        return builder.build();
    }

}
