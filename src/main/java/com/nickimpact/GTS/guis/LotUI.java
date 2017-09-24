package com.nickimpact.GTS.guis;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.configuration.MessageConfig;
import com.nickimpact.GTS.logging.Log;
import com.nickimpact.GTS.utils.LotCache;
import com.nickimpact.GTS.utils.LotUtils;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;

import net.minecraft.server.MinecraftServer;

public class LotUI extends InventoryBase {

	private Player player;
	private LotCache lot;

	private int slot;
	private int page;
	private boolean searching;
	private List<String> pokemon;
	private HashMap<String, Object> parameters;
	private boolean admin;

	public LotUI(Player player, LotCache lot, boolean trade, int index){
		super(3, Text.of(TextColors.RED, "GTS", TextColors.DARK_GRAY, " \u00bb ", TextColors.DARK_GREEN, "Confirm"));

		this.player = player;
		this.lot = lot;

		if(trade){
			this.slot = index;
			this.page = 1;
		} else {
			this.slot = -1;
			this.page = index;
		}
		this.searching = false;
		this.pokemon = Lists.newArrayList();
		this.parameters = Maps.newHashMap();
		this.admin = false;

		this.setupDesign();
	}

	public LotUI(Player player, LotCache lot, int page, boolean searching, List<String> pokemon, HashMap<String, Object> parameters, boolean admin) {
		super(3, Text.of(TextColors.RED, "GTS", TextColors.DARK_GRAY, " \u00bb ", TextColors.DARK_GREEN, "Confirm"));

		this.player = player;
		this.lot = lot;

		this.slot = -1;
		this.page = page;
		this.searching = searching;
		this.pokemon = pokemon;
		this.parameters = parameters;
		this.admin = admin;

		this.setupDesign();
	}

	private void setupDesign(){
		// Initialize border design
		for(int x = 0, y = 0; y < 3; x++){
			if(x == 9 && y == 0){
				x = 0;
				y += 2;
			} else if(x == 9){ // Second run through, break there
				break;
			}
			this.addIcon(SharedItems.forgeBorderIcon(x + (9 * y), DyeColors.BLACK));
		}

		this.addIcon(SharedItems.forgeBorderIcon(9, DyeColors.BLACK));
		this.addIcon(SharedItems.forgeBorderIcon(11, DyeColors.BLACK));
		this.addIcon(SharedItems.forgeBorderIcon(17, DyeColors.BLACK));

		// Draw the 4 other icons with their functions
		this.addIcon(new InventoryIcon(10, lot.getLot().getItem().getItem(lot)));

		if(admin){
			List<Text> lore = Lists.newArrayList(
					Text.of(TextColors.GREEN, "Left Click"),
					Text.of(TextColors.GRAY, "    * Remove and give pokemon back"),
					Text.of(TextColors.RED, "Right Click"),
					Text.of(TextColors.GRAY, "    * Remove and delete pokemon")
					);
			InventoryIcon admin = new InventoryIcon(12, ItemStack.builder()
					.itemType(ItemTypes.ANVIL)
					.quantity(1)
					.keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Remove from GTS"))
					.keyValue(Keys.ITEM_LORE, lore)
					.build()
					);
			admin.addListener(ClickInventoryEvent.class, e -> {
				Player p = e.getCause().first(Player.class).get();
				HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
				textOptions.put("pokemon", Optional.of(lot.getLot().getItem().getName()));

				if(GTS.getInstance().getLots().stream().noneMatch(lot -> lot.getLot().getLotID() == this.lot.getLot().getLotID()))
					return;

				if (e instanceof ClickInventoryEvent.Secondary) {
					for (Text text : MessageConfig.getMessages("Administrative.LotUI.Delete", textOptions))
						p.sendMessage(text);

					for (int i = 0; i < GTS.getInstance().getLots().size(); i++) {
						if (GTS.getInstance().getLots().get(i).getLot().getLotID() == lot.getLot().getLotID()) {
							GTS.getInstance().getLots().remove(i);
							break;
						}
					}

					LotUtils.deleteLot(lot.getLot().getLotID());
				} else if (e instanceof ClickInventoryEvent.Primary){
					for(Text text : MessageConfig.getMessages("Administrative.LotUI.Remove", textOptions))
						p.sendMessage(text);

					textOptions.putAll(LotUtils.getInfo(lot.getLot().getItem().getPokemon(lot.getLot())));
					Log log = LotUtils.forgeLog(Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(lot.getLot().getOwner()).get(), "Removal", textOptions);
					LotUtils.addLog(lot.getLot().getOwner(), log);
					Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer) Sponge.getServer(), p.getUniqueId());
					if(storage.isPresent()) {
						storage.get().addToParty(lot.getLot().getItem().getPokemon(lot.getLot()));
						storage.get().sendUpdatedList();
					} else {
						GTS.getInstance().getLogger().error("Error occurred in Lot Confirmation for " + p.getName());
					}
					for (int i = 0; i < GTS.getInstance().getLots().size(); i++){
						if (GTS.getInstance().getLots().get(i).getLot().getLotID() == lot.getLot().getLotID()) {
							GTS.getInstance().getLots().remove(i);
							break;
						}
					}

					LotUtils.deleteLot(lot.getLot().getLotID());
				}
				Sponge.getScheduler().createTaskBuilder().execute(() ->
				p.closeInventory()
						).delayTicks(1).submit(GTS.getInstance());
			});
			this.addIcon(admin);
		} else {
			if(lot.getLot().getOwner().equals(player.getUniqueId())){
				InventoryIcon remove = new InventoryIcon(12, ItemStack.builder()
						.itemType(ItemTypes.ANVIL)
						.quantity(1)
						.keyValue(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Remove from GTS"))
						.build()
						);
				remove.addListener(ClickInventoryEvent.class, e -> {
					Player p = e.getCause().first(Player.class).get();
					HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
					textOptions.put("pokemon", Optional.of(lot.getLot().getItem().getName()));

					if(GTS.getInstance().getLots().stream().noneMatch(lot -> lot.getLot().getLotID() == this.lot.getLot().getLotID()))
						return;

					for(Text text : MessageConfig.getMessages("Generic.Remove.Success", textOptions))
						p.sendMessage(text);
					Optional<PlayerStorage> storage = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID((MinecraftServer) Sponge.getServer(), p.getUniqueId());
					if(storage.isPresent()) {
						storage.get().addToParty(lot.getLot().getItem().getPokemon(lot.getLot()));
						storage.get().sendUpdatedList();
					} else {
						GTS.getInstance().getLogger().error("Error occurred in Lot Confirmation for " + p.getName());
					}

					for (int i = 0; i < GTS.getInstance().getLots().size(); i++){
						if (GTS.getInstance().getLots().get(i).getLot().getLotID() == lot.getLot().getLotID()) {
							GTS.getInstance().getLots().remove(i);
							break;
						}
					}

					LotUtils.deleteLot(lot.getLot().getLotID());
					textOptions.putAll(LotUtils.getInfo(lot.getLot().getItem().getPokemon(lot.getLot())));
					Log log = LotUtils.forgeLog(p, "Removal", textOptions);
					LotUtils.addLog(p.getUniqueId(), log);

					Sponge.getScheduler().createTaskBuilder().execute(() ->
					p.closeInventory()
							).delayTicks(1).submit(GTS.getInstance());
				});
				this.addIcon(remove);
			} else {
				InventoryIcon confirm = SharedItems.confirmIcon(12);
				confirm.addListener(ClickInventoryEvent.class, e -> {
					Player p = e.getCause().first(Player.class).get();
					HashMap<String, Optional<Object>> textOptions = Maps.newHashMap();
					textOptions.put("pokemon", Optional.of(lot.getLot().getItem().getName()));

					if(GTS.getInstance().getLots().stream().noneMatch(lot -> lot.getLot().getLotID() == this.lot.getLot().getLotID()))
						return;

					LotCache lc = GTS.getInstance().getLots().stream().filter(l -> l.getLot().getLotID() == lot.getLot().getLotID()).findAny().orElse(null);
					if(lc != null){
						if(lc.getLot().isAuction())
							LotUtils.bid(p, lc.getLot());
						else
							if(lc.getLot().isTrade())
								LotUtils.trade(p, lc, this.slot);
							else
								LotUtils.buyLot(p, lc);
					} else {
						for(Text text : MessageConfig.getMessages("Generic.Purchase.Failed", textOptions))
							p.sendMessage(text);
					}

					Sponge.getScheduler().createTaskBuilder().execute(() ->
					p.closeInventory()
							).delayTicks(1).submit(GTS.getInstance());
				});
				this.addIcon(confirm);
			}
		}

		InventoryIcon cancel = SharedItems.denyIcon(16);
		cancel.addListener(ClickInventoryEvent.class, e -> {
			Sponge.getScheduler().createTaskBuilder().execute(() -> {
				Player p = e.getCause().first(Player.class).get();
				p.closeInventory();
				p.openInventory(new MainUI(p, this.page, this.searching, this.pokemon, this.parameters).getInventory());
			}).delayTicks(1).submit(GTS.getInstance());
		});
		this.addIcon(cancel);

		this.addIcon(new InventoryIcon(14, lot.getLot().getItem().setStats()));
	}
}
