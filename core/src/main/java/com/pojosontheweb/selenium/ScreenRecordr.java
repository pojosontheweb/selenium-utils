package com.pojosontheweb.selenium;

import com.google.common.io.Files;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.monte.media.AudioFormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

public class ScreenRecordr {

    private ScreenRecorder screenRecorder = null;

    public ScreenRecordr start() {

        try {

            if (screenRecorder == null) {

                //Create a instance of GraphicsConfiguration to get the Graphics configuration
                //of the Screen. This is needed for ScreenRecorder class.
                GraphicsConfiguration gc = GraphicsEnvironment//
                        .getLocalGraphicsEnvironment()//
                        .getDefaultScreenDevice()//
                        .getDefaultConfiguration();

                //Create a instance of ScreenRecorder with the required configurations
                screenRecorder = new ScreenRecorder(gc,
                        new Format(
                                MediaTypeKey,
                                FormatKeys.MediaType.FILE,
                                MimeTypeKey, MIME_QUICKTIME),
                        new Format(MediaTypeKey,
                                FormatKeys.MediaType.VIDEO,
                                EncodingKey,
                                ENCODING_QUICKTIME_JPEG,
                                CompressorNameKey,
                                ENCODING_QUICKTIME_JPEG,
//                                COMPRESSOR_NAME_QUICKTIME_JPEG,
                                DepthKey,
                                24,
                                FrameRateKey,
                                Rational.valueOf(15),
                                QualityKey,
                                0.5f,
                                KeyFrameIntervalKey,
                                15 * 60),
                        new Format(
                                MediaTypeKey,
                                FormatKeys.MediaType.VIDEO,
                                EncodingKey,
                                "black",
                                FrameRateKey,
                                Rational.valueOf(30)),
                        null);
            }

            if (!screenRecorder.getState().equals(ScreenRecorder.State.RECORDING)) {
                screenRecorder.start();
            }

            System.out.println("ScreenRecordr : started video recording");
            return this;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            if (screenRecorder.getState().equals(ScreenRecorder.State.RECORDING)) {
                System.out.println("SeleniumScreenRecorder : stopping recorder");
                screenRecorder.stop();
            }
            System.out.println("SeleniumScreenRecorder : stopped video recording. List of created files :");
            for (File f : getVideoFiles()) {
                System.out.println("  * " + f.getAbsolutePath());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<File> getVideoFiles() {
        if (screenRecorder == null) {
            return Collections.emptyList();
        }
        return screenRecorder.getCreatedMovieFiles();
    }

    public ScreenRecordr moveVideoFilesTo(File destDir, String filePrefix) {
        stop();
        List<File> files = getVideoFiles();
        System.out.println("ScreenRecordr : copying " + files.size() + " video files to " + destDir +
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
                File vidFile = new File(destDir, fileName);
                try {
                    Files.copy(f, vidFile);
                    System.out.println("ScreenRecordr : " + f.getAbsolutePath() + "=>" + vidFile.getAbsolutePath());
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
        stop();
        List<File> files = getVideoFiles();
        for (File f : files) {
            f.delete();
        }
        return this;
    }
}
