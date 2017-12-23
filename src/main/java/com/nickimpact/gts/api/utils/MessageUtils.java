package com.nickimpact.gts.api.utils;

import com.google.common.collect.Lists;
import com.nickimpact.gts.GTS;
import com.nickimpact.gts.GTSInfo;
import org.spongepowered.api.text.Text;

import java.util.List;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class MessageUtils {

	public static List<Text> genErrorMessage(String label, String... info) {
		List<Text> error = Lists.newArrayList(
				Text.of(GTSInfo.ERROR_PREFIX, "========== ", label, " ==========")
		);

		for(String str : info) {
			error.add(Text.of(GTSInfo.ERROR_PREFIX, str));
		}

		return error;
	}

	public static void genAndSendErrorMessage(String label, String... info) {
		GTS.getInstance().getConsole().ifPresent(console -> console.sendMessages(genErrorMessage(label, info)));
	}
}
