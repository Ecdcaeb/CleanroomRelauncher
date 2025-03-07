package com.cleanroommc.relauncher.download;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;

public final class GlobalDownloader {

    public static final GlobalDownloader INSTANCE = new GlobalDownloader();

    private final List<ForkJoinTask> downloads = new ArrayList<>();

    public void from(String source, File destination) {
        URL url;
        try {
            url = URI.create(source).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(String.format("Unable to construct url %s", source), e);
        }
        this.downloads.add(ForkJoinPool.commonPool().submit(() -> {
            try {
                FileUtils.copyURLToFile(url, destination);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Unable to download %s to %s", url, destination), e);
            }
        }));
    }

    public void immediatelyFrom(String source, File destination) {
        this.from(source, destination);
        try {
            this.downloads.remove(this.downloads.size() - 1).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Unable to complete download", e);
        }
    }

    public void blockUntilFinished() {
        for (Future download : this.downloads) {
            try {
                download.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Unable to complete download", e);
            }
        }
    }

}
