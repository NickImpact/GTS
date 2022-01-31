/* This design originates from Nucleus, which is licensed under MIT */
package net.impactdev.gts.gradle.tasks;

import net.impactdev.gts.gradle.enums.ReleaseLevel;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReleaseNotesTask extends DefaultTask {

    public String result = "No templated release notes available";

    public String version;
    public String hash;
    public String sponge;
    public String message;
    public String sinceLastRelease;
    public ReleaseLevel level;

    public String getVersion() {
        return this.version;
    }

    public String getHash() {
        return this.hash;
    }

    private String getShortHash() {
        return this.hash.substring(0, 7);
    }

    public String getSpongeVersion() {
        return this.sponge;
    }

    public String getMessage() {
        return this.message;
    }

    public ReleaseLevel getLevel() {
        return this.level;
    }

    public String getSinceLastRelease() {
        return this.sinceLastRelease;
    }

    @TaskAction
    public void run() {
        Path template = this.getProject().getRootDir().toPath()
                .resolve("changelogs")
                .resolve("templates")
                .resolve(this.getLevel().getTemplate() + ".md");


        if(Files.exists(template)) {
            try {
                String result = this.read(Files.newBufferedReader(template));
                this.result = Placeholders.transform(this, result);
            } catch (Exception e) {
                throw new GradleException(e.getMessage());
            }
        }

    }

    private Optional<String> getChangesForVersion() {
        Path notes = this.getProject().getRootDir().toPath()
                .resolve("changelogs")
                .resolve(this.getVersion() + ".md");

        if(Files.exists(notes)) {
            try {
                return Optional.of(this.read(Files.newBufferedReader(notes, StandardCharsets.UTF_8)));
            } catch (Exception e) {
                e.printStackTrace();
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private String read(BufferedReader reader) throws IOException {
        StringBuilder result = new StringBuilder();
        String working;
        while((working = reader.readLine()) != null) {
            result.append(working).append('\n');
        }
        return result.toString();
    }

    private enum Placeholders {

        VERSION("version", ReleaseNotesTask::getVersion),
        HASH("commit-hash", ReleaseNotesTask::getHash),
        SHORT_HASH("commit-hash-short", ReleaseNotesTask::getShortHash),
        MESSAGE("commit-message", ReleaseNotesTask::getMessage),
        SPONGE("sponge", ReleaseNotesTask::getSpongeVersion),
        CHANGES("changes", task -> {
            Optional<String> result = task.getChangesForVersion();
            return result.orElse("No release notes available for this version...");
        }),
        SINCE_LAST_RELEASE("since-last-release", task -> {
            Pattern pattern = Pattern.compile("(?<commit>[0-9a-z]+)( [(].+[)])? (?<message>.+)");
            Matcher matcher = pattern.matcher(task.getSinceLastRelease());
            StringJoiner joiner = new StringJoiner("\n");
            while(matcher.find()) {
                String commit = matcher.group("commit");
                commit = "[" + commit + "](https://github.com/NickImpact/GTS/commit/" + commit + ")";

                joiner.add(commit + " " + matcher.group("message") + "  ");
            }

            return joiner.toString();
        })
        ;

        private final String key;
        private final Function<ReleaseNotesTask, String> replacement;

        Placeholders(String key, Function<ReleaseNotesTask, String> replacement) {
            this.key = key;
            this.replacement = replacement;
        }

        public String getKey() {
            return this.key;
        }

        public Function<ReleaseNotesTask, String> getReplacement() {
            return this.replacement;
        }

        public static String transform(ReleaseNotesTask task, String in) {
            String out = in;
            for(Placeholders placeholder : Placeholders.values()) {
                out = out.replace("{{" + placeholder.getKey() + "}}", placeholder.getReplacement().apply(task));
            }

            return out;
        }
    }

}
