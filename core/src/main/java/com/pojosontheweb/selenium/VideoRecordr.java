package com.pojosontheweb.selenium;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.io.Files;

public abstract class VideoRecordr {

    protected abstract void setCaptureDelay(int captureDelay);

    public abstract VideoRecordr start();

    protected abstract void stop(boolean createVideo);

    protected abstract List<File> getVideoFiles();

    protected abstract String getVideoFileExt();

    public VideoRecordr moveVideoFilesTo(File destDir, String filePrefix) {
        stop(true);
        List<File> files = getVideoFiles();
        Findr.logDebug("[VideoRecordr] moving " + files.size() + " video files to " + destDir +
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
                    Findr.logDebug("[VideoRecordr] " + f.getAbsolutePath() + " => " + vidFile.getAbsolutePath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    f.delete();
                }
            }
        }
        return this;
    }

    public VideoRecordr removeVideoFiles() {
        Findr.logDebug("[VideoRecordr] removing video files");
        stop(false);
        getVideoFiles().forEach(File::delete);
        return this;
    }

}
