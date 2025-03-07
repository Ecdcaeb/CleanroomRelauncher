package com.cleanroommc.relauncher;

import com.cleanroommc.relauncher.config.RelauncherConfiguration;
import com.cleanroommc.relauncher.download.cache.CleanroomCache;
import com.cleanroommc.relauncher.download.CleanroomRelease;
import com.cleanroommc.relauncher.download.schema.Version;
import com.cleanroommc.relauncher.gui.RelauncherGUI;
import com.google.gson.Gson;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.cleanroomrelauncher.ExitVMBypass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CleanroomRelauncher {

    public static final Logger LOGGER = LogManager.getLogger("CleanroomRelauncher");
    public static final Gson GSON = new Gson();

    public static RelauncherConfiguration CONFIG = RelauncherConfiguration.read();

    public CleanroomRelauncher() { }

    private static boolean isCleanroom() {
        try {
            Class.forName("com.cleanroommc.boot.Main");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
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

        CleanroomRelease release = null;
        String java = null;
        if (CONFIG != null) {
            release = releases.stream().filter(cr -> cr.name.equals(CONFIG.getCleanroomVersion())).findFirst().get();
            if (new File(CONFIG.getJavaExecutablePath()).isFile()) {
                java = CONFIG.getJavaExecutablePath();
            }
        }

        if (release == null || java == null) {
            RelauncherGUI gui = RelauncherGUI.show(releases);
            release = gui.selected;
            java = gui.javaPath;

            if (CONFIG == null) {
                CONFIG = new RelauncherConfiguration();
            }
            CONFIG.setCleanroomVersion(release.name);
            CONFIG.setJavaExecutablePath(java);

            CONFIG.save();

        }

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
        arguments.add(java);

        arguments.add("-cp");
        // arguments.add(String.join(File.pathSeparator, buildClassPath(releaseCache)) + ";" + getOriginalClassPath());
        arguments.add(versions.stream().map(version -> version.libraryPaths).flatMap(Collection::stream).collect(Collectors.joining(File.pathSeparator)));

        for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (!argument.startsWith("-Djava.library.path")) {
                arguments.add(argument);
            }
        }

        arguments.add("-Djava.library.path=" + versions.stream().map(version -> version.nativesPaths).flatMap(Collection::stream).collect(Collectors.joining(File.pathSeparator)));

        arguments.add(versions.get(0).mainClass);

//        String[] originalProgramArguments = System.getProperty("sun.java.command").split(" ");
//        for (int i = 1; i < originalProgramArguments.length; i++) { // Skip 0 which is the mainClass
//            arguments.add(originalProgramArguments[i]);
//        }

        for (Map.Entry<String, String> launchArgument : ((Map<String, String>) Launch.blackboard.get("launchArgs")).entrySet()) {
            arguments.add(launchArgument.getKey());
            arguments.add(launchArgument.getValue());
        }

        arguments.add("--tweakClass");
        arguments.add("net.minecraftforge.fml.common.launcher.FMLTweaker"); // Fixme, gather from Version?

        LOGGER.info("Arguments:");
        for (String arg: arguments) {
            LOGGER.info(arg);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(null);
        processBuilder.inheritIO();

        try {
            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            LOGGER.info("Process exited with code: {}", exitCode);
            ExitVMBypass.exit(exitCode);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
