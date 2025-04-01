package com.cleanroommc.relauncher.config;

import com.cleanroommc.relauncher.CleanroomRelauncher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.launchwrapper.Launch;

import java.io.*;

public class RelauncherConfiguration {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final File FILE = new File(Launch.minecraftHome, "config/relauncher.json");

    static {
        File oldConfig = new File(Launch.minecraftHome, "cleanroom-relauncher-v1.properties");
        if (oldConfig.exists()) {
            oldConfig.delete();
        }
    }

    public static RelauncherConfiguration read() {
        if (!FILE.exists()) {
            return new RelauncherConfiguration();
        }
        try (FileReader reader = new FileReader(FILE)) {
            return GSON.fromJson(reader, RelauncherConfiguration.class);
        } catch (IOException e) {
            CleanroomRelauncher.LOGGER.error("Unable to read config", e);
            return new RelauncherConfiguration();
        }
    }

    private String cleanroomVersion;
    private String javaExecutablePath;
    private String javaArguments;

    public String getCleanroomVersion() {
        return cleanroomVersion;
    }

    public String getJavaExecutablePath() {
        return javaExecutablePath;
    }

    public String getJavaArguments() {
        return javaArguments;
    }

    public void setCleanroomVersion(String cleanroomVersion) {
        this.cleanroomVersion = cleanroomVersion;
    }

    public void setJavaExecutablePath(String javaExecutablePath) {
        this.javaExecutablePath = javaExecutablePath.replace("\\\\", "/");
    }

    public void setJavaArguments(String javaArguments) {
        this.javaArguments = javaArguments;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            CleanroomRelauncher.LOGGER.error("Unable to save config", e);
        }
    }

}
