package com.cleanroommc.relauncher.download.cache;

import com.cleanroommc.relauncher.download.CleanroomInstaller;
import com.cleanroommc.relauncher.download.CleanroomMultiMcPack;
import com.cleanroommc.relauncher.download.CleanroomRelease;
import com.cleanroommc.relauncher.download.schema.Version;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

    public List<Version> download() throws IOException {
        if (!Files.isDirectory(this.directory)) {
            Files.createDirectories(this.directory);
        }

        Path multiMcPackZip = this.getMultiMcPackZip();
        Path lwjglJson = this.getLwjglVersionJson();
        Path forgeJson = this.getForgeJson();
        Path minecraftJson = this.getMinecraftJson();

        // Path installerJar = this.getInstallerJar();
        Path universalJar = this.getUniversalJar();
        // Path versionJson = this.getVersionJson();

        Path librariesDirectory = this.getLibrariesDirectory();
        Path nativesDirectory = this.getNativesDirectory();

        CleanroomMultiMcPack multiMcPack = CleanroomMultiMcPack.of(this.version, this.getMultiMcPackZip());
        // CleanroomInstaller installer = CleanroomInstaller.of(this.version, installerJar);

        multiMcPack.install(this.release.getMultiMcPackArtifact().downloadUrl);

        if (!Files.exists(lwjglJson) || !Files.exists(forgeJson) || !Files.exists(minecraftJson) || !Files.exists(universalJar)) {
            multiMcPack.extract(this);
        }

        /*
        if (!Files.exists(universalJar) || !Files.exists(versionJson)) {
            installer.extract(this);
        }
         */

        List<Version> versions = new ArrayList<>();
        Version forgeJsonVersion = Version.parse(forgeJson);
        forgeJsonVersion.libraryPaths.add(this.getUniversalJar().toAbsolutePath().toString());
        versions.add(forgeJsonVersion);
        versions.add(Version.parse(lwjglJson));
        versions.add(Version.parse(minecraftJson));

        for (Version version : versions) {
            version.downloadLibraries(librariesDirectory);
            version.extractNatives(librariesDirectory, nativesDirectory);
        }

        /*
        Version version = Version.parse(versionJson);

        for (Version.Library library : version.libraries) {
            if (!library.isNative()) {
                Path libraryJar = librariesDirectory.resolve(library.downloads.artifact.path);
                if (!Files.exists(libraryJar)) {
                    FileUtils.copyURLToFile(new URL(library.downloads.artifact.url), libraryJar.toFile());
                }
            } else {

            }
        }
         */

        // return version;
        return versions;
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

    public Path getNativesDirectory() {
        return CACHE_DIR.resolve("natives/");
    }

    public Path getVersionJson() {
        return this.directory.resolve("version.json");
    }

    @Deprecated
    public Path getMultiMcPackZip() {
        return this.directory.resolve("mmc.zip");
    }

    @Deprecated
    public Path getLwjglVersionJson() {
        return this.directory.resolve("org.lwjgl3.json");
    }

    @Deprecated
    public Path getForgeJson() {
        return this.directory.resolve("net.minecraftforge.json");
    }

    @Deprecated
    public Path getMinecraftJson() {
        return this.directory.resolve("net.minecraft.json");
    }

}
