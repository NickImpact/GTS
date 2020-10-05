package net.impactdev.gts.common.utils.lang;

import java.util.List;
import java.util.StringJoiner;

public class StringComposer {

    public static String composeListAsString(List<String> list) {
        StringJoiner sb = new StringJoiner("\n");
        for(String s : list) {
            sb.add(s);
        }

        return sb.toString();
    }

}
