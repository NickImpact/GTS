package net.impactdev.gts.util;

import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

public class GTSKeys {

    public static Key gts(@Subst("gts") final @NotNull @Pattern("[a-z0-9_\\-./]+") String value) {
        return Key.key("gts", value);
    }

}
