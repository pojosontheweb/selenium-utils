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
package com.github.agomezmoron.multimedia.recorder.configuration;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.util.UUID;

import com.github.agomezmoron.multimedia.recorder.VideoRecorder;

/**
 * Video recorder configuration to be used in {@link VideoRecorder}.
 * @author Alejandro Gomez <agommor@gmail.com>
 *
 */
public class VideoRecorderConfiguration {

    /**
     * Screen Width.
     */
    private int width = getMaxWidth();

    /**
     * Screen Height.
     */
    private int height = getMaxHeight();

    /**
     * Flag to know if the user want to keep the frames.
     */
    private boolean keepFrames = false;

    /**
     * Flag to know if the video will be in full screen or not.
     */
    private boolean useFullScreen = true;

    /**
     * X coordinate.
     */
    private int x = 0;

    /**
     * y coordinate.
     */
    private int y = 0;

    /**
     * Interval where the images will be capture (in milliseconds).
     */
    private int captureInterval = 50;

    private final File defaultDirectory = createDefaultDirectory();

    private static File createDefaultDirectory() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File f = new File(tmpDir + File.separator + UUID.randomUUID());
        f.mkdirs();
        return f;
    }

    /**
     * Temporal directory to be used.
     */
    private File tempDirectory = defaultDirectory;

    /**
     * Video path where the video will be saved.
     */
    private File videoDirectory = defaultDirectory;

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public  void setWidth(int width) {
        if (this.width >= 0 && width <= getMaxWidth()) {
            this.width = width;
            if (this.width < getMaxWidth()) {
                useFullScreen = false;
            }
        }
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        if (this.height >= 0 && height <= getMaxHeight()) {
            this.height = height;
            if (this.height < getMaxHeight()) {
                useFullScreen = false;
            }
        }
    }

    /**
     * @return the keepFrames
     */
    public boolean wantToKeepFrames() {
        return keepFrames;
    }

    /**
     * @param keepFrames the keepFrames to set
     */
    public void setKeepFrames(boolean keepFrames) {
        this.keepFrames = keepFrames;
    }

    /**
     * @return the useFullScreen
     */
    public boolean wantToUseFullScreen() {
        return useFullScreen;
    }

    /**
     * @param useFullScreen the useFullScreen to set
     */
    public void wantToUseFullScreen(boolean useFullScreen) {
        this.useFullScreen = useFullScreen;
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @param x the x to set.
     */
    public void setX(int x) {
        if (this.x >= 0 && x <= getMaxWidth()) {
            this.x = x;
        }
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y) {
        if (this.y >= 0 && y <= getMaxHeight()) {
            this.y = y;
        }
    }

    /**
     * @return the captureInterval
     */
    public int getCaptureInterval() {
        return captureInterval;
    }

    /**
     * @param captureInterval the captureInterval to set
     */
    public void setCaptureInterval(int captureInterval) {
        this.captureInterval = captureInterval;
    }

    /**
     * @return the tempDirectory
     */
    public File getTempDirectory() {
        return tempDirectory;
    }

    /**
     * @param tempDirectory the tempDirectory to set
     */
    public void setTempDirectory(File tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    /**
     * It sets the x coordinate.
     * @param newX to be used.
     * @param newY to be used.
     */
    public void setCoordinates(int newX, int newY) {
        setX(newX);
        setY(newY);
    }

    /**
     * @return the defaultDirectory
     */
    public File getDefaultDirectory() {
        return defaultDirectory;
    }

    /**
     * @return the videoPath
     */
    public File getVideoDirectory() {
        return videoDirectory;
    }

    /**
     * @param videoPath the videoPath to set
     */
    public void setVideoDirectory(File videoPath) {
        this.videoDirectory = videoPath;
    }

    public final int getMaxWidth() {
        int maxWidth = 0;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();
        Rectangle allScreenBounds = new Rectangle();
        for (GraphicsDevice screen : screens) {
            Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
            maxWidth += screenBounds.width;
        }
        return maxWidth;
    }

    public final int getMaxHeight() {
        int maxHeight = 0;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();
        Rectangle allScreenBounds = new Rectangle();
        for (GraphicsDevice screen : screens) {
            Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
            maxHeight += screenBounds.height;
        }
        return maxHeight;
    }
}
