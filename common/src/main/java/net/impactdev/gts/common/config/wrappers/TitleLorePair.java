package net.impactdev.gts.common.config.wrappers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class TitleLorePair {

	private final String title;
	private final List<String> lore;

}
