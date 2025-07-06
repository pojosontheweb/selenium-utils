package com.pojosontheweb.selenium;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

public class SeleniumRecordr implements VideoRecordr {

    private static final String CAPTURE_PATTERN = "capture-%05d.png";

    private final TakesScreenshot takesScreenshot;
    private final int captureInterval = 10; // millis
    private final List<File> videos = new Vector<>();

    public SeleniumRecordr(TakesScreenshot takesScreenshot) {
        this.takesScreenshot = takesScreenshot;
    }

    private Path videoDir = null;
    private final List<File> pngs = new Vector<>();
    private Thread recordingThread = null;
    private long recordingAt;

    @Override
    public SeleniumRecordr start() {
        if (recordingThread != null) {
            stop();
        }
        if (recordingThread == null) {
            videos.clear();
            pngs.clear();
            recordingThread = createRecordingThread();
            recordingThread.start();
            recordingAt = System.currentTimeMillis();
        }
        return this;
    }

    @Override
    public void stop() {
        if (recordingThread != null) {
            if (recordingThread.isAlive()) {
                while (pngs.isEmpty()) {
                    try {
                        Thread.sleep(captureInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                recordingThread.interrupt();
            }
            var vid = createVideo(System.currentTimeMillis() - recordingAt);
            videos.add(vid);
            pngs.forEach(File::delete);
        }
    }

    @Override
    public String getVideoFileExt() {
        return ".mp4";
    }

    @Override
    public List<File> getVideoFiles() {
        return List.copyOf(videos);
    }

    private Thread createRecordingThread() {
        return new Thread() {
            @Override
            public void run() {
                try {
                    do {
                        File tmp = takesScreenshot.getScreenshotAs(OutputType.FILE);
                        var pngPath = tmp.toPath().resolveSibling(String.format(CAPTURE_PATTERN, pngs.size()));
                        var png = Files.move(tmp.toPath(), pngPath);
                        png.toFile().deleteOnExit();
                        pngs.add(png.toFile());
                        Thread.sleep(captureInterval);
                    } while (recordingThread != null);
                } catch (InterruptedException ignore) {
                } catch (Exception e) {
                    e.getStackTrace();
                } finally {
                }
                recordingThread = null;
            }
        };
    }

    private File createVideo(long durationMillis) {
        if (videoDir == null) {
            try {
                videoDir = Files.createTempDirectory("SeleniumRecordr");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        var uuid = UUID.randomUUID();
        var path = videoDir.resolve(uuid.toString() + getVideoFileExt());

        var frameRate = durationMillis > 0 ? (pngs.size() / (durationMillis / 1000))
                : (1000 / captureInterval);
        var pattern = pngs.get(0).toPath().resolveSibling(CAPTURE_PATTERN);
        var args = List.of("ffmpeg",
                "-hide_banner", "-loglevel", "error",
                "-f", "image2", "-r", "" + frameRate,
                "-i", pattern.toString(),
                "-vcodec", "libx264", "-crf", "22", "-y", path.toString());
        System.err.println("FW " + args);
        try {
            var p = new ProcessBuilder().redirectErrorStream(true).inheritIO().command(args).start();
            int status = p.waitFor();
            if (status != 0) {
                throw new RuntimeException("ffmpeg failed " + status);
            }
        } catch (InterruptedException | IOException e) {
            // e.printStackTrace();
            throw new RuntimeException(e);
        }

        return path.toFile();
    }
}
