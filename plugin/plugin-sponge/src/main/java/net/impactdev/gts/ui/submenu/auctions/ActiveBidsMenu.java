package net.impactdev.gts.ui.submenu.auctions;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.impactdev.gts.api.listings.auctions.Auction;
import net.impactdev.gts.api.listings.buyitnow.BuyItNow;
import net.impactdev.gts.api.listings.makeup.Display;
import net.impactdev.gts.api.listings.manager.ListingManager;
import net.impactdev.gts.common.config.MsgConfigKeys;
import net.impactdev.gts.common.config.wrappers.TitleLorePair;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.listings.SpongeListing;
import net.impactdev.gts.sponge.ui.SpongeAsyncPage;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.ui.submenu.SpongeListingMenu;
import net.impactdev.gts.ui.submenu.browser.SpongeSelectedListingMenu;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.gui.InventoryDimensions;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongeUI;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ActiveBidsMenu extends SpongeAsyncPage<SpongeListing> {

    private final Player viewer;
    private static final MessageService<Text> PARSER = Utilities.PARSER;
    private Task runner;

    @SafeVarargs
    public ActiveBidsMenu(Player viewer, Predicate<SpongeListing>... conditions) {
        super(GTSPlugin.getInstance(),
                viewer,
                Impactor.getInstance().getRegistry().get(ListingManager.class).fetchListings(),
                listing -> {
                    if(!listing.hasExpired()) {
                        if (listing instanceof Auction) {
                            Auction auction = (Auction) listing;
                            return auction.getBids().containsKey(viewer.getUniqueId());
                        }
                    }
                    return false;
                }
        );

        this.viewer = viewer;
        this.conditions.addAll(Arrays.asList(conditions));

        final Config lang = GTSPlugin.getInstance().getMsgConfig();
        final MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
        this.applier(listing -> {
            Display<ItemStack> display = listing.getEntry().getDisplay(viewer.getUniqueId(), listing);
            ItemStack item = display.get();

            Optional<List<Text>> lore = item.get(Keys.ITEM_LORE);
            lore.ifPresent(texts -> texts.addAll(service.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_LISTING_DETAIL_SEPARATOR))));
            Supplier<List<Text>> append = () -> {
                List<Text> result = Lists.newArrayList();
                Auction auction = (Auction) listing;
                List<String> input;
                if(auction.getBids().size() > 1) {
                    input = lang.get(MsgConfigKeys.UI_AUCTION_DETAILS_WITH_BIDS);
                } else if(auction.getBids().size() == 1) {
                    input = lang.get(MsgConfigKeys.UI_AUCTION_DETAILS_WITH_SINGLE_BID);
                } else {
                    input = lang.get(MsgConfigKeys.UI_AUCTION_DETAILS_NO_BIDS);
                }
                List<Supplier<Object>> sources = Lists.newArrayList(() -> auction);
                result.addAll(service.parse(input, sources));

                return result;
            };
            List<Text> result = lore.orElse(Lists.newArrayList());
            result.addAll(append.get());
            item.offer(Keys.ITEM_LORE, result);

            SpongeIcon icon = new SpongeIcon(item);
            icon.addListener(clickable -> {
                this.getView().close(this.getViewer());
                new SpongeSelectedListingMenu(this.getViewer(), listing, () -> new SpongeListingMenu(this.getViewer()), true).open();
            });
            return icon;
        });
    }

    @Override
    public void open() {
        super.open();
        this.runner = this.schedule();
        this.getView().attachCloseListener(close -> this.runner.cancel());
    }

    @Override
    protected Text getTitle() {
        return PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.UI_PLAYER_ACTIVE_BIDS_TITLE));
    }

    @Override
    protected Map<PageIconType, PageIcon<ItemType>> getPageIcons() {
        Map<PageIconType, PageIcon<ItemType>> options = Maps.newHashMap();
        options.put(PageIconType.PREV, new PageIcon<>(ItemTypes.ARROW, 48));
        options.put(PageIconType.NEXT, new PageIcon<>(ItemTypes.ARROW, 50));

        return options;
    }

    @Override
    protected InventoryDimensions getContentZone() {
        return new InventoryDimensions(7, 3);
    }

    @Override
    protected Tuple<Integer, Integer> getOffsets() {
        return new Tuple<>(1, 1);
    }

    @Override
    protected Tuple<Long, TimeUnit> getTimeout() {
        return new Tuple<>(5L, TimeUnit.SECONDS);
    }

    @Override
    protected SpongeLayout design() {
        SpongeLayout.SpongeLayoutBuilder slb = SpongeLayout.builder();
        slb.dimension(9, 5).border().dimension(9, 6);
        slb.slots(SpongeIcon.BORDER, 45, 46, 47, 51, 52, 53);

        return slb.build();
    }

    @Override
    protected SpongeUI build(SpongeLayout layout) {
        return SpongeUI.builder()
                .title(this.title)
                .dimension(InventoryDimension.of(9, 6))
                .build()
                .define(this.layout);
    }

    @Override
    protected SpongeIcon getLoadingIcon() {
        return new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DISPLAY_NAME, PARSER.parse(
                        Utilities.readMessageConfigOption(MsgConfigKeys.UI_MENU_LISTINGS_SPECIAL_LOADING),
                        Lists.newArrayList(this::getViewer)
                ))
                .add(Keys.DYE_COLOR, DyeColors.YELLOW)
                .build()
        );
    }

    @Override
    protected SpongeIcon getTimeoutIcon() {
        TitleLorePair pair = Utilities.readMessageConfigOption(MsgConfigKeys.UI_MENU_LISTINGS_SPECIAL_TIMED_OUT);
        return new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DISPLAY_NAME, PARSER.parse(pair.getTitle(), Lists.newArrayList(this::getViewer)))
                .add(Keys.DYE_COLOR, DyeColors.RED)
                .add(Keys.ITEM_LORE, PARSER.parse(pair.getLore(), Lists.newArrayList(this::getViewer)))
                .build()
        );
    }

    @Override
    protected Consumer<List<SpongeListing>> applyWhenReady() {
        return collection -> {};
    }

    private Task schedule() {
        return Sponge.getScheduler().createTaskBuilder()
                .execute(this::apply)
                .interval(1, TimeUnit.SECONDS)
                .submit(GTSPlugin.getInstance().getBootstrap());
    }
}
