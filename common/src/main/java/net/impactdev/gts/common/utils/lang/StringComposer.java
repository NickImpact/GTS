package net.impactdev.gts.common.utils.lang;

import net.kyori.text.TextComponent;

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
        StringBuilder name = new StringBuilder(component.content());
        List<TextComponent> children = component.children()
                .stream()
                .filter(c -> c instanceof TextComponent)
                .map(c -> (TextComponent) c)
                .collect(Collectors.toList());

        for(TextComponent c : children) {
            name.append(component.content());
            name.append(readNameFromComponent(c));
        }

        return name.toString();
    }

}
