package net.impactdev.gts.ui.templates.configs;

import com.google.common.collect.Lists;
import net.impactdev.gts.ui.icons.IconTemplate;
import net.impactdev.impactor.api.configuration.adapter.ConfigurationAdapter;
import net.impactdev.impactor.api.configuration.key.ConfigKey;
import net.impactdev.impactor.api.ui.containers.views.pagination.updaters.PageUpdaterType;
import org.spongepowered.math.vector.Vector2i;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.impactdev.impactor.api.configuration.key.ConfigKeyFactory.key;
import static net.impactdev.impactor.api.configuration.key.ConfigKeyFactory.stringKey;

public class ListingViewConfigKeys {

    public static final ConfigKey<String> TITLE = stringKey("title", "");
    public static final ConfigKey<List<IconTemplate>> BACKGROUND = key(
            adapter -> adapter.getNodeList("background")
                    .stream()
                    .map(IconTemplate::fromConfiguration)
                    .collect(Collectors.toList())
    );

    public static final ConfigKey<PaginationSection> LISTINGS = key(adapter -> PaginationSection.fromConfiguration("listings", adapter));
    public static final ConfigKey<PaginationSection> FILTERS = key(adapter -> PaginationSection.fromConfiguration("filters", adapter));

    public static final class PaginationSection {
        private final Vector2i area;
        private final Vector2i offset;

        private final Map<PageUpdaterType, IconTemplate> updaters;

        private PaginationSection(Vector2i area, Vector2i offset, Map<PageUpdaterType, IconTemplate> updaters) {
            this.area = area;
            this.offset = offset;
            this.updaters = updaters;
        }

        public Vector2i area() {
            return area;
        }

        public Vector2i offset() {
            return offset;
        }

        public Map<PageUpdaterType, IconTemplate> updaters() {
            return updaters;
        }

        public static PaginationSection fromConfiguration(String root, ConfigurationAdapter adapter) {
            Vector2i area = Vector2i.from(adapter.getInteger(root + ".area.columns", 9), adapter.getInteger(root + ".area.rows", 6));
            Vector2i offset = Vector2i.from(adapter.getInteger(root + ".offset.x", 0), adapter.getInteger(root + ".offset.y", 0));

            Map<PageUpdaterType, IconTemplate> updaters = new HashMap<>();
            for(String key : adapter.getKeys(root + ".updaters", Lists.newArrayList())) {
                Optional<PageUpdaterType> type = Arrays.stream(PageUpdaterType.values())
                        .filter(value -> value.name().equalsIgnoreCase(key))
                        .findFirst();

                type.ifPresent(pageUpdaterType -> updaters.put(
                        pageUpdaterType,
                        IconTemplate.fromConfiguration(adapter.getNode(root + ".updaters." + key))
                ));
            }

            return new PaginationSection(area, offset, updaters);
        }
    }
}
