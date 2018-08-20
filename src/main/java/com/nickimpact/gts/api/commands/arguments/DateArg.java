package com.nickimpact.gts.api.commands.arguments;

import com.google.common.collect.Lists;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateArg extends CommandElement {

	private static final Pattern DATE_FORMAT = Pattern.compile("^(?<month>[0-9][0-9]?)/(?<day>[0-9][0-9]?)/(?<year>[0-9]{2,4})(-(?<hour>[0-9][0-9]?)(:(?<minute>[0-9][0-9]))?)?$");

	private static final String YEAR_FIX = "20xx";

	public DateArg(@Nullable Text key) {
		super(key);
	}

	@SuppressWarnings("MagicConstant")
	@Nullable
	@Override
	protected Date parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
		String arg = args.next();
		Matcher matcher = DATE_FORMAT.matcher(arg);
		if(matcher.matches()) {
			int month = Integer.parseInt(matcher.group("month")) - 1;
			int day = Integer.parseInt(matcher.group("day"));
			String y = matcher.group("year");
			String proper = "";
			if(y.length() < 4) {
				for(int i = 0; i < 2; i++) {
					proper = String.valueOf(YEAR_FIX.charAt(i));
					if((proper + y).length() == 4) {
						break;
					}
				}
			} else {
				proper = y;
			}
			int year = Integer.parseInt(proper);
			String hourS = matcher.group("hour");
			String minuteS = matcher.group("minute");

			int hour = hourS != null ? Integer.parseInt(hourS) : -1;
			int minute = minuteS != null ? Integer.parseInt(minuteS) : -1;
			if(hour == 24) {
				hour = 0;
			}
			if(minute == 60) {
				minute = 0;
			}

			if(this.validate(month, day, year, hour, minute)) {
				return new GregorianCalendar(year, month, day, hour != -1 ? hour : 0, minute != -1 ? minute : 0).getTime();
			} else {
				throw args.createError(Text.of("The specified time is of an incorrect format, or breaches time constraints..."));
			}
		}

		throw args.createError(Text.of("The specified time is of an incorrect format, or breaches time constraints..."));
	}

	@Override
	public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
		return Lists.newArrayList();
	}

	private boolean validate(int m, int d, int y, int h, int min) {
		if(m < 1 || m > 12) {
			return false;
		}

		Calendar tester = new GregorianCalendar(y, m, 1);
		int days = tester.getActualMaximum(Calendar.DAY_OF_MONTH);
		if(d < 1 || d > days) {
			return false;
		}

		if(h != -1 && h > 23) {
			return false;
		}

		return min == -1 || min <= 59;
	}
}
