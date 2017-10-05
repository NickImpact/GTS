package com.nickimpact.GTS.guis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.configuration.MessageConfig;
import com.nickimpact.GTS.guis.exceptions.SearchException;
import com.nickimpact.GTS.utils.Lot;
import com.nickimpact.GTS.utils.LotCache;
import com.nickimpact.GTS.utils.PokemonItem;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.abilities.AbilityBase;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.enums.EnumEggGroup;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.heldItems.EnumHeldItems;
import com.pixelmonmod.pixelmon.enums.items.EnumPokeballs;

public class MainUI extends InventoryBase {

	private Player player;
	private int page = 1;
	private int maxPage;

	private boolean searching;
	private List<String> pokemon;
	private HashMap<String, Object> parameters;

	public MainUI(Player player, int page){
		super(6, Text.of(
				TextColors.RED, "GTS", TextColors.DARK_GRAY, " \u00bb ",
				TextColors.DARK_GREEN, "Listings"
				));

		this.player = player;
		this.page = page;

		this.searching = false;
		this.pokemon = Lists.newArrayList();
		this.parameters = Maps.newHashMap();

		int size = GTS.getInstance().getLots().size();
		this.maxPage = size % 28 == 0 && size / 28 != 0 ?
				size / 28 :
					size / 28 + 1;

		this.setupDesign();

		try {
			this.setupListings();
		}
		catch(SearchException e){
			player.sendMessage(e.getResult());
		}
	}

	public MainUI(Player player, int page, boolean searching, List<String> pokemon, HashMap<String, Object> parameters){
		super(6, Text.of(
				TextColors.RED, "GTS", TextColors.DARK_GRAY, " \u00bb ",
				TextColors.DARK_GREEN, "Listings"
				));

		this.player = player;
		this.page = page;

		this.searching = searching;
		this.pokemon = pokemon;
		this.parameters = parameters;

		if(!this.searching) {
			int size = GTS.getInstance().getLots().size();
			this.maxPage = size % 28 == 0 && size / 28 != 0 ?
					size / 28 :
						size / 28 + 1;
		} else
			try {
				int size = gatherSearchResults().size();
				this.maxPage = size % 28 == 0 ?
						size / 28 :
							size / 28 + 1;
			} catch (SearchException e) {
				this.maxPage = 1;
			}

		this.setupDesign();

		try {
			this.setupListings();
		}
		catch(SearchException e){
			player.sendMessage(e.getResult());
		}
	}

	/**
	 * Initializes base design to the UI, establishing borders and
	 * other items
	 */
	private void setupDesign() {
		for(int x = 7, y = 0; y <= 4; y++){
			this.addIcon(SharedItems.forgeBorderIcon(x + (9 * y), DyeColors.BLACK));
		}

		for(int x = 0, y = 4; x < 7; x++){
			this.addIcon(SharedItems.forgeBorderIcon(x + (9 * y), DyeColors.BLACK));
		}

		// Side Panel
		InventoryIcon pageUp = SharedItems.pageIcon(8, true, page, page < maxPage ? page + 1 : 1);
		pageUp.addListener(ClickInventoryEvent.class, e -> {
			Player p = e.getCause().first(Player.class).get();

			Sponge.getScheduler().createTaskBuilder()
			.execute(() -> this.updatePage(p, true))
			.delayTicks(1)
			.submit(GTS.getInstance());
		});
		this.addIcon(pageUp);

		InventoryIcon pageDown = SharedItems.pageIcon(17, false, page, page > 1 ? page - 1 : maxPage);
		pageDown.addListener(ClickInventoryEvent.class, e -> {
			Player p = e.getCause().first(Player.class).get();

			Sponge.getScheduler().createTaskBuilder()
			.execute(() -> this.updatePage(p, false))
			.delayTicks(1)
			.submit(GTS.getInstance());
		});
		this.addIcon(pageDown);

		InventoryIcon refresh = SharedItems.refreshIcon(35);
		refresh.addListener(ClickInventoryEvent.class, e -> {
			Player p = e.getCause().first(Player.class).get();

			try {
				this.setupListings();
			} catch (SearchException e1) {
				p.sendMessage(e1.getResult());
				p.closeInventory();
			}
			this.updateContents();
		});
		this.addIcon(refresh);

		// Bottom Panel
		this.addIcon(SharedItems.balanceIcon(45, player));
		this.addIcon(SharedItems.searchIcon(48, pokemon));
		InventoryIcon plListings = SharedItems.playerListingsIcon(51);
		plListings.addListener(ClickInventoryEvent.class, e -> {
			Player p = e.getCause().first(Player.class).get();

			Sponge.getScheduler().createTaskBuilder().execute(() -> {
				p.closeInventory();
				p.openInventory(new PlayerListings(p, 1).getInventory());
			}).delayTicks(1).submit(GTS.getInstance());
		});
		this.addIcon(plListings);
	}

	/**
	 * Gathers all listings per page, and displays them into the system with all their
	 * assigned functions
	 */
	private void setupListings() throws SearchException {
		int index = (this.page - 1) * 28;
		List<LotCache> lots;
		if(this.searching){
			lots = this.gatherSearchResults();
		} else {
			lots = GTS.getInstance().getLots();
		}

		for(int x = 0, y = 0; y < 4; index++){
			if(x == 7 && y != 3){
				x = 0;
				y++;
			} else if(x == 7){
				break;
			}

			if(index >= lots.size()) {
				this.getAllIcons().remove(x + (9 * y));
				x++;
				continue;
			}

			if(lots.get(index).isExpired()) continue;
			Lot lot = lots.get(index).getLot();
			PokemonItem item = lot.getItem();
			InventoryIcon pokemon = new InventoryIcon(x + (9 * y), item.getItem(lots.get(index)));
			pokemon.addListener(ClickInventoryEvent.class, e -> {
				Player p = e.getCause().first(Player.class).get();

				int lotID = lot.getLotID();
				Optional<LotCache> lotCache = GTS.getInstance().getLots().stream().filter(l -> l.getLot().getLotID() == lotID).findFirst();
				if(!lotCache.isPresent()){
					for(Text text : MessageConfig.getMessages("Generic.Purchase.Error.Already Sold", null))
						p.sendMessage(text);
				} else {
					Sponge.getScheduler().createTaskBuilder().execute(() -> {
						p.closeInventory();

						if(this.searching){
							p.openInventory(new LotUI(p, lotCache.get(), this.page, this.searching, this.pokemon, this.parameters, false).getInventory());
						} else {
							if(lotCache.get().getLot().isTrade()){
								if(!this.player.getUniqueId().equals(lotCache.get().getLot().getOwner())) {
									p.openInventory(new TradePartySelection(this.player, lotCache.get(), this).getInventory());
								} else {
									p.openInventory(new LotUI(this.player, lotCache.get(), false, this.page).getInventory());
								}
							} else {
								p.openInventory(new LotUI(p, lotCache.get(), false, this.page).getInventory());
							}
						}
					}).delayTicks(1).submit(GTS.getInstance());
				}
			});
			this.addIcon(pokemon);
			x++;
		}
	}

	/**
	 * Updates the page of the inventory, then proceeds to update displayed listings
	 *
	 * @param upOrDown True = Page Up, False = Page Down
	 */
	private void updatePage(Player player, boolean upOrDown) {
		if(upOrDown){
			if(this.page < maxPage)
				++this.page;
			else
				this.page = 1;
		} else {
			if(this.page > 1)
				--this.page;
			else
				this.page = maxPage;
		}

		InventoryIcon pageUp = SharedItems.pageIcon(8, true, page, page < maxPage ? page + 1 : 1);
		pageUp.addListener(ClickInventoryEvent.class, e -> {
			Player p = e.getCause().first(Player.class).get();

			this.updatePage(p, true);
		});
		this.addIcon(pageUp);

		InventoryIcon pageDown = SharedItems.pageIcon(17, false, page, page > 1 ? page - 1 : maxPage);
		pageDown.addListener(ClickInventoryEvent.class, e -> {
			Player p = e.getCause().first(Player.class).get();

			this.updatePage(p, false);
		});
		this.addIcon(pageDown);

		try {
			this.setupListings();
		} catch (SearchException e) {
			player.sendMessage(e.getResult());
			player.closeInventory();
		}
		this.updateContents();
	}

	/**
	 * Attempts to locate all listings which match the specified search criteria
	 *
	 * @return A List of all listings matching the search criteria
	 * @throws SearchException Thrown when any search condition meets an ambiguity
	 */
	private List<LotCache> gatherSearchResults() throws SearchException {
		List<LotCache> valid = Lists.newArrayList();

		for(LotCache lot : GTS.getInstance().getLots()) {
			boolean addLot = true;
			EntityPixelmon poke = lot.getLot().getItem().getPokemon(lot.getLot());
			if(this.pokemon.size() == 0 || this.pokemon.stream().anyMatch(p -> p.equalsIgnoreCase(poke.getName()))) {
				for (String flag : parameters.keySet()) {
					switch (flag) {
					case "s":
					case "shiny":
						if (!poke.getIsShiny())
							addLot = false;
						break;
					case "ab":
					case "ability":
						String ability = String.valueOf(parameters.get(flag));
						Optional<AbilityBase> ab = AbilityBase.getAbility(ability);
						if (ab.isPresent()) {
							if (!poke.getAbility().getName().equalsIgnoreCase(ability))
								addLot = false;
							break;
						}

						throw new SearchException("Invalid ability: " + ability);
					case "size":
					case "growth":
						String growth = String.valueOf(parameters.get(flag));
						if (!EnumGrowth.hasGrowth(growth))
							throw new SearchException("The specified growth doesn't exist...");
						if (!poke.getGrowth().name().equalsIgnoreCase(growth))
							addLot = false;
						break;
					case "lvl":
					case "level":
						int lvl = (Integer) parameters.get(flag);
						if (lvl < 1 || lvl > PixelmonConfig.maxLevel)
							throw new SearchException(
									"The level must be between 1-" + PixelmonConfig.maxLevel + "...");

						if (poke.getLvl().getLevel() != lvl)
							addLot = false;
						break;
					case "ba":
					case "ball":
						String ball = String.valueOf(parameters.get(flag));
						if (!EnumPokeballs.hasPokeball(ball))
							throw new SearchException("The specified pokeball doesn't exist...");

						if (!poke.caughtBall.name().equalsIgnoreCase(ball))
							addLot = false;
						break;
					case "ge":
					case "gender":
						String gender = String.valueOf(parameters.get(flag));
						if (!gender.equalsIgnoreCase("male") && !gender.equalsIgnoreCase(
								"female") && !gender.equalsIgnoreCase("none") && !gender.equalsIgnoreCase(
										"genderless"))
							throw new SearchException("The specified gender doesn't exist...");

						if (!poke.gender.name().equalsIgnoreCase(gender))
							addLot = false;
						break;
					case "na":
					case "nature":
						String nature = String.valueOf(parameters.get(flag));
						if (!EnumNature.hasNature(nature))
							throw new SearchException("The specified nature doesn't exist...");

						if (!poke.getNature().name().equalsIgnoreCase(nature))
							addLot = false;
						break;
					case "f":
					case "form":
						int form = (Integer) parameters.get(flag);
						if (form < -1)
							throw new SearchException("Form values must be -1 or more...");

						if (poke.getForm() != form)
							addLot = false;
						break;
					case "evHP":
						int evHp = (Integer) parameters.get(flag);
						if (evHp < 0 || evHp > 255)
							throw new SearchException("EVs must be between 0-255...");

						if (poke.stats.EVs.HP != evHp)
							addLot = false;
						break;
					case "evAtk":
						int evAtk = (Integer) parameters.get(flag);
						if (evAtk < 0 || evAtk > 255)
							throw new SearchException("EVs must be between 0-255...");

						if (poke.stats.EVs.Attack != evAtk)
							addLot = false;
						break;
					case "evDef":
						int evDef = (Integer) parameters.get(flag);
						if (evDef < 0 || evDef > 255)
							throw new SearchException("EVs must be between 0-255...");

						if (poke.stats.EVs.Defence != evDef)
							addLot = false;
						break;
					case "evSpAtk":
						int evSpAtk = (Integer) parameters.get(flag);
						if (evSpAtk < 0 || evSpAtk > 255)
							throw new SearchException("EVs must be between 0-255...");

						if (poke.stats.EVs.SpecialAttack != evSpAtk)
							addLot = false;
						break;
					case "evSpDef":
						int evSpDef = (Integer) parameters.get(flag);
						if (evSpDef < 0 || evSpDef > 255)
							throw new SearchException("EVs must be between 0-255...");

						if (poke.stats.EVs.SpecialDefence != evSpDef)
							addLot = false;
						break;
					case "evSpeed":
						int evSpeed = (Integer) parameters.get(flag);
						if (evSpeed < 0 || evSpeed > 255)
							throw new SearchException("EVs must be between 0-255...");

						if (poke.stats.EVs.Speed != evSpeed)
							addLot = false;
						break;
					case "ivHP":
						int ivHp = (Integer) parameters.get(flag);
						if (ivHp < 0 || ivHp > 31)
							throw new SearchException("IVs must be between 0-31...");

						if (poke.stats.IVs.HP != ivHp)
							addLot = false;
						break;
					case "ivAtk":
						int ivAtk = (Integer) parameters.get(flag);
						if (ivAtk < 0 || ivAtk > 31)
							throw new SearchException("IVs must be between 0-31...");

						if (poke.stats.IVs.Attack != ivAtk)
							addLot = false;
						break;
					case "ivDef":
						int ivDef = (Integer) parameters.get(flag);
						if (ivDef < 0 || ivDef > 31)
							throw new SearchException("IVs must be between 0-31...");

						if (poke.stats.IVs.Defence != ivDef)
							addLot = false;
						break;
					case "ivSpAtk":
						int ivSpAtk = (Integer) parameters.get(flag);
						if (ivSpAtk < 0 || ivSpAtk > 31)
							throw new SearchException("IVs must be between 0-31...");

						if (poke.stats.IVs.SpAtt != ivSpAtk)
							addLot = false;
						break;
					case "ivSpDef":
						int ivSpDef = (Integer) parameters.get(flag);
						if (ivSpDef < 0 || ivSpDef > 31)
							throw new SearchException("IVs must be between 0-31...");

						if (poke.stats.IVs.SpDef != ivSpDef)
							addLot = false;
						break;
					case "ivSpeed":
						int ivSpeed = (Integer) parameters.get(flag);
						if (ivSpeed < 0 || ivSpeed > 31)
							throw new SearchException("IVs must be between 0-31...");

						if (poke.stats.IVs.SpDef != ivSpeed)
							addLot = false;
						break;
					case "friendship":
						int fr = (Integer) parameters.get(flag);
						if (fr < 0 || fr > 255)
							throw new SearchException("Friendship must be between 0-255...");

						if (poke.friendship.getFriendship() < fr)
							addLot = false;
						break;
					case "hasMove":
						Moveset moves = poke.getMoveset();
						boolean found = false;
						for (Attack atk : moves.attacks) {
							if (atk == null) continue;
							if (atk.baseAttack.getLocalizedName().equalsIgnoreCase(
									String.valueOf(parameters.get(flag)))) {
								found = true;
								break;
							}
						}
						if (!found) addLot = false;
						break;
					case "eggGroup":
						String eg = String.valueOf(parameters.get(flag));
						if (!EnumEggGroup.hasEggGroup(eg))
							throw new SearchException("The specified egg group doesn't exist...");

						boolean egFound = false;
						for (EnumEggGroup eggGroup : poke.baseStats.eggGroups) {
							if (eggGroup.name().equalsIgnoreCase(eg)) {
								egFound = true;
								break;
							}
						}
						if (!egFound) addLot = false;
						break;
					case "st":
					case "specialTexture":
						if (poke.getSpecialTexture() == 0)
							addLot = false;
						break;
					case "halloween":
					case "zombie":
						if (poke.getSpecialTexture() != 2)
							addLot = false;
						break;
					case "roasted":
						if (poke.getSpecialTexture() != 1)
							addLot = false;
						break;
					case "minPrice":
						int minPrice = (Integer) parameters.get(flag);
						if (minPrice < 0)
							throw new SearchException("Min Price must be a positive number!");

						if (lot.getLot().getPrice() < minPrice)
							addLot = false;
						break;
					case "maxPrice":
						int maxPrice = (Integer) parameters.get(flag);
						if (maxPrice < 0)
							throw new SearchException("Max Price must be a positive number!");

						if (lot.getLot().getPrice() > maxPrice)
							addLot = false;
						break;
					case "seller":
						if (!lot.getLot().getItem().getOwner().equalsIgnoreCase(
								String.valueOf(parameters.get(flag))))
							addLot = false;
						break;
					case "he":
					case "heldItem":
						String heldItem = String.valueOf(parameters.get(flag));
						if (Arrays.stream(EnumHeldItems.values()).noneMatch(
								h -> h.name().equalsIgnoreCase(heldItem)))
							throw new SearchException("The specified held item doesn't exist...");

						if (!poke.getItemHeld().getLocalizedName().equalsIgnoreCase(heldItem))
							addLot = false;
						break;
					case "auc":
					case "auction":
						if (!lot.getLot().isAuction())
							addLot = false;
						break;
					case "cash":
						if (lot.getLot().isAuction() || lot.getLot().isTrade())
							addLot = false;
						break;
					case "pokemon":
						if (lot.getLot().isAuction() || !lot.getLot().isTrade())
							addLot = false;
						break;
					}
				}
				if(addLot)
					valid.add(lot);
			}
		}
		return valid;
	}
}
