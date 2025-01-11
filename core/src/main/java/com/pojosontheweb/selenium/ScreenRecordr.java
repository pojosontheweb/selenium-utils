package com.pojosontheweb.selenium;

import com.github.agomezmoron.multimedia.recorder.VideoRecorder;
import com.github.agomezmoron.multimedia.recorder.configuration.VideoRecorderConfiguration;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Records the screen to a .mov file.
 */
public class ScreenRecordr {

    private File tmpDir = null;
    private String videoUuid = null;

    public ScreenRecordr() {
        String tmpFullPath = System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID();
        tmpDir = new File(tmpFullPath);
        tmpDir.mkdirs();
        Findr.logDebug("[ScreenRecordr] screen recorder created, tmpDir = " + tmpFullPath);
    }

    public ScreenRecordr start() {
        VideoRecorderConfiguration.setCaptureInterval(50); // 20 frames/sec
        VideoRecorderConfiguration.wantToUseFullScreen(true);
        VideoRecorderConfiguration.setVideoDirectory(tmpDir); // home
        VideoRecorderConfiguration.setKeepFrames(false);
        // you can also change the x,y using VideoRecorderConfiguration.setCoordinates(10,20);
        UUID uuid = UUID.randomUUID();
        videoUuid = uuid.toString();
        VideoRecorder.start(videoUuid);
        return this;
    }

    public void stop() {
        if (videoUuid==null) {
            return;
        }
        String videoPath;
        try {
            videoPath = VideoRecorder.stop();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Findr.logDebug("[ScreenRecordr] stopped video recording. Video path = " + videoPath);
        videoUuid = null;
    }

    public List<File> getVideoFiles() {
        if (tmpDir == null) {
            return Collections.emptyList();
        }
        File[] files = tmpDir.listFiles();
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
