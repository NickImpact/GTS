package me.nickimpact.gts.sponge.listings.makeup;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.nickimpact.gts.api.listings.entries.Entry;

@Getter
@AllArgsConstructor
public abstract class SpongeEntry<X, Y> implements Entry<X, Y> {

	private SpongeDisplay display;

}
