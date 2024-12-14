package com.cleanroommc.relauncher.download.schema;

import com.cleanroommc.relauncher.CleanroomRelauncher;
import com.sun.jna.Platform;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Version {

    public static Version parse(Path path) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(path))) {
            return CleanroomRelauncher.GSON.fromJson(reader, Version.class);
        } catch (IOException e) {
            throw new IOException("Unable to parse version.json", e);
        }
    }

    public String id;
    public String releaseTime;
    // public String type;
    public String mainClass;
    public String minecraftArguments;
    public List<Library> libraries;

    public static class Downloads {

        public Artifact artifact;

    }

    public static class Artifact {

        public String path;
        public String url;
        public String sha1;
        public int size;

    }

    public static class Library {

        public String name;
        public Downloads downloads;
        public List<Rule> rules;

        // TODO exclusion?
        public boolean apply() {
            if (this.rules == null || this.rules.isEmpty()) {
                return true;
            }
            for (Rule rule : this.rules) {
                if ("allow".equals(rule.action) && !rule.os.isSame()) {
                    return false;
                }
            }
            return true;
        }

    }

    public static class Os {

        public String name;

        // TODO: Subject to change
        public boolean isSame() {
            switch (this.name) {
                case "windows":
                    return Platform.isWindows();
                case "linux":
                    return Platform.isLinux();
                case "osx":
                    return Platform.isMac();
            }
            return false;
        }

    }

    public static class Rule {

        public String action;
        public Os os;

    }

}
