package net.impactdev.gts.ui.admin.editor;

import com.google.common.collect.Lists;
import net.impactdev.gts.SpongeGTSPlugin;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.messaging.message.errors.ErrorCode;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.sponge.utils.items.ProvidedIcons;
import net.impactdev.gts.ui.submenu.SpongeListingMenu;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.placeholders.PlaceholderSources;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.ui.containers.ImpactorUI;
import net.impactdev.impactor.api.ui.containers.icons.DisplayProvider;
import net.impactdev.impactor.api.ui.containers.icons.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.Layout;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;

import java.util.function.Supplier;

/**
 * Allows for editing a specific listing. Such actions allow for forced return to a user's stash, deleting a listing,
 * or taking the listing's entry and forcing delete following. This menu effectively resembles the selected listing menu but with different
 * possible actions.
 */
public class SpongeListingEditorMenu {

    private final ImpactorUI display;
    private final PlatformPlayer viewer;

    private final Listing focus;
    private final Supplier<SpongeListingMenu> parent;

    private final MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);

    public SpongeListingEditorMenu(ServerPlayer viewer, Listing focus, Supplier<SpongeListingMenu> parent) {
        this.viewer = PlatformPlayer.from(viewer);
        this.focus = focus;
        this.parent = parent;

        this.display = ImpactorUI.builder()
                .provider(Key.key("gts", "admin-listing-editor"))
                .title(this.service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_TITLE)))
                .layout(this.layout())
                .build();
    }

    public void open() {
        this.display.open(this.viewer);
    }

    private Layout layout() {
        Layout.LayoutBuilder builder = Layout.builder();
        Icon<ItemStack> colored = Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                        .itemType(ItemTypes.LIGHT_BLUE_STAINED_GLASS_PANE)
                        .add(Keys.CUSTOM_NAME, Component.empty())
                        .build()
                ))
                .build();

        builder.border(ProvidedIcons.BORDER);
        builder.slots(colored, 3, 4, 5, 10, 11, 12, 14, 15, 16, 21, 22, 23);
        builder.slots(ProvidedIcons.BORDER, 19, 20, 24, 25);
        builder.row(ProvidedIcons.BORDER, 3);

        builder.slot(this.delete(), 36);
        builder.slot(this.deleteAndReturn(), 38);
        builder.slot(this.copy(), 40);
        builder.slot(this.edit(), 42);
        builder.slot(this.end(), 44);

        return builder.build();
    }

    private ServerPlayer player() {
        return Sponge.server().player(this.viewer.uuid()).orElseThrow(IllegalStateException::new);
    }

    private Icon<ItemStack> delete() {
        ItemStack display = ItemStack.builder()
                .itemType(ItemTypes.HOPPER_MINECART)
                .add(Keys.CUSTOM_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_TITLE)))
                .add(Keys.LORE, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_LORE)))
                .build();

        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(display))
                .listener(context -> {
                    GTSPlugin.instance().messagingService()
                            .requestForcedDeletion(this.focus.getID(), this.viewer.uuid(), false)
                            .thenAccept(response -> {
                                if(response.wasSuccessful()) {
                                    this.player().sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_SUCCESS)));
                                } else {
                                    PlaceholderSources sources = PlaceholderSources.builder()
                                            .append(ErrorCode.class, () -> response.getErrorCode().orElse(ErrorCodes.UNKNOWN))
                                            .build();
                                    this.player().sendMessage(service.parse(
                                            Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_FAILURE),
                                            sources
                                    ));
                                }
                            });
                    Sponge.server().scheduler().submit(Task.builder()
                            .execute(() -> this.display.close(this.viewer))
                            .plugin(GTSPlugin.instance().as(SpongeGTSPlugin.class).container())
                            .build()
                    );

                    return false;
                })
                .build();
    }

    private Icon<ItemStack> deleteAndReturn() {
        ItemStack display = ItemStack.builder()
                .itemType(ItemTypes.CHEST_MINECART)
                .add(Keys.CUSTOM_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_RETURN_TITLE)))
                .add(Keys.LORE, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_RETURN_LORE)))
                .build();

        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(display))
                .listener(context -> {
                    GTSPlugin.instance().messagingService()
                            .requestForcedDeletion(this.focus.getID(), this.viewer.uuid(), true)
                            .thenAccept(response -> {
                                if(response.wasSuccessful()) {
                                    this.player().sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_SUCCESS)));
                                } else {
                                    PlaceholderSources sources = PlaceholderSources.builder()
                                            .append(ErrorCode.class, () -> response.getErrorCode().orElse(ErrorCodes.UNKNOWN))
                                            .build();
                                    this.player().sendMessage(service.parse(
                                            Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_FAILURE),
                                            sources
                                    ));
                                }
                            });

                    Sponge.server().scheduler().submit(Task.builder()
                            .execute(() -> this.display.close(this.viewer))
                            .plugin(GTSPlugin.instance().as(SpongeGTSPlugin.class).container())
                            .build()
                    );
                    return false;
                })
                .build();
    }

    private Icon<ItemStack> copy() {
        return ProvidedIcons.BORDER;
    }

    private Icon<ItemStack> edit() {
        return ProvidedIcons.BORDER;
    }

    private Icon<ItemStack> end() {
        return ProvidedIcons.BORDER;
    }
}
