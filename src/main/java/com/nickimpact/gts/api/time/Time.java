package com.nickimpact.gts.api.time;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class serves to represent a given time reference. Commonly, we will find
 * instances where we need to forge a certain time frame from a command line
 * argument. In this instance, we need a way to create a Time from many options,
 * such as, but not limited to, milliseconds and strings met with a pattern format.
 *
 * NOTE: This class does not follow a Timezone, it's meant purely to represent
 * a specific duration in time for some scenario.
 *
 * @author NickImpact
 */
public class Time
{
    private long time;

    private static final int secondsPerMinute = 60;
    private static final int secondsPerHour = secondsPerMinute * 60;
    private static final int secondsPerDay = secondsPerHour * 24;
    private static final int secondsPerWeek = secondsPerDay * 7;

    /** Returns a {@link Time} object representing how long the given milliseconds is in weeks, days, hours, minutes and seconds */
    public Time(long milliseconds)
    {
        this.time = (milliseconds / 1000);
    }

    /** Returns a {@link Time} object given the string representation e.g. 3w5d9h3m1s*/
    public Time(String formattedTime) throws IllegalArgumentException
    {
        final Pattern minorTimeString = Pattern.compile("^\\d+$");
        final Pattern timeString = Pattern.compile("^((\\d+)w)?((\\d+)d)?((\\d+)h)?((\\d+)m)?((\\d+)s)?$");

        if(minorTimeString.matcher(formattedTime).matches()) {
            this.time += Long.parseUnsignedLong(formattedTime);
            return;
        }

        Matcher m = timeString.matcher(formattedTime);
        if(m.matches()) {
            this.time =  amount(m.group(2), secondsPerWeek);
            this.time += amount(m.group(4), secondsPerDay);
            this.time += amount(m.group(6), secondsPerHour);
            this.time += amount(m.group(8), secondsPerMinute);
            this.time += amount(m.group(10), 1);
        }
    }

    private long amount(String g, int multiplier) {
        if(g != null && g.length() > 0) {
            return multiplier * Long.parseUnsignedLong(g);
        }

        return 0;
    }

    public Instant getAsInstant() throws InvalidTimeException
    {
    	if(this.time <= 0) {
    		throw new InvalidTimeException();
	    }

        return Instant.EPOCH.plusSeconds(this.time);
    }

    public long getTime() {
    	return this.time;
    }

    @Override
    public String toString()
    {
	    if(time <= 0)
		    return "Error";

	    return "Weeks: " + this.time % secondsPerWeek + '\n' +
			    "Days:  " + (this.time % secondsPerDay) + '\n' +
			    new DecimalFormat("00").format(this.time % secondsPerHour) + ':' +
			    new DecimalFormat("00").format(this.time % secondsPerMinute) + ':' +
			    new DecimalFormat("00").format(this.time % 60);
    }
}
