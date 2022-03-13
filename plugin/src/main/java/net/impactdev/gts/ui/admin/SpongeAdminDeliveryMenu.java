package net.impactdev.gts.ui.admin;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import net.impactdev.gts.GTSSpongePlugin;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.gts.sponge.utils.items.SkullCreator;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.gui.Clickable;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongeUI;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SpongeAdminDeliveryMenu {

    private static ChatProcessor processor = new ChatProcessor();

    private final Player viewer;
    private final SpongeUI view;

    private final GameProfile target;
    private final List<SpongeEntry<?>> entries = Lists.newArrayList();

    public SpongeAdminDeliveryMenu(Player viewer) {
        this(viewer, null);
    }

    public SpongeAdminDeliveryMenu(Player viewer, GameProfile target) {
        this.viewer = viewer;
        this.target = target;

        this.view = SpongeUI.builder()
                .title(Text.of(TextColors.RED, "GTS ", TextColors.GRAY, "\u00bb"))
                .dimension(InventoryDimension.of(9, 6))
                .build()
                .define(this.design());
    }

    public void open() {
        this.view.open(this.viewer);
    }

    private SpongeLayout design() {
        SpongeLayout.SpongeLayoutBuilder builder = SpongeLayout.builder();
        builder.border().rows(SpongeIcon.BORDER, 2, 3);

        builder.slot(this.target(), 13);
        builder.slots(this.surround(), 3, 4, 5, 10, 11, 12, 14, 15, 16, 21, 22, 23);

        return builder.build();
    }

    private SpongeIcon surround() {
        return new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DISPLAY_NAME, Text.EMPTY)
                .add(Keys.DYE_COLOR, this.target == null ? DyeColors.RED : DyeColors.LIME)
                .build()
        );
    }

    private SpongeIcon target() {
        SpongeIcon result;
        if(this.target == null) {
            result = new SpongeIcon(ItemStack.builder()
                    .itemType(ItemTypes.STONE_BUTTON)
                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Awaiting Player Selection"))
                    .add(Keys.ITEM_LORE, Lists.newArrayList(
                            Text.of(TextColors.GRAY, "Click to select a player that will"),
                            Text.of(TextColors.GRAY, "be the target of the options below."),
                            Text.EMPTY,
                            Text.of(TextColors.GRAY, "Current Delivery Contents:"),
                            Text.of(TextColors.RED, "* Empty")
                    ))
                    .build()
            );
        } else {
            List<Text> lore = Lists.newArrayList(
                    Text.of(TextColors.GRAY, "Click to select a player that will"),
                    Text.of(TextColors.GRAY, "be the target of the options below."),
                    Text.EMPTY,
                    Text.of(TextColors.GRAY, "Current Target: ", TextColors.GREEN, this.target.getName().get()),
                    Text.EMPTY,
                    Text.of(TextColors.GRAY, "Current Delivery Contents:")
            );
            for(SpongeEntry<?> entry : this.entries) {
                Text translated = TextSerializers.JSON.deserialize(GsonComponentSerializer.gson().serialize(entry.getName()));
                lore.add(Text.of(TextColors.GRAY, "* ", translated));
            }

            result = new SpongeIcon(ItemStack.builder()
                    .from(SkullCreator.fromProfile(this.target))
                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Target Selected"))
                    .add(Keys.ITEM_LORE, lore)
                    .build()
            );
        }

        result.addListener(clickable -> {
            this.view.close(clickable.getPlayer());
            processor.add(clickable.getPlayer().getUniqueId());

            MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
            clickable.getPlayer().sendMessage(service.parse(GTSPlugin.getInstance().getMsgConfig().get(MsgConfigKeys.ADMIN_USERNAME_QUERY)));
        });

        return result;
    }

    private Consumer<Clickable<Player, ClickInventoryEvent>> process(Consumer<Clickable<Player, ClickInventoryEvent>> action) {
        return clickable -> {
            if(this.target != null) {
                action.accept(clickable);
            }
        };
    }

    public static class ChatProcessor {

        private final Cache<UUID, Boolean> processing = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .removalListener((key, ignore, cause) -> {
                    if(key instanceof UUID) {
                        Sponge.getServer().getPlayer((UUID) key).ifPresent(player -> {
                            MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
                            player.sendMessage(service.parse(GTSPlugin.getInstance().getMsgConfig().get(MsgConfigKeys.ADMIN_TIMEOUT)));
                        });
                    }
                })
                .build();

        ChatProcessor() {
            Sponge.getEventManager().registerListeners(
                    GTSPlugin.getInstance()
                            .as(GTSSpongePlugin.class)
                            .getPluginContainer(),
                    this
            );
        }

        public void add(UUID uuid) {
            this.processing.put(uuid, false);
        }

        @Listener
        public void handleMessage(MessageChannelEvent event) {
            this.cause(event).ifPresent(source -> {
                if(this.processing.asMap().containsKey(source.getUniqueId())) {
                    event.setMessageCancelled(true);
                    this.processing.invalidate(source.getUniqueId());

                    Text result = event.getFormatter().getBody().format();
                    String content = result.toPlain().replaceAll("[<>]", "");

                    Sponge.getServiceManager().provideUnchecked(UserStorageService.class)
                            .get(content)
                            .ifPresent(user -> {
                                SpongeAdminDeliveryMenu menu = new SpongeAdminDeliveryMenu(source, user.getProfile());
                                menu.open();
                            });
                }
            });
        }

        private Optional<Player> cause(MessageChannelEvent event) {
            return event.getContext().get(EventContextKeys.PLAYER_SIMULATED)
                    .map(x -> Sponge.getServer().getPlayer(x.getUniqueId()))
                    .orElseGet(() -> Optional.of(event.getCause().root())
                            .filter(x -> x instanceof Player)
                            .map(x -> (Player) x)
                    );
        }
    }
}
