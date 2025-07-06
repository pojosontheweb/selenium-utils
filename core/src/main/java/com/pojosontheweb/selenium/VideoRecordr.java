package com.pojosontheweb.selenium;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.io.Files;

public interface VideoRecordr {

    VideoRecordr start();

    void stop();

    List<File> getVideoFiles();

    String getVideoFileExt();

    default VideoRecordr moveVideoFilesTo(File destDir, String filePrefix) {
        return defaultMoveVideoFilesTo(destDir, filePrefix);
    }

    default VideoRecordr removeVideoFiles() {
        return defaultRemoveVideoFiles();
    }

    default VideoRecordr defaultMoveVideoFilesTo(File destDir, String filePrefix) {
        stop();
        List<File> files = getVideoFiles();
        Findr.logDebug("[ScreenRecordr] moving " + files.size() + " video files to " + destDir +
                " with filePrefix=" + filePrefix);
        int totalCount = 1;
        boolean needsCount = files.size() > 1;
        for (File f : files) {
            if (f.exists()) {
                String fileName = filePrefix;
                if (needsCount) {
                    fileName += "-" + totalCount++;
                }
                fileName += getVideoFileExt();
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }
                File vidFile = new File(destDir, fileName);
                try {
                    Files.copy(f, vidFile);
                    Findr.logDebug("[ScreenRecordr] " + f.getAbsolutePath() + " => " + vidFile.getAbsolutePath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    f.delete();
                }
            }
        }
        return this;
    }

    default VideoRecordr defaultRemoveVideoFiles() {
        Findr.logDebug("[ScreenRecordr] removing video files");
        stop();
        List<File> files = getVideoFiles();
        for (File f : files) {
            f.delete();
        }
        return this;
    }

}
