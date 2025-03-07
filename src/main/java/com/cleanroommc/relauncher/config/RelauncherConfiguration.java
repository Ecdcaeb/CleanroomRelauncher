package com.cleanroommc.relauncher.config;

import com.cleanroommc.relauncher.CleanroomRelauncher;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Properties;

public class RelauncherConfiguration {

    public static final File LOCATION = new File("cleanroom-relauncher.properties");

    public static RelauncherConfiguration read() {
        RelauncherConfiguration config = new RelauncherConfiguration();
        try (FileInputStream fis = new FileInputStream(LOCATION)) {
            Properties properties = new Properties();
            properties.load(fis);

            for (Field field : RelauncherConfiguration.class.getDeclaredFields()) {
                try {
                    field.set(config, properties.getProperty(field.getName(), null));
                } catch (IllegalAccessException e) {
                    CleanroomRelauncher.LOGGER.fatal("Could not set property {}", field.getName(), e);
                }
            }
        } catch (IOException e) {
            CleanroomRelauncher.LOGGER.fatal("Could not read configuration from file!", e);
            return null;
        }
        return config;
    }

    private String cleanroomVersion;
    private String javaExecutablePath;
    private String jvmArguments;

    public String getCleanroomVersion() {
        return cleanroomVersion;
    }

    public String getJavaExecutablePath() {
        return javaExecutablePath;
    }

    public String getJvmArguments() {
        return jvmArguments;
    }

    public void setCleanroomVersion(String cleanroomVersion) {
        this.cleanroomVersion = cleanroomVersion;
    }

    public void setJavaExecutablePath(String javaExecutablePath) {
        this.javaExecutablePath = javaExecutablePath;
    }

    public void setJvmArguments(String jvmArguments) {
        this.jvmArguments = jvmArguments;
    }

    public void save() {
        try (FileOutputStream fos = new FileOutputStream(LOCATION)) {
            Properties properties = new Properties();

            for (Field field : RelauncherConfiguration.class.getDeclaredFields()) {
                try {
                    properties.setProperty(field.getName(), field.get(this).toString());
                } catch (IllegalAccessException e) {
                    CleanroomRelauncher.LOGGER.fatal("Could not get property {}", field.getName(), e);
                }
            }

            properties.store(fos, "This is the relauncher properties file, it is first set up via a GUI, the GUI will never spawn after the first configuration unless this file is removed.");
        } catch (IOException e) {
            CleanroomRelauncher.LOGGER.fatal("Could not write configuration to file!", e);
        }
    }

}
