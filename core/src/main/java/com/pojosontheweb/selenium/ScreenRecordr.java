package com.pojosontheweb.selenium;

import com.github.agomezmoron.multimedia.recorder.VideoRecorder;
import com.github.agomezmoron.multimedia.recorder.configuration.VideoRecorderConfiguration;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Records the screen to a .mov file.
 */
public class ScreenRecordr {
    private String videoUuid = null;

    private VideoRecorderConfiguration recorderConfiguration = new VideoRecorderConfiguration();

    private VideoRecorder recorder;

    public ScreenRecordr() {
        Findr.logDebug("[ScreenRecordr] screen recorder created");
    }

    public void setRecorderConfiguration(VideoRecorderConfiguration recorderConfiguration) {
        this.recorderConfiguration = recorderConfiguration;
    }

    public ScreenRecordr start() {
        if (recorder != null) {
            recorder.stop();
        }
        UUID uuid = UUID.randomUUID();
        videoUuid = uuid.toString();
        recorder = new VideoRecorder(recorderConfiguration);
        recorder.start(videoUuid);
        return this;
    }

    public void stop() {
        if (recorder == null) {
            return;
        }
        if (videoUuid==null) {
            return;
        }
        String videoPath = recorder.stop();
        Findr.logDebug("[ScreenRecordr] stopped video recording. Video path = " + videoPath);
    }

    public List<File> getVideoFiles() {
        File[] files = recorderConfiguration.getVideoDirectory().listFiles();
        if (files == null) {
            return null;
        }
        return Arrays.asList(files);
    }

    public ScreenRecordr moveVideoFilesTo(File destDir, String filePrefix) {
        stop();
        List<File> files = getVideoFiles();
        Findr.logDebug("[ScreenRecordr] moving " + files.size() + " video files to " + destDir +
                " with filePrefix=" + filePrefix);
        int totalCount = 1;
        boolean needsCount = files.size()>1;
        for (File f : files) {
            if (f.exists()) {
                String fileName = filePrefix;
                if (needsCount) {
                    fileName += "-" + totalCount++;
                }
                fileName += ".mov";
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }
                File vidFile = new File(destDir, fileName);
                try {
                    Files.copy(f, vidFile);
                    Findr.logDebug("[ScreenRecordr] " + f.getAbsolutePath() + " => " + vidFile.getAbsolutePath());
                } catch(IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    f.delete();
                }
            }
        }
        return this;
    }


    public ScreenRecordr removeVideoFiles() {
        Findr.logDebug("[ScreenRecordr] removing video files");
        stop();
        List<File> files = getVideoFiles();
        for (File f : files) {
            f.delete();
        }
        return this;
    }
}
