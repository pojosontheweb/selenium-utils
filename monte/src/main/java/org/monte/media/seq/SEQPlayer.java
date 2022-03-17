
package org.monte.media.seq;

import org.monte.media.MovieControl;
import org.monte.media.ColorCyclePlayer;
import org.monte.media.AbstractPlayer;
import org.monte.media.gui.JMovieControlAqua;
import org.monte.media.gui.ImagePanel;
import org.monte.media.image.BitmapImage;
import org.monte.media.*;
import org.monte.media.io.BoundedRangeInputStream;
import org.monte.media.ilbm.ColorCycle;
import org.monte.media.ilbm.ColorCyclingMemoryImageSource;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import static java.lang.Math.*;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.event.*;



public class SEQPlayer
        extends AbstractPlayer
        implements ColorCyclePlayer {

    
    private ColorCyclingMemoryImageSource memoryImage;
    
    private BoundedRangeModel timeModel;
    
    private BoundedRangeInputStream cachingControlModel;
    
    private InputStream in;
    
    private int inputFileSize = -1;
    
    private SEQMovieTrack track;
    
    private BitmapImage bitmapEven, bitmapOdd;
    
    private int preparedEven, preparedOdd;
    
    private int fetchedEven, fetchedOdd;
    
    private int displayFrame = -1;
    
    private boolean isPlayEveryFrame = false;
    
    private volatile boolean isLoop = true;
    

    
    private float jiffieMillis = 1000f / 60f;
    
    private int globalFrameDuration = -1;
    
    private ImagePanel  visualComponent;
    
    private MovieControl controlComponent;
    
    private Object decoderLock = new Object();
    
    private ColorModel preferredColorModel = null;
    
    private volatile boolean isCached = false;
    
    private SEQAudioCommand[] audioChannels = new SEQAudioCommand[4];
    
    private boolean isAudioEnabled = true;
    
    private boolean isLoadAudio;
    
    private boolean debug = false;
    
    private Hashtable properties;
    
    private boolean isAudioAvailable;
    
    private boolean isColorCyclingAvailable;
    
    private boolean isColorCyclingStarted;
    
    private boolean isPingPong = true;
    
    private int playDirection = 1;

    private class Handler implements MouseListener, PropertyChangeListener, ChangeListener {




        @Override
        public void mouseClicked(MouseEvent event) {
            if (getState() != CLOSED && event.getModifiers() == InputEvent.BUTTON1_MASK) {
                if (getState() == STARTED && getTargetState() == STARTED && event.getClickCount() == 1) {
                    stop();
                } else if (getState() != STARTED && getTargetState() != STARTED && event.getClickCount() == 2) {
                    start();
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent event) {
        }

        @Override
        public void mouseExited(MouseEvent event) {
        }

        @Override
        public void mousePressed(MouseEvent event) {
        }

        @Override
        public void mouseReleased(MouseEvent event) {
        }




        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (timeModel != null) {
                int count = track.getFrameCount();
                timeModel.setMaximum(count > 0 ? count - 1 : 0);
                synchronized (decoderLock) {
                    decoderLock.notifyAll();
                }
            }
            if (event.getPropertyName().equals("audioClipCount")) {
                setAudioAvailable(track.getAudioClipCount() > 0);
            } else if (event.getPropertyName().equals("colorCyclesCount")) {
                setColorCyclingAvailable(track.getColorCyclesCount() > 0);
            }
        }




        @Override
        public void stateChanged(ChangeEvent evt) {

            if (evt.getSource() == timeModel) {
                if (getState() == STARTED) {

                    synchronized (this) {
                        notifyAll();
                    }
                } else {

                    dispatcher.dispatch(
                            new Runnable() {

                                @Override
                                public void run() {
                                    renderVideo(getTimeModel().getValue());
                                }
                            });
                }
            }
        }
    }
    private Handler handler = new Handler();

    public SEQPlayer(InputStream in) {
        this(in, -1, true);
    }

    
    public SEQPlayer(InputStream in, int inputFileSize, boolean loadAudio) {
        this.in = in;
        this.inputFileSize = inputFileSize;
        this.isLoadAudio = loadAudio;
    }

    
    public void setPreferredColorModel(ColorModel cm) {
        if (bitmapEven == null) {
            preferredColorModel = cm;
        }
    }

    
    @Override
    public BoundedRangeModel getTimeModel() {
        return timeModel;
    }

    
    @Override
    public void setAudioEnabled(boolean newValue) {
        boolean oldValue = isAudioEnabled;
        isAudioEnabled = newValue;
        propertyChangeSupport.firePropertyChange("audioEnabled",
                (oldValue) ? Boolean.TRUE : Boolean.FALSE,
                (newValue) ? Boolean.TRUE : Boolean.FALSE);
    }

    
    @Override
    public boolean isAudioEnabled() {
        return isAudioEnabled;
    }

    
    public void setSwapSpeakers(boolean newValue) {
        boolean oldValue = track.isSwapSpeakers();
        track.setSwapSpeakers(newValue);
        propertyChangeSupport.firePropertyChange("swapSpeakers",
                (oldValue) ? Boolean.TRUE : Boolean.FALSE,
                (newValue) ? Boolean.TRUE : Boolean.FALSE);
    }

    
    public boolean isSwapSpeakers() {
        return track.isSwapSpeakers();
    }

    
    @Override
    public BoundedRangeModel getCachingModel() {
        return cachingControlModel;
    }

    
    protected ImageProducer getImageProducer() {
        return memoryImage;
    }

    
    public SEQMovieTrack getMovieTrack() {
        return track;
    }

    
    @Override
    public synchronized Component getVisualComponent() {
        if (visualComponent == null) {
            visualComponent =  new ImagePanel();
            if (getImageProducer() != null) {
                visualComponent.setImage(visualComponent.getToolkit().createImage(getImageProducer()));
            }
            visualComponent.addMouseListener(handler);
        }
        return visualComponent;
    }

    
    @Override
    public synchronized Component getControlPanelComponent() {
        if (controlComponent == null) {
            controlComponent = new JMovieControlAqua();
            controlComponent.setPlayer(this);
        }
        return controlComponent.getComponent();
    }

    
    @Override
    protected void doUnrealized() {
    }

    
    @Override
    protected void doRealizing() {
        timeModel = new DefaultBoundedRangeModel(0, 0, 0, 0);
        timeModel.addChangeListener(handler);
        cachingControlModel = new BoundedRangeInputStream(in);
        

        track = new SEQMovieTrack();
        track.addPropertyChangeListener(handler);




        synchronized (this) {
            if (controlComponent != null) {
                controlComponent.setPlayer(this);
            }
        }



        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    SEQDecoder decoder = new SEQDecoder(cachingControlModel);
                    decoder.produce(track, isLoadAudio);
                    isCached = true;
                    cachingControlModel.setValue(cachingControlModel.getMaximum());
                    propertyChangeSupport.firePropertyChange("cached", Boolean.FALSE, Boolean.TRUE);



                    if (track.getFrameCount() == 0) {
                        synchronized (decoderLock) {
                            setTargetState(CLOSED);
                            decoderLock.notifyAll();
                        }
                    }
                } catch (Throwable e) {
                    synchronized (decoderLock) {
                        if (visualComponent != null) {
                            visualComponent.setMessage(e.toString());
                        }
                        setTargetState(CLOSED);
                        decoderLock.notifyAll();
                        e.printStackTrace();
                    }
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
        };
        t.start();




        synchronized (decoderLock) {
            while (track.getFrameCount() < 1 && getTargetState() != CLOSED) {
                try {
                    decoderLock.wait();
                } catch (InterruptedException e) {
                }
            }
        }



        ColorModel cm;
        int width = track.getWidth();
        int height = track.getHeight();
        int nbPlanes = track.getNbPlanes();
        int masking = track.getMasking();

        if (track.getFrameCount() > 0) {
            SEQFrame frame = track.getFrame(0);
            cm = frame.getColorModel();
        } else {
            cm = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
        }
        bitmapEven = new BitmapImage(
                width,
                height,
                nbPlanes + (masking == SEQMovieTrack.MSK_HAS_MASK ? 1 : 0),
                cm);
        bitmapOdd = new BitmapImage(
                width,
                height,
                nbPlanes + (masking == SEQMovieTrack.MSK_HAS_MASK ? 1 : 0),
                cm);

        jiffieMillis = 1000f / (float) track.getJiffies();

        if (track.getColorCycles().isEmpty()) {
            bitmapEven.setPreferredChunkyColorModel(preferredColorModel);
            bitmapOdd.setPreferredChunkyColorModel(preferredColorModel);
        }

         properties = new Hashtable();
        properties.put(
                "aspect",
                new Double((double) track.getXAspect() / (double) track.getYAspect()));
        Object comment = track.getProperty("comment");
        if (comment != null) {
            properties.put("comment", comment);
        }
        String s;
        switch (track.getScreenMode()) {
            case SEQMovieTrack.MODE_INDEXED_COLORS:
                s = "Indexed Colors";
                break;
            case SEQMovieTrack.MODE_DIRECT_COLORS:
                s = "Direct Colors";
                break;
            case SEQMovieTrack.MODE_EHB:
                s = "EHB";
                break;
            case SEQMovieTrack.MODE_HAM6:
                s = "HAM 6";
                break;
            case SEQMovieTrack.MODE_HAM8:
                s = "HAM 8";
                break;
            default:
                s = "unknown";
                break;
        }
        properties.put("screenMode", s);
        properties.put("nbPlanes", "" + track.getNbPlanes());
        properties.put("jiffies", "" + track.getJiffies());
        properties.put("colorCycling", "" + track.getColorCycles().size());

        if (bitmapEven.isEnforceDirectColors()) {
            cm = (preferredColorModel instanceof DirectColorModel) ? preferredColorModel : new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
            memoryImage = new ColorCyclingMemoryImageSource(width, height, cm, new int[width * height], 0, width, properties);
        } else if (cm instanceof DirectColorModel) {
            memoryImage = new ColorCyclingMemoryImageSource(width, height, cm, new int[width * height], 0, width, properties);
        } else {
            memoryImage = new ColorCyclingMemoryImageSource(width, height, cm, new byte[width * height], 0, width, properties);
            if (track.getColorCycles().size() > 0) {
                for (ColorCycle cc : track.getColorCycles()) {
                    memoryImage.addColorCycle(cc);
                }
                if (isColorCyclingStarted()) {
                    memoryImage.start();
                }
            }

        }
        memoryImage.setAnimated(true);
        preparedEven = preparedOdd = Integer.MAX_VALUE;
        fetchedEven = fetchedOdd = Integer.MAX_VALUE;
        if (track.getFrameCount() > 0) {
            renderVideo(0);
            properties.put("renderMode", bitmapEven.getChunkyColorModel());
        }




        synchronized (this) {
            if (visualComponent != null) {
                visualComponent.setImage(visualComponent.getToolkit().createImage(getImageProducer()));
            }
        }
    }

    
    @Override
    protected void doRealized() {

    }

    
    @Override
    protected void doPrefetching() {
        renderVideo(timeModel.getValue());
    }

    
    @Override
    protected void doPrefetched() {
    }

    public void setPlayEveryFrame(boolean newValue) {
        isPlayEveryFrame = newValue;
    }

    

    
    public void setDebug(boolean newValue) {
        this.debug = newValue;
        if (newValue == false && visualComponent != null) {
            visualComponent.setMessage(null);
        }
    }

    
    public boolean isPlayWrapupFrames() {
        return true;

    }

    
    public void setFramesPerSecond(float framesPerSecond) {
        if (framesPerSecond <= 0f) {
            setGlobalFrameDuration(-1);
        } else {
            setGlobalFrameDuration((int) (1000f / framesPerSecond));
        }
    }

    
    public void setGlobalFrameDuration(int frameDuration) {
        this.globalFrameDuration = frameDuration;
    }

    public boolean isPlayEveryFrame() {
        return isPlayEveryFrame;
    }

    public void setLoop(boolean newValue) {
        boolean oldValue = isLoop;
        isLoop = newValue;
        propertyChangeSupport.firePropertyChange("isLoop", oldValue, newValue);
    }

    public boolean isLoop() {
        return isLoop;
    }

    public String getDeltaOperationDescription() {
        String s;
        int op = track.getDeltaOperation();
        switch (op) {
            case SEQDeltaFrame.OP_Copy:
                s = "OP Direct";
                break;
            case SEQDeltaFrame.OP_XOR:
                s = "XOR";
                break;
            default:
                s = "unknown";
                break;
        }
        return s + " OP(" + op + ")";
    }

    
    @Override
    protected void doStarted() {
        long mediaTime = System.currentTimeMillis() + (long) jiffieMillis;
        int index;
        long sleepTime;


        if (timeModel.getValue() == timeModel.getMaximum()) {
            timeModel.setValue(timeModel.getMinimum());
        }

        while (getTargetState() == STARTED) {
            index = timeModel.getValue();
            if (isPlayEveryFrame) {
                if (isAudioEnabled) {
                    prepareAudio(index);
                }
                prepareVideo(index);
                if (mediaTime > System.currentTimeMillis()) {
                    sleepTime = mediaTime - System.currentTimeMillis();
                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                        }
                    }
                }
                if (globalFrameDuration == -1) {
                    mediaTime = System.currentTimeMillis() + (long) (max(track.getFrameDuration(index),1) * jiffieMillis);
                } else {
                    mediaTime = System.currentTimeMillis() + globalFrameDuration;
                }
                if (isAudioEnabled && !timeModel.getValueIsAdjusting()) {
                    renderAudio(index);
                } else {
                    muteAudio();
                }
                renderVideo(index);
            } else {
                if (mediaTime > System.currentTimeMillis()) {
                    if (isAudioEnabled) {
                        prepareAudio(index);
                    }
                    prepareVideo(index);
                    sleepTime = mediaTime - System.currentTimeMillis();
                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                        }
                    }
                    if (globalFrameDuration == -1) {
                        mediaTime += (long) (max(track.getFrameDuration(index),1) * jiffieMillis);
                    } else {
                        mediaTime += (long) globalFrameDuration;
                    }
                    if (isAudioEnabled && !timeModel.getValueIsAdjusting()) {
                        renderAudio(index);
                    } else {
                        muteAudio();
                    }
                    renderVideo(index);
                } else {
                    if (isAudioEnabled && !timeModel.getValueIsAdjusting()) {
                        renderAudio(index);
                    } else {
                        muteAudio();
                    }
                    if (globalFrameDuration == -1) {
                        mediaTime += (long) (max(track.getFrameDuration(index),1) * jiffieMillis);
                    } else {
                        mediaTime += (long) globalFrameDuration;
                    }
                }
            }

            if (!timeModel.getValueIsAdjusting()) {
                if (timeModel.getValue() == timeModel.getMaximum()) {
                    if (isCached && isLoop && !isPingPong) {
                        timeModel.setValue(timeModel.getMinimum());
                    } else if (isCached && isPingPong && playDirection == 1) {
                        playDirection = -1;
                        timeModel.setValue(timeModel.getValue() + playDirection);
                    } else {
                        break;
                    }
                } else if (timeModel.getValue() == timeModel.getMinimum() && isPingPong && playDirection == -1) {
                    playDirection = 1;
                    timeModel.setValue(timeModel.getValue() + playDirection);
                } else {
                    timeModel.setValue(timeModel.getValue() + playDirection);
                }
            }
        }
        

        renderVideo(timeModel.getValue());
        muteAudio();
    }

    private void muteAudio() {
        for (int i = 0; i < audioChannels.length; i++) {
            if (audioChannels[i] != null) {
                audioChannels[i].stop(track);
                audioChannels[i] = null;
            }
        }
    }

    
    @Override
    protected void doClosed() {
        try {
            in.close();
        } catch (IOException e) {
        }
    }

    private void fetchFrame(int index) {
        SEQFrame frame = null;
        int fetched;
        int interleave = track.getInterleave();
        BitmapImage bitmap;
        if (interleave == 1 || (index & 1) == 0) {

            if (fetchedEven == index) {
                return;
            }
            fetched = fetchedEven;
            bitmap = bitmapEven;
            fetchedEven = index;
            if (fetched == index + interleave && track.getFrame(fetched).isBidirectional()) {
                frame = (SEQFrame) track.getFrame(fetched);
                frame.decode(bitmap, track);
                return;
            } else {
                if (fetched > index) {
                    frame = (SEQFrame) track.getFrame(0);
                    frame.decode(bitmap, track);
                    fetched = 0;
                }
            }
        } else {

            if (fetchedOdd == index) {
                return;
            }
            fetched = fetchedOdd;
            bitmap = bitmapOdd;
            fetchedOdd = index;
            if (fetched == index + interleave && track.getFrame(fetched).isBidirectional()) {
                frame = (SEQFrame) track.getFrame(fetched);
                frame.decode(bitmap, track);
                return;
            } else {
                if (fetched > index) {
                    frame = (SEQFrame) track.getFrame(0);
                    frame.decode(bitmap, track);
                    frame = (SEQFrame) track.getFrame(1);
                    frame.decode(bitmap, track);
                    fetched = 1;
                }
            }
        }
        for (int i = fetched + interleave; i <= index; i += interleave) {
            frame = (SEQFrame) track.getFrame(i);
            frame.decode(bitmap, track);
        }
    }

    
    private void prepareVideo(int index) {
        BitmapImage bitmap;
        int prepared;
        int interleave = track.getInterleave();

        if (interleave == 1 || (index & 1) == 0) {

            if (preparedEven == index) {
                return;
            }
            prepared = preparedEven;
            preparedEven = index;
            bitmap = bitmapEven;
        } else {

            if (preparedOdd == index) {
                return;
            }
            prepared = preparedOdd;
            preparedOdd = index;
            bitmap = bitmapOdd;
        }



        fetchFrame(index);


        SEQFrame frame = (SEQFrame) track.getFrame(index);
        ColorModel cm = frame.getColorModel();
        bitmap.setPlanarColorModel(cm);
        if (prepared == index - interleave &&
                (bitmap.getPixelType() == BitmapImage.BYTE_PIXEL ||
                cm == ((SEQFrame) track.getFrame(prepared)).getColorModel())) {
            bitmap.convertToChunky(
                    frame.getTopBound(track),
                    frame.getLeftBound(track),
                    frame.getBottomBound(track),
                    frame.getRightBound(track));

        } else if (isPingPong && prepared == index + interleave &&
                (bitmap.getPixelType() == BitmapImage.BYTE_PIXEL ||
                cm == ((SEQFrame) track.getFrame(prepared)).getColorModel())) {
            frame = (SEQFrame) track.getFrame(index + interleave);
            bitmap.convertToChunky(
                    frame.getTopBound(track),
                    frame.getLeftBound(track),
                    frame.getBottomBound(track),
                    frame.getRightBound(track));
        } else {
            bitmap.convertToChunky();
        }
    }

    
    private void prepareAudio(int index) {
        SEQFrame frame = (SEQFrame) track.getFrame(index);
        SEQAudioCommand[] audioCommands = frame.getAudioCommands();
        if (audioCommands != null) {
            for (int i = 0; i < audioCommands.length; i++) {
                audioCommands[i].prepare(track);
            }
        }
    }

    
    private void renderVideo(int index) {
        if (displayFrame == index) {
            return;
        }
        int interleave = track.getInterleave();

        BitmapImage bitmap;
        if (interleave == 1 || (index & 1) == 0) {

            bitmap = bitmapEven;
        } else {

            bitmap = bitmapOdd;
        }

        prepareVideo(index);
        ColorModel cm = bitmap.getChunkyColorModel();
        if (bitmap.getPixelType() == BitmapImage.INT_PIXEL) {
            memoryImage.newPixels(bitmap.getIntPixels(), cm, 0, track.getWidth());
        } else {
            memoryImage.newPixels(bitmap.getBytePixels(), cm, 0, track.getWidth());
        }
        displayFrame = index;

        if (debug && visualComponent != null) {
            SEQFrame frame = (SEQFrame) track.getFrame(index);
            StringBuilder buf = new StringBuilder();
            buf.append("frame:");
            buf.append(index);
            buf.append(" duration:");
            buf.append(frame.getRelTime());
            buf.append(", seq op:");
            buf.append(frame.getOperation());

            SEQAudioCommand[] audioCommands = frame.getAudioCommands();
            if (audioCommands != null) {
                for (int i = 0; i < audioCommands.length; i++) {
                    switch (audioCommands[i].getCommand()) {
                        case SEQAudioCommand.COMMAND_PLAY_SOUND:
                            buf.append("\nplay");
                            break;
                        case SEQAudioCommand.COMMAND_STOP_SOUND:
                            buf.append("\nstop");
                            break;
                        case SEQAudioCommand.COMMAND_SET_FREQVOL:
                            buf.append("\nfreqvol");
                            break;
                        default:
                            buf.append("ILLEGAL COMMAND:");
                            buf.append(audioCommands[i].getCommand());
                            break;
                    }
                    buf.append(" sound:");
                    buf.append(audioCommands[i].getSound());
                    buf.append(" freq:");
                    buf.append(audioCommands[i].getFrequency());
                    buf.append(" vol:");
                    buf.append(audioCommands[i].getVolume());
                    buf.append(" channels:");
                    int channelMask = audioCommands[i].getChannelMask();
                    boolean first = true;
                    for (int j = 0; j < 4; j++) {
                        if (((1 << j) & channelMask) != 0) {
                            if (!first) {
                                buf.append(", ");
                            }
                            buf.append(j);
                            buf.append((j % 2 == 0) ? "(l)" : "(r)");
                            first = false;
                        }
                    }
                }
            }

            visualComponent.setMessage(buf.toString());
        }
    }

    
    private synchronized void renderAudio(int index) {
        prepareAudio(index);


        if (isActive()) {

            SEQFrame frame = (SEQFrame) track.getFrame(index);
            SEQAudioCommand[] audioCommands = frame.getAudioCommands();
            if (audioCommands != null) {
                for (int i = 0; i < audioCommands.length; i++) {
                    audioCommands[i].doCommand(track, audioChannels);
                }
            }
        }
    }

    
    @Override
    public long getTotalDuration() {
        if (globalFrameDuration == -1) {
            return (long) (track.getTotalDuration() * jiffieMillis);
        } else {
            return track.getFrameCount() * globalFrameDuration;
        }
    }

    
    @Override
    public boolean isCached() {
        return isCached;
    }

    
    @Override
    public boolean isAudioAvailable() {
        return isAudioAvailable;
    }

    private void setAudioAvailable(boolean newValue) {
        boolean oldValue = isAudioAvailable;
        isAudioAvailable = newValue;
        propertyChangeSupport.firePropertyChange("audioAvailable", oldValue, newValue);
    }

    public void setPingPong(boolean newValue) {
        boolean oldValue = isPingPong;
        isPingPong = newValue;
        if (!newValue) {
            playDirection = 1;
        }
        propertyChangeSupport.firePropertyChange("pingPong", oldValue, newValue);
    }

    public boolean isPingPong() {
        return isPingPong;
    }

    private void setColorCyclingAvailable(boolean newValue) {
        boolean oldValue = isColorCyclingAvailable;
        isColorCyclingAvailable = newValue;
        propertyChangeSupport.firePropertyChange("colorCyclingAvailable", oldValue, newValue);
    }

    
    @Override
    public boolean isColorCyclingStarted() {
        return isColorCyclingStarted;
    }

    
    @Override
    public void setColorCyclingStarted(boolean newValue) {
        boolean oldValue = isColorCyclingStarted;
        isColorCyclingStarted = newValue;
        if (memoryImage != null) {
            memoryImage.setColorCyclingStarted(newValue);
            propertyChangeSupport.firePropertyChange("colorCyclingStarted", oldValue, newValue);
        }
    }

    
    @Override
    public boolean isColorCyclingAvailable() {
        return isColorCyclingAvailable;
    }

    
    @Override
    public void setBlendedColorCycling(boolean newValue) {
        if (memoryImage != null) {
            boolean oldValue = memoryImage.isBlendedColorCycling();
            memoryImage.setBlendedColorCycling(newValue);
            propertyChangeSupport.firePropertyChange("blendedColorCycling", oldValue, newValue);
        }
    }

    
    @Override
    public boolean isBlendedColorCycling() {
        return memoryImage == null ? false : memoryImage.isBlendedColorCycling();
    }
}
