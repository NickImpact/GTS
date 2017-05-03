package com.nickimpact.GTS.commands;

import com.google.common.collect.Lists;
import com.nickimpact.GTS.GTS;
import com.nickimpact.GTS.logging.Log;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Created by nickd on 4/17/2017.
 */
public class LogCmd implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<User> target = args.getOne("target");
        if(target.isPresent()){
            Optional<Player> player = target.get().getPlayer();
            player.ifPresent(pl -> {
                if(args.hasAny("r") && !args.hasAny("e")){
                    Log log = GTS.getInstance().getSql().getMostRecentLog(pl.getUniqueId());


                } else if(!args.hasAny("r") && args.hasAny("e")){
                    Log log = GTS.getInstance().getSql().getEarliestLog(pl.getUniqueId());

                } else if(args.hasAny("r") && args.hasAny("e")){
                    Log recent = GTS.getInstance().getSql().getMostRecentLog(pl.getUniqueId());
                    Log earliest = GTS.getInstance().getSql().getEarliestLog(pl.getUniqueId());

                } else {
                    List<Log> logs = GTS.getInstance().getSql().getLogs(pl.getUniqueId());
                    List<Text> lText = Lists.newArrayList();

                    SimpleDateFormat format = new SimpleDateFormat("E MM/dd/yy hh:mm:ss a");
                    for(Log log : logs){
                        List<String> info = Lists.newArrayList(log.getLog().split(Pattern.quote(" | ")));
                        Text infoText = Text.of(TextColors.YELLOW, info.remove(0));
                        for(String line : info){
                            infoText = Text.of(infoText, "\n", TextColors.GRAY, line.substring(0, line.indexOf(":")) + ": ", TextColors.YELLOW, line.substring(line.indexOf(":") + 2));
                        }

                        Text text = Text.of(
                                TextColors.GREEN, String.format(" %4d", log.getId()), TextColors.GRAY, ")   ",
                                TextColors.GOLD, String.format("%18s", log.getAction()) + "   ",
                                Text.builder().append(Text.of(TextColors.GRAY, "[", TextColors.YELLOW, "Log Info", TextColors.GRAY, "]"))
                                              .onClick(TextActions.runCommand("/gts:gts logs list --log-id=" + log.getId()))
                                              .onHover(TextActions.showText(infoText))
                                .build(), "   ",
                                TextColors.WHITE, format.format(log.getDate())
                        );
                        lText.add(text);
                    }

                    PaginationList.builder()
                            .title(Text.of(TextColors.YELLOW, pl.getName() + "'s Logs", TextColors.WHITE))
                            .header(Text.of(
                                    TextColors.GREEN, "   ID   ",
                                    TextColors.WHITE, "|        ",
                                    TextColors.GREEN, "Action        ",
                                    TextColors.WHITE, "|    ",
                                    TextColors.GREEN, "Info    ",
                                    TextColors.WHITE, "|          ",
                                    TextColors.GREEN, "Date\n",
                                    TextColors.WHITE, "-----------------------------------------------------"
                                    )
                            )
                            .contents(lText)
                            .sendTo(src);
                }
            });
        } else {
            if(args.hasAny("log-id")){
                int id = args.<Integer>getOne("log-id").get();
                Log log = GTS.getInstance().getSql().getLog(id);

                List<String> info = Lists.newArrayList(log.getLog().split(Pattern.quote(" | ")));
                List<Text> infoText = Lists.newArrayList();
                infoText.add(Text.of(TextColors.YELLOW, info.remove(0)));
                for(String line : info){
                    infoText.add(Text.of("  ", TextColors.GRAY, line.substring(0, line.indexOf(":")) + ": ", TextColors.YELLOW, line.substring(line.indexOf(":") + 2)));
                }

                for(Text text : infoText){
                    src.sendMessage(text);
                }
            }
        }

        return CommandResult.success();
    }

    public static CommandSpec registerCommand(){
        return CommandSpec.builder()
                .permission("gts.admin.command.log")
                .child(CommandSpec.builder()
                    .executor(new LogCmd())
                    .arguments(
                        GenericArguments.flags()
                            .flag("r")
                            .flag("e")
                            .valueFlag(GenericArguments.integer(Text.of("log-id")), "-log-id")
                            .buildWith(GenericArguments.none()),
                        GenericArguments.optionalWeak(GenericArguments.user(Text.of("target")))
                    )
                    .description(Text.of("Check out the logged actions of a player"))
                .build(), "list")
                .child(CommandSpec.builder()
                    .executor(new LogClearCmd())
                    .arguments(GenericArguments.user(Text.of("target")))
                    .description(Text.of("Clear the logs assigned to a player"))
                .build(), "clear")
        .build();
    }
}
