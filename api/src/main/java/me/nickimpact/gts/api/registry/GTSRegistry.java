package me.nickimpact.gts.api.registry;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.nickimpact.gts.api.extensions.Extension;
import me.nickimpact.gts.api.util.Builder;

import java.util.function.Supplier;

public interface GTSRegistry {

	<T> void register(Class<T> type, T value);

	<T> T get(Class<T> type);

	void registerExtension(Extension extension);

	ImmutableList<Extension> getLoadedExtensions();

	<T extends Builder> void registerBuilderSupplier(Class<T> type, Supplier<? extends T> builder);

	<T extends Builder> T createBuilder(Class<T> type);

	@Getter
	@AllArgsConstructor
	class Provider<T> {
		private T instance;
	}

}
