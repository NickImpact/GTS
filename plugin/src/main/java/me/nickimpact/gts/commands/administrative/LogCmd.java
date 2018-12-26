package me.nickimpact.gts.commands.administrative;

import com.google.common.collect.Lists;
import me.nickimpact.gts.GTS;
import me.nickimpact.gts.GTSInfo;
import me.nickimpact.gts.api.commands.arguments.DateArg;
import me.nickimpact.gts.logs.Log;
import com.nickimpact.impactor.api.commands.SpongeCommand;
import com.nickimpact.impactor.api.commands.SpongeSubCommand;
import com.nickimpact.impactor.api.commands.annotations.Aliases;
import com.nickimpact.impactor.api.commands.annotations.Permission;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.time.temporal.ChronoField;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@Aliases({"logs"})
@Permission(admin = true)
public class LogCmd extends SpongeSubCommand {

	private final Text USER = Text.of("user");
	private final Text LOG = Text.of("log");

	private final Text FROM = Text.of("from");
	private final Text TO = Text.of("to");

	public LogCmd(SpongePlugin plugin) {
		super(plugin);
	}

	@Override
	public CommandElement[] getArgs() {
		return new CommandElement[] {
				GenericArguments.user(USER),
				GenericArguments.optional(new DateArg(FROM)),
				GenericArguments.optional(new DateArg(TO)),
				GenericArguments.optional(GenericArguments.integer(LOG))
		};
	}

	@Override
	public Text getDescription() {
		return Text.of();
	}

	@Override
	public Text getUsage() {
		return Text.of("/gts admin logs <user> (log id)");
	}

	@Override
	public SpongeCommand[] getSubCommands() {
		return new SpongeCommand[0];
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		User user = args.<User>getOne(USER).get();
		Optional<Date> from = args.getOne(FROM);
		Optional<Date> to = args.getOne(TO);
		Optional<Integer> id = args.getOne(LOG);

		src.sendMessage(Text.of(GTSInfo.PREFIX, TextColors.GRAY, "Fetching logs, please wait..."));
		GTS.getInstance().getStorage().getLogs(user.getUniqueId()).thenAccept(lgs -> {
			Predicate<Log> predicate = log -> {
				if(from.isPresent()) {
					return to.map(date -> {
						if(date.equals(from.get())) {
							Calendar cal = new GregorianCalendar(date.toInstant().get(ChronoField.YEAR), date.toInstant().get(ChronoField.MONTH_OF_YEAR), date.toInstant().get(ChronoField.DAY_OF_MONTH));
							cal.set(Calendar.HOUR_OF_DAY, 23);
							cal.set(Calendar.MINUTE, 59);
							return log.getDate().after(from.get()) && log.getDate().before(cal.getTime());
						}
						return log.getDate().after(from.get()) && log.getDate().before(date);
					}).orElseGet(() -> log.getDate().after(from.get()));
				} else return to.map(date -> log.getDate().before(date)).orElse(true);
			};

			List<Log> collection = lgs.stream().filter(predicate).collect(Collectors.toList());

			if(id.isPresent()) {
				Log log = collection.get(id.get() - 1);
				src.sendMessages(log.getHover().stream().map(TextSerializers.FORMATTING_CODE::deserialize).collect(Collectors.toList()));
			} else {
				try {
					List<Text> info = Lists.newArrayList(Text.EMPTY);
					int index = 0;
					for (Log log : collection) {
						info.add(log.toText(src, ++index));
					}


					from.ifPresent(date -> src.sendMessage(Text.of(TextColors.GRAY, "From: ", TextColors.YELLOW, Log.sdf.format(date))));
					to.ifPresent(date -> src.sendMessage(Text.of(TextColors.GRAY, "To: ", TextColors.YELLOW, Log.sdf.format(date))));
					Text header = Text.of(TextColors.GRAY, src instanceof Player ? "Hover over an entry for more info!" : "Specify a log ID for more info!");
					PaginationList.builder()
							.title(Text.of(TextColors.YELLOW, user.getName(), "'s Logs"))
							.header(header)
							.contents(info)
							.linesPerPage(8)
							.sendTo(src);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		return CommandResult.success();
	}
}
