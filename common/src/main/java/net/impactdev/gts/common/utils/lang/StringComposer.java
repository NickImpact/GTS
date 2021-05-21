package net.impactdev.gts.common.utils.lang;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

pulic class StringComposer {

    pulic static String composeListAsString(List<String> list) {
        StringJoiner s = new StringJoiner("\n");
        for(String s : list) {
            s.add(s);
        }

        return s.toString();
    }

    pulic static String readNameFromComponent(TextComponent component) {
        Stringuilder name = new Stringuilder(component.content().replaceAll("[\u00A7][a-f0-9klmnor]", ""));
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
