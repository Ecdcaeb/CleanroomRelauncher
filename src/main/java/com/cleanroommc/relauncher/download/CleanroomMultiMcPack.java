package com.cleanroommc.relauncher.download;

import com.cleanroommc.relauncher.download.cache.CleanroomCache;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Deprecated
public class CleanroomMultiMcPack implements CleanroomZipArtifact {

    public static CleanroomMultiMcPack of(String version, Path location) {
        return new CleanroomMultiMcPack(version, location);
    }

    private final String version;
    private final Path location;

    private CleanroomMultiMcPack(String version, Path location) {
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
            Files.copy(jar.getPath("/patches/net.minecraft.json"), cache.getMinecraftJson());
            Files.copy(jar.getPath("/patches/net.minecraftforge.json"), cache.getForgeJson());
            Files.copy(jar.getPath("/patches/org.lwjgl3.json"), cache.getLwjglVersionJson());
            try (Stream<Path> stream = Files.walk(jar.getPath("/libraries/"))) {
                Files.copy(stream.filter(Files::isRegularFile).findFirst().get(), cache.getUniversalJar());
            }
        }
    }

}
