/**
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Alejandro Gómez Morón
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.agomezmoron.multimedia.recorder;

import com.sun.jna.Platform;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.media.MediaLocator;

import com.github.agomezmoron.multimedia.capture.ScreenCapture;
import com.github.agomezmoron.multimedia.external.JpegImagesToMovie;
import com.github.agomezmoron.multimedia.recorder.configuration.VideoRecorderConfiguration;
import com.github.agomezmoron.multimedia.recorder.listener.VideoRecorderEventListener;
import com.github.agomezmoron.multimedia.recorder.listener.VideoRecorderEventObject;

/**
 * It models the video recorder.
 *
 * @author Alejandro Gomez <agommor@gmail.com>
 *
 */
public class VideoRecorder {

    /**
     * Status of the recorder.
     */
    private boolean recording = false;

    /**
     * Associated frames.
     */
    private List<String> frames;

    /**
     * Video name.
     */
    private String videoName = "output.mov";

    private Thread currentThread;

    private List<VideoRecorderEventListener> listeners = new ArrayList<>();

    private VideoRecorderConfiguration configuration;


    /**
     * Strategy to record using {@link Thread}.
     */
    private Thread getRecordThread() {
        return new Thread() {
            @Override
            public void run() {
                Robot rt;
                ScreenCapture capture;
                try {
                    rt = new Robot();
                    do {
                        capture = new ScreenCapture(rt.createScreenCapture(new Rectangle(
                                configuration.getX(), configuration.getY(),
                                configuration.getWidth(), configuration.getHeight())));

                        VideoRecorderEventObject videoRecorderEvObj = new VideoRecorderEventObject(this, capture);

                        //Exploring all the listeners
                        for (VideoRecorderEventListener vr : listeners) {

                            //Creating the object that will be sent
                            vr.frameAdded(videoRecorderEvObj);
                        }

                        frames.add(VideoRecorderUtil.saveIntoDirectory(capture, new File(
                                configuration.getTempDirectory().getAbsolutePath() + File.separatorChar
                                        + videoName.replace(".mov", ""))));
                        Thread.sleep(configuration.getCaptureInterval());
                    } while (recording);
                } catch (Exception e) {

                    System.out.println(e.getStackTrace());
                    recording = false;
                }
            }
        };
    }

    /**
     * We don't allow to create objects for this class.
     */
    public VideoRecorder(VideoRecorderConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * It stops the recording and creates the video.
     *
     * @return a {@link String} with the path where the video was created or null if the video couldn't be created.
     * @throws MalformedURLException
     */
    public String stop() {
        String videoPathString = null;
        if (recording) {
            recording = false;
            if (currentThread.isAlive()) {
                long now = new Date().getTime();
                while (frames.isEmpty()) {
                    try {
                        Thread.sleep(configuration.getCaptureInterval());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                currentThread.interrupt();
            }
            videoPathString = createVideo();
            if (!configuration.wantToKeepFrames()) {
                deleteDirectory(new File(configuration.getTempDirectory().getAbsolutePath()
                        + File.separatorChar + videoName.replace(".mov", "")));
            }
        }
        return videoPathString;
    }

    /**
     * It starts recording (if it wasn't started before).
     *
     * @param newVideoName with the output of the video.
     */
    public void start(String newVideoName) {
        if (!recording) {
            if (!configuration.getTempDirectory().exists()) {
                configuration.getTempDirectory().mkdirs();
            }
            calculateScreenshotSize();
            videoName = newVideoName;
            if (!videoName.endsWith(".mov")) {
                videoName += ".mov";
            }
            recording = true;
            frames = new ArrayList<String>();
            /**
             * When we started recording.
             */
            long startedAt = new Date().getTime();
            currentThread = getRecordThread();
            currentThread.start();
        }
    }

    /**
     * It calculates the screenshot size before recording. If the useFullScreen was defined, the width, height or x
     */
    private void calculateScreenshotSize() {
        // if fullScreen was set, all the configuration will be changed back.
        if (configuration.wantToUseFullScreen()) {
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            configuration.setWidth((int) size.getWidth());
            configuration.setHeight((int) size.getHeight());
            configuration.setCoordinates(0, 0);
        } else {
            // we have to check if x+width <= Toolkit.getDefaultToolkit().getScreenSize().getWidth() and the same for
            // the height
            if (configuration.getX() + configuration.getWidth() > Toolkit.getDefaultToolkit()
                    .getScreenSize().getWidth()) {
                configuration.setWidth((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth()
                        - configuration.getX()));
            }
            if (configuration.getY() + configuration.getHeight() > Toolkit.getDefaultToolkit()
                    .getScreenSize().getHeight()) {
                configuration.setHeight((int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight()
                        - configuration.getY()));
            }
        }
    }

    /**
     * It deletes recursively a directory.
     *
     * @param directory to be deleted.
     * @return true if the directory was deleted successfully.
     */
    private static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }

    /**
     * It creates the video.
     *
     * @return a {@link String} with the path where the video was created or null if the video couldn't be created.
     * @throws MalformedURLException
     */
    private String createVideo() {
        Vector<String> vector = new Vector<>(frames);
        String videoPathString = null;
        JpegImagesToMovie jpegImaveToMovie = new JpegImagesToMovie();
        if (!configuration.getVideoDirectory().exists()) {
            configuration.getVideoDirectory().mkdirs();
        }
        MediaLocator oml;
        String fileURL;
        if (Platform.isWindows()) {
            fileURL = "file://" + configuration.getVideoDirectory().getPath() + File.separatorChar + videoName;
        } else {
            fileURL = configuration.getVideoDirectory().getAbsolutePath() + File.separatorChar + videoName;
        }
        if ((oml = JpegImagesToMovie.createMediaLocator(fileURL)) == null) {
            System.exit(0);
        }
        if (jpegImaveToMovie.doIt(configuration.getWidth(), configuration.getHeight(), (1000 / configuration.getCaptureInterval()), vector, oml)) {
            videoPathString = configuration.getVideoDirectory().getAbsolutePath() + File.separatorChar
                    + videoName;
        }
        return videoPathString;
    }

}
