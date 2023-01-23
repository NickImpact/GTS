package net.impactdev.gts.ui.icons.functions;

import com.google.common.collect.Maps;
import net.kyori.adventure.key.Key;

import java.util.Map;
import java.util.Optional;

public final class IconFunctionRegistry {

    private static final Map<Key, IconFunction> FUNCTIONS = Maps.newHashMap();

    static {
        IconFunctions.dummy();
    }

    public static Optional<IconFunction> locate(Key key) {
        return Optional.ofNullable(FUNCTIONS.get(key));
    }

    public static IconFunction register(Key key, IconFunction function) {
        FUNCTIONS.put(key, function);
        return function;
    }

}
