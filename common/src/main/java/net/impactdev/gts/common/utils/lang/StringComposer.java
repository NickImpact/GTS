package net.impactdev.gts.common.utils.lang;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class StringComposer {

    public static String composeListAsString(List<String> list) {
        StringJoiner sb = new StringJoiner("\n");
        for(String s : list) {
            sb.add(s);
        }

        return sb.toString();
    }

    public static String readNameFromComponent(TextComponent component) {
        StringBuilder name = new StringBuilder(component.content().replaceAll("[\u00A7][a-f0-9klmnor]", ""));
        List<TextComponent> children = component.children()
                .stream()
                .filter(c -> c instanceof TextComponent)
                .map(c -> (TextComponent) c)
                .map(c -> c.style(Style.empty()))
                .collect(Collectors.toList());

        for(TextComponent c : children) {
            name.append(component.content().replaceAll("[\u00A7][a-f0-9klmnor]", ""));
            name.append(readNameFromComponent(c));
        }

        return name.toString();
    }

}
