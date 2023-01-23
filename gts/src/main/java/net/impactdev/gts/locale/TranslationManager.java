package net.impactdev.gts.locale;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.impactdev.gts.configuration.GTSConfigKeys;
import net.impactdev.gts.locale.translations.Translation;
import net.impactdev.gts.locale.translations.TranslationSet;
import net.impactdev.gts.plugin.GTSPlugin;
import net.impactdev.impactor.api.text.TextProcessor;
import net.impactdev.impactor.api.utility.ExceptionPrinter;
import net.impactdev.impactor.api.utility.printing.PrettyPrinter;
import net.kyori.adventure.translation.Translator;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TranslationManager {

    private static final Locale DEFAULT_LOCALE = Locale.US;

    private final Path directory;
    private final TextProcessor processor;
    private final Set<Locale> installed = ConcurrentHashMap.newKeySet();
    private final Cache<Locale, TranslationSet> translations = Caffeine.newBuilder().build();

    public TranslationManager() {
        this.directory = GTSPlugin.instance().configurationDirectory().resolve("translations");
        this.processor = GTSPlugin.instance().configuration().get(GTSConfigKeys.TEXT_PROCESSOR);

        try {
            if(Files.notExists(this.getRepositoryDirectory())) {
                Files.createDirectories(this.getRepositoryDirectory());
            }

            if(Files.notExists(this.getCustomDirectory())) {
                Files.createDirectories(this.getCustomDirectory());
            }
        } catch (Exception ignored) {}
    }

    public void reload() {
        this.translations.invalidateAll();
        this.installed.clear();

        // Load official translations first, allowing for custom translations to overwrite
        this.loadFromFileSystem(this.getRepositoryDirectory());
        this.loadFromFileSystem(this.getCustomDirectory());
    }

    public TranslationSet fetchFromLocaleOrDefault(Locale locale) {
        return this.translations.get(
                locale,
                key -> Optional.ofNullable(this.translations.getIfPresent(DEFAULT_LOCALE))
                        .orElseThrow(() -> new IllegalStateException("Fallback locale could not be located"))
        );
    }

    public TextProcessor processor() {
        return this.processor;
    }

    public Path getRepositoryDirectory() {
        return this.directory.resolve("repository");
    }

    public Path getCustomDirectory() {
        return this.directory.resolve("custom");
    }

    public Path getRepositoryStatusFile() {
        return this.getRepositoryDirectory().resolve("status.json");
    }

    public Set<Locale> getInstalledLocales() {
        return Collections.unmodifiableSet(this.installed);
    }

    private void loadFromFileSystem(Path directory) {
        try(Stream<Path> stream = Files.list(directory)) {
            List<Path> translations = stream.filter(TranslationManager::isConfigurationFile).collect(Collectors.toList());
            for(Path translation : translations) {
                try {
                    Map.Entry<Locale, TranslationSet> result = this.loadTranslationFile(translation);
                    this.translations.put(result.getKey(), result.getValue());
                } catch (Exception e) {
                    GTSPlugin.instance().logger().warn("Error loading locale file: " + translation.getFileName());
                }
            }
        } catch (Exception e) {
            GTSPlugin.instance().logger().severe("Exception occurred loading translations...");
            ExceptionPrinter.print(GTSPlugin.instance().logger(), e);
        }

        this.translations.asMap().forEach((locale, config) -> {
            Locale noCountry = new Locale(locale.getLanguage());
            if(!locale.equals(noCountry) && this.installed.add(noCountry)) {
                this.translations.put(noCountry, config);
            }
        });
    }

    static boolean isConfigurationFile(Path path) {
        return path.getFileName().toString().endsWith(".conf");
    }

    private Map.Entry<Locale, TranslationSet> loadTranslationFile(Path target) throws Exception {
        String name = target.getFileName().toString();
        String localeString = name.substring(0, name.length() - ".conf".length());
        Locale locale = parseLocale(localeString);
        if(locale == null) {
            throw new IllegalStateException("Unknown locale '" + localeString + "' - skipping registration");
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject json = gson.fromJson(new FileReader(target.toFile()), JsonObject.class);

        this.installed.add(locale);
        return Maps.immutableEntry(locale, TranslationSet.fromJson(json));
    }

    static Locale parseLocale(String input) {
        return Translator.parseLocale(input);
    }

}
