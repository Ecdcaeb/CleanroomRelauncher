package com.cleanroommc.relauncher;

import com.cleanroommc.relauncher.download.cache.CleanroomCache;
import com.cleanroommc.relauncher.download.CleanroomRelease;
import com.cleanroommc.relauncher.gui.RelauncherGUI;
import com.google.gson.Gson;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CleanroomRelauncher implements IFMLLoadingPlugin {

    public static final Logger LOGGER;
    public static final Gson GSON;

    static {
        LOGGER = LogManager.getLogger("CleanroomRelauncher");
        GSON = new Gson();

        run();
    }

    private static void run() {
        try {
            List<CleanroomRelease> releases = CleanroomRelease.queryAll();
            LOGGER.info("{} releases found.", releases.size());

            CleanroomRelease candidateRelease = RelauncherGUI.show(releases);
            CleanroomCache releaseCache = CleanroomCache.of(candidateRelease);

            releaseCache.download(); // Blocking
        } catch (IOException e) {
            throw new RuntimeException(e);
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
