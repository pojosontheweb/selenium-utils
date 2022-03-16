
package org.monte.media.pbm;

import org.monte.media.gui.ImagePanel;
import org.monte.media.io.BoundedRangeInputStream;
import org.monte.media.AbstractPlayer;
import org.monte.media.gui.JMovieControlAqua;
import org.monte.media.MovieControl;
import org.monte.media.ColorCyclePlayer;
import org.monte.media.ilbm.ColorCyclingMemoryImageSource;
import java.awt.Component;
import java.awt.image.ColorModel;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;


public class PBMPlayer extends AbstractPlayer implements ColorCyclePlayer {


    private ColorCyclingMemoryImageSource memoryImage;

    private BoundedRangeModel timeModel;

    private BoundedRangeInputStream cachingControlModel;

    private InputStream in;

    private int inputFileSize = -1;

    private ImagePanel visualComponent;

    private MovieControl controlComponent;

    private volatile boolean isCached = false;

    public PBMPlayer(InputStream in) {
        this(in, -1);
    }


    public PBMPlayer(InputStream in, int inputFileSize) {
        this.in = in;
        this.inputFileSize = inputFileSize;
        timeModel = new DefaultBoundedRangeModel(0, 0, 0, 0);
    }

    @Override
    protected void doClosed() {
    }

    @Override
    protected void doUnrealized() {
    }

    @Override
    protected void doRealizing() {
        cachingControlModel = new BoundedRangeInputStream(in);
        if (inputFileSize != -1) {
            cachingControlModel.setMaximum(inputFileSize);
        }

        try {
            PBMDecoder decoder = new PBMDecoder(cachingControlModel);
            ArrayList<ColorCyclingMemoryImageSource> track = decoder.produce();
            isCached = true;
            cachingControlModel.setValue(cachingControlModel.getMaximum());
            propertyChangeSupport.firePropertyChange("cached", Boolean.FALSE, Boolean.TRUE);


            if (track.size() == 0) {
                setTargetState(CLOSED);
            } else {
                memoryImage = track.get(0);
                memoryImage.setAnimated(true);
                if (memoryImage.isColorCyclingAvailable()) {
                    propertyChangeSupport.firePropertyChange("colorCyclingAvailable", false, true);
                }
            }
            timeModel.setRangeProperties(0, 1, 0, 1, false);


        } catch (Throwable e) {
            if (visualComponent != null) {
                visualComponent.setMessage(e.toString());
            }
            setTargetState(CLOSED);
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    protected void doRealized() {
        getVisualComponent();
        if (getImageProducer() != null) {
            visualComponent.setImage(visualComponent.getToolkit().createImage(getImageProducer()));
        }
    }

    @Override
    protected void doPrefetching() {

    }

    @Override
    protected void doPrefetched() {

    }

    @Override
    protected void doStarted() {

    }

    @Override
    public void setAudioEnabled(boolean b) {

    }

    @Override
    public boolean isAudioEnabled() {
        return false;
    }

    @Override
    public boolean isAudioAvailable() {
        return false;
    }

    @Override
    public BoundedRangeModel getTimeModel() {
        return timeModel;
    }

    @Override
    public BoundedRangeModel getCachingModel() {
        return cachingControlModel;
    }

    @Override
    public synchronized Component getVisualComponent() {
        if (visualComponent == null) {
            visualComponent = new ImagePanel();
            if (getImageProducer() != null) {
                visualComponent.setImage(visualComponent.getToolkit().createImage(getImageProducer()));
            }

        }
        return visualComponent;
    }

    @Override
    public Component getControlPanelComponent() {
        if (controlComponent == null) {
            controlComponent = new JMovieControlAqua();
            controlComponent.setPlayer(this);
        }
        return controlComponent.getComponent();
    }

    @Override
    public long getTotalDuration() {
        return 0;
    }


    protected ImageProducer getImageProducer() {
        return memoryImage;
    }

    @Override
    public void setColorCyclingStarted(boolean newValue) {
        if (memoryImage != null) {
            boolean oldValue = memoryImage.isColorCyclingStarted();
            memoryImage.setColorCyclingStarted(newValue);
            propertyChangeSupport.firePropertyChange("colorCyclingStarted", oldValue, newValue);
        }
    }

    @Override
    public boolean isColorCyclingAvailable() {
        return memoryImage == null ? false : memoryImage.isColorCyclingAvailable();
    }

    @Override
    public boolean isColorCyclingStarted() {
        return (memoryImage == null) ? false : memoryImage.isColorCyclingStarted();
    }


    @Override
    public boolean isCached() {
        return isCached;
    }


    public void setBlendedColorCycling(boolean newValue) {
        if (memoryImage != null) {
            boolean oldValue = memoryImage.isBlendedColorCycling();
            memoryImage.setBlendedColorCycling(newValue);
            propertyChangeSupport.firePropertyChange("blendedColorCycling", oldValue, newValue);
        }
    }


    public boolean isBlendedColorCycling() {
        return memoryImage == null ? false : memoryImage.isBlendedColorCycling();
    }
}
