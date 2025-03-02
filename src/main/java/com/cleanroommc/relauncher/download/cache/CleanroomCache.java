package com.cleanroommc.relauncher.download.cache;

import com.cleanroommc.relauncher.download.CleanroomInstaller;
import com.cleanroommc.relauncher.download.CleanroomRelease;
import com.cleanroommc.relauncher.download.schema.Version;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CleanroomCache {

    private static final Path CACHE_DIR = Paths.get(System.getProperty("user.home"), ".cleanroom", "relauncher");

    public static CleanroomCache of(CleanroomRelease release) {
        return new CleanroomCache(release);
    }

    private final Path directory;
    private final CleanroomRelease release;
    private final String version;

    private CleanroomCache(CleanroomRelease release) {
        this.directory = CACHE_DIR.resolve(release.tagName);
        this.release = release;
        this.version = release.tagName;
    }

    public Version download() throws IOException {
        if (!Files.isDirectory(this.directory)) {
            Files.createDirectories(this.directory);
        }

        Path installerJar = this.getInstallerJar();
        Path universalJar = this.getUniversalJar();
        Path versionJson = this.getVersionJson();
        Path librariesDirectory = this.getLibrariesDirectory();

        CleanroomInstaller installer = CleanroomInstaller.of(this.version, installerJar);

        if (!Files.exists(installerJar)) {
            FileUtils.copyURLToFile(new URL(this.release.getInstallerArtifact().downloadUrl), installerJar.toFile());
        }
        if (!Files.exists(universalJar) || !Files.exists(versionJson)) {
            installer.extract(this);
        }

        Version version = Version.parse(versionJson);

        for (Version.Library library : version.libraries) {
            if (!library.downloads.artifact.url.isEmpty() && library.apply()) {
                Path libraryJar = librariesDirectory.resolve(library.downloads.artifact.path);
                if (!Files.exists(libraryJar)) {
                    FileUtils.copyURLToFile(new URL(library.downloads.artifact.url), libraryJar.toFile());
                }
            }
        }

        return version;
    }

    public Path getInstallerJar() {
        return this.directory.resolve("installer.jar");
    }

    public Path getUniversalJar() {
        return this.directory.resolve("universal.jar");
    }

    public Path getLibrariesDirectory() {
        return CACHE_DIR.resolve("libraries/");
    }

    public Path getVersionJson() {
        return this.directory.resolve("version.json");
    }

}
