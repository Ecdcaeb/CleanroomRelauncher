package com.cleanroommc.relauncher.download;

import com.cleanroommc.relauncher.download.cache.CleanroomCache;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class CleanroomInstaller implements CleanroomZipArtifact {

    public static CleanroomInstaller of(String version, Path location) {
        return new CleanroomInstaller(version, location);
    }

    private final String version;
    private final Path location;

    private CleanroomInstaller(String version, Path location) {
        this.version = version;
        this.location = location;
    }

    @Override
    public void install(String url) throws IOException {
        if (!Files.exists(this.location)) {
            FileUtils.copyURLToFile(new URL(url), this.location.toFile());
        }
    }

    @Override
    public void extract(CleanroomCache cache) throws IOException {
        try (FileSystem jar = FileSystems.newFileSystem(this.location, null)) {
            Files.copy(jar.getPath("/version.json"), cache.getVersionJson());
            Files.copy(jar.getPath("/maven/com/cleanroommc/cleanroom/" + this.version + "/cleanroom-" + this.version + ".jar"), cache.getUniversalJar());
        }
    }

}
