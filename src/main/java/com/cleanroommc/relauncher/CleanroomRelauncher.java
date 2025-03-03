package com.cleanroommc.relauncher;

import com.cleanroommc.relauncher.download.cache.CleanroomCache;
import com.cleanroommc.relauncher.download.CleanroomRelease;
import com.cleanroommc.relauncher.download.schema.Version;
import com.cleanroommc.relauncher.gui.RelauncherGUI;
import com.google.gson.Gson;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CleanroomRelauncher implements IFMLLoadingPlugin {

    public static final Logger LOGGER;
    public static final Gson GSON;

    static {
        LOGGER = LogManager.getLogger("CleanroomRelauncher");
        GSON = new Gson();

        if (!isCleanroom()) {
            run();
        }
    }

    private static boolean isCleanroom()
    {
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

    private static void run() {
        List<CleanroomRelease> releases;
        try {
            releases = CleanroomRelease.queryAll();
        } catch (IOException e) {
            LOGGER.throwing(e);
            LOGGER.info("Relaunch task aborted.");
            return;
        }

        LOGGER.info("{} cleanroom releases found.", releases.size());

        RelauncherGUI gui = RelauncherGUI.show(releases);
        CleanroomRelease candidateRelease = gui.selected;
        String javaPath = gui.javaPath;
        if (candidateRelease == null) {
            LOGGER.info("Didn't select a cleanroom version to relaunch.");
            LOGGER.info("Relaunch task aborted.");
            return;
        }
        CleanroomCache releaseCache = CleanroomCache.of(candidateRelease);

        Version version;
        try {
            version = releaseCache.download(); // Blocking
        } catch (IOException e) {
            LOGGER.throwing(e);
            LOGGER.info("Relaunch task aborted.");
            return;
        }

        LOGGER.info("Relaunching " + version.id);

        List<String> currentArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        LOGGER.info("Current arguments:");
        for (String arg: currentArgs)
            LOGGER.info(arg);

        File currentDir = (new File("")).getAbsoluteFile();

        List<File> libraryFiles = findJarFiles(releaseCache.getLibrariesDirectory().toFile());
        StringBuilder classPath = new StringBuilder();
        for (File lib : libraryFiles) {
            classPath.append(lib.getAbsolutePath()).append(";");
        }
        classPath.append(releaseCache.getUniversalJar().toString());

        List<String> args = new ArrayList<>();
        args.add(javaPath);
        args.addAll(currentArgs);
        args.add("-cp");
        args.add(classPath.toString());
        args.add(version.mainClass);
        args.add(version.minecraftArguments);

        LOGGER.info("Arguments:");
        for (String arg: args) {
            LOGGER.info(arg);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.directory(currentDir);

        try {
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.info("[Output]: " + line);
            }
            while ((line = errorReader.readLine()) != null) {
                LOGGER.info("[Error]: " + line);
            }

            int exitCode = process.waitFor();
            LOGGER.info("Process exited with code: " + exitCode);
            System.exit(0);
        } catch (IOException | InterruptedException e) {
            LOGGER.throwing(e);
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) { }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}
