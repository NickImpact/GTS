package me.nickimpact.gts.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import me.nickimpact.gts.api.query.SignQuery;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.signview.SpongeSignQuery;
import me.nickimpact.gts.ui.SpongeMainMenu;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

@CommandAlias("gts")
public class GTSCommand extends BaseCommand {

    @Default
    public void execute(Player issuer) {
        new SpongeMainMenu(issuer).open();
    }

    @Subcommand("ignore")
    public void ignore(Player issuer) {

    }

    @Subcommand("test")
    public void test(Player issuer) {
        SignQuery.<Text, Player>builder()
                .text(Lists.newArrayList(
                        Text.EMPTY,
                        Text.of("^^^^^^^^^^^"),
                        Text.of("Enter a Price"),
                        Text.of("for your Listing")
                ))
                .position(new Vector3d(0, 1, 0))
                .build()
                .sendTo(issuer);
    }

    @Subcommand("admin")
    @CommandPermission("gts.commands.admin.base")
    public class Admin extends BaseCommand {

        @Subcommand("ping")
        @CommandPermission("gts.commands.admin.ping")
        public void processPingRequest(CommandIssuer issuer) {
            issuer.sendMessage("Check the console for the status of this message!");
            GTSPlugin.getInstance().getMessagingService().sendPing();
        }

        @Subcommand("edit")
        public void processEditRequest(Player editor) {

        }

    }

}
