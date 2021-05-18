package net.impactdev.gts.ui.admin.editor;

import com.google.common.collect.Lists;
import net.impactdev.gts.api.listings.Listing;
import net.impactdev.gts.api.messaging.message.errors.ErrorCodes;
import net.impactdev.gts.api.messaging.message.type.admin.ForceDeleteMessage;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.messaging.messages.admin.ForceDeleteMessageImpl;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.ui.SpongeAsyncPage;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongeUI;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;

import java.util.function.Supplier;

/**
 * Allows for editing a specific listing. Such actions allow for forced return to a user's stash, deleting a listing,
 * or taking the listing's entry and forcing delete following. This menu effectively resembles the selected listing menu but with different
 * possible actions.
 */
public class SpongeListingEditorMenu {

    private SpongeUI display;
    private Player viewer;

    private Listing focus;
    private Supplier<SpongeAsyncPage<?>> parent;

    public SpongeListingEditorMenu(Player viewer, Listing focus, Supplier<SpongeAsyncPage<?>> parent) {
        this.viewer = viewer;
        this.focus = focus;
        this.parent = parent;

        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        this.display = SpongeUI.builder()
                .title(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_TITLE)))
                .dimension(InventoryDimension.of(9, 6))
                .build()
                .define(this.layout());
    }

    public void open() {
        this.display.open(this.viewer);
    }

    private SpongeLayout layout() {
        SpongeLayout.SpongeLayoutBuilder builder = SpongeLayout.builder();
        SpongeIcon colored = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DYE_COLOR, DyeColors.LIGHT_BLUE)
                .add(Keys.DISPLAY_NAME, Text.EMPTY)
                .build()
        );

        builder.border();
        builder.slots(colored, 3, 4, 5, 10, 11, 12, 14, 15, 16, 21, 22, 23);
        builder.slots(SpongeIcon.BORDER, 19, 20, 24, 25);
        builder.row(SpongeIcon.BORDER, 3);

        builder.slot(this.deleteAndReturn(), 36);
        builder.slot(this.delete(), 38);
        builder.slot(this.copy(), 40);
        builder.slot(this.edit(), 42);
        builder.slot(this.end(), 44);

        return builder.build();
    }

    private SpongeIcon delete() {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        ItemStack display = ItemStack.builder()
                .itemType(ItemTypes.CHEST_MINECART)
                .add(Keys.DISPLAY_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_RETURN_TITLE)))
                .add(Keys.ITEM_LORE, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_RETURN_LORE)))
                .build();
        SpongeIcon icon = new SpongeIcon(display);
        icon.addListener(clickable -> {
            GTSPlugin.getInstance().getMessagingService()
                    .requestForcedDeletion(this.focus.getID(), this.viewer.getUniqueId(), false)
                    .thenAccept(response -> {
                        if(response.wasSuccessful()) {
                            this.viewer.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_SUCCESS)));
                        } else {
                            this.viewer.sendMessage(service.parse(
                                    Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_FAILURE),
                                    Lists.newArrayList(() -> response.getErrorCode().orElse(ErrorCodes.UNKNOWN))
                            ));
                        }
                    });
            Sponge.getScheduler().createTaskBuilder().execute(() -> this.display.close(this.viewer)).submit(GTSPlugin.getInstance().getBootstrap());
        });
        return icon;
    }

    private SpongeIcon deleteAndReturn() {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);

        ItemStack display = ItemStack.builder()
                .itemType(ItemTypes.HOPPER_MINECART)
                .add(Keys.DISPLAY_NAME, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_NORETURN_TITLE)))
                .add(Keys.ITEM_LORE, service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_NORETURN_LORE)))
                .build();
        SpongeIcon icon = new SpongeIcon(display);
        icon.addListener(clickable -> {
            GTSPlugin.getInstance().getMessagingService()
                    .requestForcedDeletion(this.focus.getID(), this.viewer.getUniqueId(), true)
                    .thenAccept(response -> {
                        if(response.wasSuccessful()) {
                            this.viewer.sendMessage(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_SUCCESS)));
                        } else {
                            this.viewer.sendMessage(service.parse(
                                    Utilities.readMessageConfigOption(MsgConfigKeys.ADMIN_LISTING_EDITOR_DELETE_ACTOR_RESPONSE_FAILURE),
                                    Lists.newArrayList(() -> response.getErrorCode().orElse(ErrorCodes.UNKNOWN))
                            ));
                        }
                    });
            Sponge.getScheduler().createTaskBuilder().execute(() -> this.display.close(this.viewer)).submit(GTSPlugin.getInstance().getBootstrap());
        });
        return icon;
    }

    private SpongeIcon copy() {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        return SpongeIcon.BORDER;
    }

    private SpongeIcon edit() {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        return SpongeIcon.BORDER;
    }

    private SpongeIcon end() {
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        return SpongeIcon.BORDER;
    }
}
