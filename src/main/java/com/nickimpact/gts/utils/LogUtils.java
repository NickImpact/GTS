package com.nickimpact.gts.utils;

import com.google.common.collect.Lists;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.logs.Log;
import com.nickimpact.gts.logs.LogAction;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class LogUtils {

	/** The current log index of the program */
    private static int current;

	/**
	 * Creates a log using the specified information. The template represents the hover action that a user will
	 * have when they hover over the "[Log Info]" icon in their chat. The info in this template will be updated
	 * by the replacements mapping.
	 *
	 * @param user The user the log is being built for
	 * @param action The action that provoked the log's creation
	 * @param template The template used for the hover action
	 * @param replacements A mapping that holds potential replacement options for a log template
	 * @return A log composed of all passed in data
	 */
    public static Log forgeLog(User user, LogAction action, List<String> template, Map<String, Function<CommandSource, Optional<Text>>> replacements) {
        Log.Builder lb = Log.builder().id(current++).action(action).source(user.getUniqueId());

        List<String> base = Lists.newArrayList();
	    try {
		    base.addAll(GTS.getInstance().getTextParsingUtils().parse(
				    GTS.getInstance().getTextParsingUtils().getTemplates(template),
				    Sponge.getServer().getConsole(),
				    replacements,
				    null
		    ).stream().map(Text::toPlain).collect(Collectors.toList()));
	    } catch (NucleusException e) {
		    e.printStackTrace();
	    }

	    return lb.log(base).build();
    }

	/**
	 * Updates the current log marker to that of the passed in ID plus 1.
	 *
	 * @param id The id of the latest log
	 */
	public static void setCurrent(int id) {
    	current = id + 1;
    }
}
