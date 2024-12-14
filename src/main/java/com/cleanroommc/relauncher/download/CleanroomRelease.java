package com.cleanroommc.relauncher.download;

import com.cleanroommc.relauncher.CleanroomRelauncher;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class CleanroomRelease {

    public static List<CleanroomRelease> queryAll() throws IOException {
        try {
            URL url = new URL("https://api.github.com/repos/CleanroomMC/Cleanroom/releases");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

            if (connection.getResponseCode() != 200) {
                throw new IOException("Failed to fetch releases: HTTP error code " + connection.getResponseCode());
            }

            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                return CleanroomRelauncher.GSON.fromJson(reader, new TypeToken<List<CleanroomRelease>>(){ }.getType());
            }
        } catch (Exception e) {
            throw new IOException("Failed to fetch or parse releases", e);
        }
    }

    public String name;
    @SerializedName("tag_name")
    public String tagName;
    public List<Asset> assets;

    public Asset getInstallerArtifact() {
        for (Asset asset : this.assets) {
            if (asset.name.endsWith("-installer.jar")) {
                return asset;
            }
        }
        return null;
    }

    public static class Asset {

        public String name;
        @SerializedName("browser_download_url")
        public String downloadUrl;
        public long size;

    }

}
