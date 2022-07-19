package net.impactdev.gts.ui.admin;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import net.impactdev.gts.SpongeGTSPlugin;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.listings.makeup.SpongeEntry;
import net.impactdev.gts.sponge.utils.items.ProvidedIcons;
import net.impactdev.gts.sponge.utils.items.SkullCreator;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.platform.players.PlatformPlayer;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.ui.containers.ImpactorUI;
import net.impactdev.impactor.api.ui.containers.icons.DisplayProvider;
import net.impactdev.impactor.api.ui.containers.icons.Icon;
import net.impactdev.impactor.api.ui.containers.layouts.Layout;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scheduler.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SpongeAdminDeliveryMenu {

    private static final ChatProcessor processor = new ChatProcessor();

    private final PlatformPlayer viewer;
    private final ImpactorUI view;

    private final GameProfile target;
    private final List<SpongeEntry<?>> entries = Lists.newArrayList();

    public SpongeAdminDeliveryMenu(ServerPlayer viewer) {
        this(viewer, null);
    }

    public SpongeAdminDeliveryMenu(ServerPlayer viewer, GameProfile target) {
        this.viewer = PlatformPlayer.from(viewer);
        this.target = target;

        this.view = ImpactorUI.builder()
                .provider(Key.key("gts", "admin-deliveries"))
                .title(Component.text("GTS").color(NamedTextColor.RED)
                        .append(Component.text(" \u00bb ").color(NamedTextColor.GRAY))
                        .append(Component.text("Delivery Management").color(NamedTextColor.DARK_AQUA))
                )
                .layout(this.design())
                .build();
    }

    public void open() {
        this.view.open(this.viewer);
    }

    private Layout design() {
        Layout.LayoutBuilder builder = Layout.builder();
        builder.border(ProvidedIcons.BORDER).rows(ProvidedIcons.BORDER, 2, 3);

        builder.slot(this.target(), 13);
        builder.slots(this.surround(), 3, 4, 5, 10, 11, 12, 14, 15, 16, 21, 22, 23);

        return builder.build();
    }

    private Icon<ItemStack> surround() {
        return Icon.builder(ItemStack.class)
                .display(new DisplayProvider.Constant<>(ItemStack.builder()
                    .itemType(this.target == null ? ItemTypes.RED_STAINED_GLASS_PANE : ItemTypes.LIME_STAINED_GLASS_PANE)
                    .add(Keys.CUSTOM_NAME, Component.empty())
                    .build()
                ))
                .build();
    }

    private Icon<ItemStack> target() {
        Icon.IconBuilder<ItemStack> builder = Icon.builder(ItemStack.class)
                .listener(context -> {
                    ServerPlayer player = context.require(ServerPlayer.class);
                    PlatformPlayer platform = PlatformPlayer.from(player);

                    this.view.close(platform);
                    processor.add(platform.uuid());

                    MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
                    player.sendMessage(service.parse(GTSPlugin.instance().configuration().language().get(MsgConfigKeys.ADMIN_USERNAME_QUERY)));

                    return false;
                });

        if(this.target == null) {
            builder.display(new DisplayProvider.Constant<>(ItemStack.builder()
                    .itemType(ItemTypes.STONE_BUTTON)
                    .add(Keys.CUSTOM_NAME, Component.text("Awaiting Player Selection").color(NamedTextColor.RED))
                    .add(Keys.LORE, Lists.newArrayList(
                            Component.text("Click to select a player that will").color(NamedTextColor.GRAY),
                            Component.text("be the target of the options below.").color(NamedTextColor.GRAY),
                            Component.empty(),
                            Component.text("Current Delivery Contents:").color(NamedTextColor.GRAY),
                            Component.text("* Empty").color(NamedTextColor.RED)
                    ))
                    .build()
            ));
        } else {
            List<Component> lore = Lists.newArrayList(
                    Component.text("Click to select a player that will").color(NamedTextColor.GRAY),
                    Component.text("be the target of the options below.").color(NamedTextColor.GRAY),
                    Component.empty(),
                    Component.text("Current Target: ").color(NamedTextColor.GRAY)
                                    .append(Component.text(this.target.name().get()).color(NamedTextColor.GREEN)),
                    Component.empty(),
                    Component.text("Current Delivery Contents:").color(NamedTextColor.GRAY)
            );
            for(SpongeEntry<?> entry : this.entries) {
                lore.add(Component.text("* ").color(NamedTextColor.GRAY).append(entry.getName()));
            }

            builder.display(new DisplayProvider.Constant<>(ItemStack.builder()
                    .from(SkullCreator.fromProfile(this.target))
                    .add(Keys.CUSTOM_NAME, Component.text("Target Selected").color(NamedTextColor.GREEN))
                    .add(Keys.LORE, lore)
                    .build()
            ));
        }

        return builder.build();
    }

//    private Consumer<Clickable<Player, ClickInventoryEvent>> process(Consumer<Clickable<Player, ClickInventoryEvent>> action) {
//        return clickable -> {
//            if(this.target != null) {
//                action.accept(clickable);
//            }
//        };
//    }

    public static class ChatProcessor {

        private final Cache<UUID, Boolean> processing = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .removalListener((key, ignore, cause) -> {
                    if(key instanceof UUID) {
                        Sponge.server().player((UUID) key).ifPresent(player -> {
                            MessageService service = Impactor.getInstance().getRegistry().get(MessageService.class);
                            player.sendMessage(service.parse(GTSPlugin.instance().configuration().language().get(MsgConfigKeys.ADMIN_TIMEOUT)));
                        });
                    }
                })
                .build();

        ChatProcessor() {
            Sponge.eventManager().registerListeners(
                    GTSPlugin.instance().as(SpongeGTSPlugin.class).container(),
                    this
            );
        }

        public void add(UUID uuid) {
            this.processing.put(uuid, false);
        }

        @Listener
        public void handleMessage(PlayerChatEvent event) {
            this.cause(event).ifPresent(source -> {
                if(this.processing.asMap().containsKey(source.uniqueId())) {
                    event.setCancelled(true);
                    this.processing.invalidate(source.uniqueId());

                    Component result = event.message();
                    String content = LegacyComponentSerializer.legacyAmpersand().serialize(result).replaceAll("[<>]", "");

                    source.sendMessage(Component.text("Locating user profile, one moment...").color(NamedTextColor.GRAY));
                    Sponge.server().userManager()
                            .load(content)
                            .thenAccept(user -> user.ifPresent(u -> {
                                Sponge.server().scheduler().submit(Task.builder()
                                                .execute(() -> {
                                                    SpongeAdminDeliveryMenu menu = new SpongeAdminDeliveryMenu(source, u.profile());
                                                    menu.open();
                                                })
                                                .plugin(GTSPlugin.instance().as(SpongeGTSPlugin.class).container())
                                        .build()
                                );
                            }));
                }
            });
        }

        private Optional<ServerPlayer> cause(PlayerChatEvent event) {
            return event.context().get(EventContextKeys.PLAYER)
                    .map(x -> Sponge.server().player(x.uniqueId()))
                    .orElseGet(() -> Optional.of(event.cause().root())
                            .filter(x -> x instanceof ServerPlayer)
                            .map(x -> (ServerPlayer) x)
                    );
        }
    }
}
