
package org.monte.media.ilbm;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageConsumer;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import javax.swing.Timer;


public class ColorCyclingMemoryImageSource extends MemoryImageSource {

    private int width;
    private int height;
    private ColorModel model;
    private Object pixels;
    private int pixeloffset;
    private int pixelscan;
    private Hashtable properties;
    private ArrayList<ColorCycle> colorCycles = new ArrayList<ColorCycle>();
    private Timer timer;
    private HashSet<ImageConsumer> consumers = new HashSet<ImageConsumer>();

    private boolean isColorCyclingAvailable;

    private boolean isStarted;

    private boolean isBlendedColorCycling;
    private volatile ColorModel cycledModel;


    public ColorCyclingMemoryImageSource(int w, int h, ColorModel cm,
            byte[] pix, int off, int scan) {
        super(w, h, cm, pix, off, scan);
        initialize(w, h, cm, (Object) pix, off, scan, new Hashtable());
    }


    public ColorCyclingMemoryImageSource(int w, int h, ColorModel cm,
            byte[] pix, int off, int scan,
            Hashtable<?, ?> props) {
        super(w, h, cm, pix, off, scan, props);
        initialize(w, h, cm, (Object) pix, off, scan, props);
    }


    public ColorCyclingMemoryImageSource(int w, int h, ColorModel cm,
            int[] pix, int off, int scan) {
        super(w, h, cm, pix, off, scan);
        initialize(w, h, cm, (Object) pix, off, scan, null);
    }


    public ColorCyclingMemoryImageSource(int w, int h, ColorModel cm,
            int[] pix, int off, int scan,
            Hashtable<?, ?> props) {
        super(w, h, cm, pix, off, scan, props);
        initialize(w, h, cm, (Object) pix, off, scan, props);
    }

    private void initialize(int w, int h, ColorModel cm,
            Object pix, int off, int scan, Hashtable props) {
        width = w;
        height = h;
        model = cm;
        pixels = pix;
        pixeloffset = off;
        pixelscan = scan;
        if (props == null) {
            props = new Hashtable();
        }
        properties = props;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ColorModel getColorModel() {
        return model;
    }

    public Hashtable getProperties() {
        return properties;
    }

    @Override
    public synchronized void newPixels(byte[] newpix, ColorModel newmodel,
            int offset, int scansize) {
        this.pixels = newpix;
        this.model = newmodel;
        this.pixeloffset = offset;
        this.pixelscan = scansize;
        super.newPixels(newpix, cycledModel == null ? newmodel : cycledModel, offset, scansize);
    }


    @Override
    public synchronized void newPixels(int[] newpix, ColorModel newmodel,
            int offset, int scansize) {
        this.pixels = newpix;
        this.model = newmodel;
        this.pixeloffset = offset;
        this.pixelscan = scansize;
        super.newPixels(newpix, cycledModel == null ? newmodel : cycledModel, offset, scansize);
    }

    public void addColorCycle(ColorCycle cc) {
        colorCycles.add(cc);
    }

    @Override
    public void addConsumer(ImageConsumer ic) {
        super.addConsumer(ic);
        consumers.add(ic);
        if (isStarted && !consumers.isEmpty()) {
            startAnimationTimer();
        }
    }

    @Override
    public void removeConsumer(ImageConsumer ic) {
        super.removeConsumer(ic);
        consumers.remove(ic);
        if (isStarted && consumers.isEmpty()) {
            stopAnimationTimer();
        }
    }

    @Override
    public void setAnimated(boolean b) {
        super.setAnimated(b);
        isColorCyclingAvailable = b;

        if (isColorCyclingAvailable && !consumers.isEmpty() && isStarted) {
            startAnimationTimer();
        } else {
            stopAnimationTimer();
        }
    }


    public void setColorCyclingStarted(boolean b) {
        isStarted = b;
        if (isColorCyclingAvailable && !consumers.isEmpty() && isStarted) {
            startAnimationTimer();
        } else {
            stopAnimationTimer();
        }
    }


    public boolean isColorCyclingStarted() {
        return isStarted;
    }


    public void start() {
        setColorCyclingStarted(true);
    }


    public void stop() {
        setColorCyclingStarted(false);
    }

    public boolean isStarted() {
        return isColorCyclingStarted();
    }

    private synchronized void startAnimationTimer() {
        if (timer != null) {
            return;
        }
        if (model instanceof IndexColorModel) {
            final IndexColorModel icm = (IndexColorModel) model;
            final int[] rgbs = new int[icm.getMapSize()];
            icm.getRGBs(rgbs);


            int delay = 1000;
            int i = 0;
            if (isBlendedColorCycling) {
                for (ColorCycle cc : colorCycles) {
                    if (cc.isActive()) {



                        int ccDelay = 1000 / 4 * cc.getTimeScale() / cc.getRate();
                        if (ccDelay < delay) {
                            delay = Math.max(1, ccDelay);
                        }
                    }
                }
                delay = Math.max(delay, 1000 / 60);
            } else {
                for (ColorCycle cc : colorCycles) {
                    if (cc.isActive()) {


                        int ccDelay = 1000 / 2 * cc.getTimeScale() / cc.getRate();
                        if (ccDelay < delay) {
                            delay = Math.max(1, ccDelay);
                        }
                    }
                }
            }

            timer = new Timer(delay, new ActionListener() {

                private int[] previousCycled = new int[rgbs.length];
                private int[] cycled = new int[rgbs.length];
                long startTime = System.currentTimeMillis();

                @Override
                public void actionPerformed(ActionEvent evt) {
                    long now = System.currentTimeMillis();
                    System.arraycopy(rgbs, 0, cycled, 0, rgbs.length);
                    for (ColorCycle cc : colorCycles) {
                        cc.doCycle(cycled, now - startTime);
                    }

                    if (!Arrays.equals(previousCycled, cycled)) {
                        ColorCyclingMemoryImageSource.super.newPixels((byte[]) pixels,
                                cycledModel = new IndexColorModel(8, cycled.length, cycled, 0, false, -1, DataBuffer.TYPE_BYTE),
                                pixeloffset,
                                pixelscan);
                    }

                    int[] tmp = previousCycled;
                    previousCycled = cycled;
                    cycled = tmp;
                }
            });
            timer.setRepeats(true);
            timer.start();
        }
    }

    private synchronized void stopAnimationTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
            cycledModel = null;

            ColorCyclingMemoryImageSource.super.newPixels((byte[]) pixels,
                    model,
                    pixeloffset,
                    pixelscan);
        }

    }


    public BufferedImage toBufferedImage() {
        DataBuffer buf = (pixels instanceof byte[]) ?
                new DataBufferByte((byte[]) pixels, pixelscan * height, pixeloffset) :
                new DataBufferInt((int[]) pixels, pixelscan * height, pixeloffset);
        SampleModel sm = model.createCompatibleSampleModel(width, height);
        WritableRaster raster = Raster.createWritableRaster(sm, buf, new Point());
        return new BufferedImage(model, raster, false, properties);
    }

    public boolean isColorCyclingAvailable() {
        return isColorCyclingAvailable;
    }

    @SuppressWarnings("unchecked")
    public void putProperties(Hashtable props) {
        properties.putAll(props);
    }

    public void setBlendedColorCycling(boolean newValue) {
        isBlendedColorCycling = newValue;
        for (ColorCycle cc : colorCycles) {
            cc.setBlended(newValue);
        }
        if (isStarted()) {
            stop();
            start();
        }
    }

    public boolean isBlendedColorCycling() {
        return isBlendedColorCycling;
    }
}
