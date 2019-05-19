package me.nickimpact.gts.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.ui.SpigotMainUI;
import org.bukkit.entity.Player;

@CommandAlias("gts")
@Description("Controls the functionality of GTS")
@CommandPermission("gts.command.gts.base")
public class SpigotGtsCmd extends BaseCommand {

	@Default
	public void execute(Player player) {
		new SpigotMainUI(player).open();
	}

	@Subcommand("sell|add")
	@CommandPermission("gts.command.sell.base")
	public class SellSub extends BaseCommand {

		@Default
		public void execute(Player player, @Optional EntryClassification classification, @Optional String... additionals) {
			if(classification == null) {
				// TODO - Entry UI base
			} else {
				if(player.hasPermission("gts.command.sell." + classification.getPrimaryIdentifier().toLowerCase())) {
					if(additionals.length == 0) {
						classification.getUi().createFor(player).getDisplay().open(player);
					} else {
						classification.getCmdHandler().apply(player, additionals);
					}
				}
			}
		}
	}
}
