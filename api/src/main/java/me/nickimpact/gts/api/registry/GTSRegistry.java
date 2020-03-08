package me.nickimpact.gts.api.registry;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.nickimpact.gts.api.extensions.Extension;
import me.nickimpact.gts.api.util.Builder;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class GTSRegistry {

	private static List<Extension> extensions = Lists.newArrayList();
	private static Map<Class<?>, Supplier<?>> builders = Maps.newHashMap();

	/**
	 * Registers an Extension to GTS.
	 *
	 * @param extension The extension to register
	 */
	public static void registerExtension(Extension extension) {
		extensions.add(extension);
	}

	/**
	 * Returns a list of extensions that are loaded into GTS. This list should not be modifiable.
	 *
	 * @return An immutable list made up of loaded extensions
	 */
	public static ImmutableList<Extension> getLoadedExtensions() {
		return ImmutableList.copyOf(extensions);
	}

	public static <T extends Builder> void registerBuilderSupplier(Class<T> type, Supplier<? extends T> builder) {
		Preconditions.checkArgument(builders.containsKey(type), "Already registered a builder supplier for: " + type.getCanonicalName());
		builders.put(type, builder);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Builder> T createBuilder(Class<T> type) {
		Preconditions.checkNotNull(type, "Input builder type was null");
		final Supplier<?> supplier = builders.get(type);
		Preconditions.checkNotNull(supplier, "Could not find a Supplier for the provided builder type: " + type.getCanonicalName());
		return (T) supplier.get();
	}
}
