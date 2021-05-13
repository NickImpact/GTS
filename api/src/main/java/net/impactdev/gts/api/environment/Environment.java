package net.impactdev.gts.api.environment;

import com.google.common.collect.Maps;
import net.impactdev.gts.api.util.PrettyPrinter;

import java.util.Map;

public class Environment implements PrettyPrinter.IPrettyPrintable {

    private final Map<String, String> environment = Maps.newLinkedHashMap();

    public void append(String key, String version) {
        this.environment.put(key, version);
    }

    @Override
    public void print(PrettyPrinter printer) {
        for(Map.Entry<String, String> entry : this.environment.entrySet()) {
            printer.add("  * " + entry.getKey() + ": " + entry.getValue());
        }
    }

}
