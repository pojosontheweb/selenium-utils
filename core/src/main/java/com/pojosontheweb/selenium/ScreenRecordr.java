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
public class ScreenRecordr implements VideoRecordr {
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
        if (videoUuid == null) {
            return;
        }
        String videoPath = recorder.stop();
        Findr.logDebug("[ScreenRecordr] stopped video recording. Video path = " + videoPath);
    }

    @Override
    public String getVideoFileExt() {
        return ".mov";
    }

    public List<File> getVideoFiles() {
        File[] files = recorderConfiguration.getVideoDirectory().listFiles();
        if (files == null) {
            return null;
        }
        return Arrays.asList(files);
    }

}
