package com.nickimpact.gts.api.commands.arguments;

import com.google.common.collect.Lists;
import com.nickimpact.gts.api.time.Time;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.List;

public class TimeArg extends CommandElement {

	public TimeArg(@Nullable Text key) {
		super(key);
	}

	@Nullable
	@Override
	protected Time parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
		String arg = args.next();
		Time result;
		if(arg.equals("-1")) {
			return Time.SPECIAL;
		}

		try {
			result = new Time(Long.parseLong(arg));
		} catch (Exception e) {
			try {
				result = new Time(arg);
			} catch (Exception e1) {
				return Time.INVALID;
			}
		}
		return result;
	}

	@Override
	public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
		return Lists.newArrayList();
	}
}
