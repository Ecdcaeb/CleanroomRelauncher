package com.cleanroommc.relauncher;

import com.cleanroommc.relauncher.download.cache.CleanroomCache;
import com.cleanroommc.relauncher.download.CleanroomRelease;
import com.cleanroommc.relauncher.download.schema.Version;
import com.cleanroommc.relauncher.gui.RelauncherGUI;
import com.google.gson.Gson;
import net.minecraftforge.fml.cleanroomrelauncher.ExitVMBypass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CleanroomRelauncher {

    public static final Logger LOGGER = LogManager.getLogger("CleanroomRelauncher");
    public static final Gson GSON = new Gson();

    public CleanroomRelauncher() { }

    private static boolean isCleanroom() {
        try {
            Class.forName("com.cleanroommc.boot.Main");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static List<File> findJarFiles(File dir) {
        List<File> jarFiles = new ArrayList<>();
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        jarFiles.addAll(findJarFiles(file));
                    } else if (file.getName().endsWith(".jar")) {
                        jarFiles.add(file);
                    }
                }
            }
        }
        return jarFiles;
    }

    private static List<String> buildClassPath(CleanroomCache cache) {
        List<String> classPath = new ArrayList<>();

        classPath.add(cache.getUniversalJar().toAbsolutePath().toString());

        List<File> jars = findJarFiles(cache.getLibrariesDirectory().toFile());
        for (File jar : jars) {
            classPath.add(jar.getAbsolutePath());
        }

        return classPath;
    }

    private static String getOriginalClassPath() {
        return System.getProperty("java.class.path");
    }

    private static String libraryPath() {
        return System.getProperty("java.library.path");
    }

    void run() {
        if (isCleanroom()) {
            LOGGER.info("Cleanroom detected. No need to relaunch!");
            return;
        }

        List<CleanroomRelease> releases;
        try {
            releases = CleanroomRelease.queryAll();
        } catch (IOException e) {
            throw new RuntimeException("Unable to query Cleanroom's releases.", e);
        }

        LOGGER.info("{} cleanroom releases were queried.", releases.size());

        RelauncherGUI gui = RelauncherGUI.show(releases);
        CleanroomRelease release = gui.selected;
        String java = gui.javaPath;

        CleanroomCache releaseCache = CleanroomCache.of(release);

        LOGGER.info("Preparing Cleanroom v{} and its libraries...", release.name);
        List<Version> versions;
        try {
            versions = releaseCache.download(); // Blocking
        } catch (IOException e) {
            throw new RuntimeException("Unable to grab CleanroomVersion to relaunch.", e);
        }

        LOGGER.info("Preparing to relaunch Cleanroom v{}", release.name);

        List<String> arguments = new ArrayList<>();
        // arguments.add(java);
        arguments.add("X:\\Caches\\.gradle\\jdks\\azul_systems__inc_-21-amd64-windows\\zulu21.32.17-ca-jdk21.0.2-win_x64\\bin\\javaw.exe");
        arguments.add("-cp");
        // arguments.add(String.join(File.pathSeparator, buildClassPath(releaseCache)) + ";" + getOriginalClassPath());
        arguments.add(versions.stream().map(version -> version.libraryPaths).flatMap(Collection::stream).collect(Collectors.joining(File.pathSeparator)));
        arguments.add("-Djava.library.path=" + versions.stream().map(version -> version.nativesPaths).flatMap(Collection::stream).collect(Collectors.joining(File.pathSeparator)));
        arguments.add(versions.get(0).mainClass);
        // arguments.addAll(Arrays.asList(version.minecraftArguments.split(" ")));

        LOGGER.info("Arguments:");
        for (String arg: arguments) {
            LOGGER.info(arg);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(null);
        processBuilder.inheritIO();

        try {
            Process process = processBuilder.start();

            /*
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.info(line);
            }
            while ((line = errorReader.readLine()) != null) {
                LOGGER.error(line);
            }
             */

            int exitCode = process.waitFor();
            LOGGER.info("Process exited with code: {}", exitCode);
            ExitVMBypass.exit(exitCode);
        } catch (IOException | InterruptedException e) {
            LOGGER.throwing(e);
        }
    }


}
