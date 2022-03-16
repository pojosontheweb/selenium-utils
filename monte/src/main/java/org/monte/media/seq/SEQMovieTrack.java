
package org.monte.media.seq;

import org.monte.media.ilbm.ColorCycle;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.applet.AudioClip;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.*;
import static java.lang.Math.*;


public class SEQMovieTrack {


    private int width, height;

    private int xPosition, yPosition;

    private int nbPlanes;

    private int nbPalettes_ = -1;

    private int masking;
    public final static int MSK_NONE = 0,
            MSK_HAS_MASK = 1,
            MSK_HAS_TRANSPARENT_COLOR = 2,
            MSK_LASSO = 3;

    private int transparentColor;

    private int xAspect, yAspect;

    private int pageWidth, pageHeight;

    private int screenMode;

    private int jiffies;

    private final boolean isPlayWrapupFrames = true;

    private boolean isSwapSpeakers = false;

    public final static int MODE_INDEXED_COLORS = 0,
            MODE_DIRECT_COLORS = 1,
            MODE_EHB = 2,
            MODE_HAM6 = 3,
            MODE_HAM8 = 4;

    private int compression_;
    public final static int CMP_NONE = 0,
            CMP_BYTE_RUN_1 = 1,
            CMP_VERTICAL = 2;

    private long totalDuration_;

    private Hashtable properties_ = new Hashtable();

    private Vector frames_ = new Vector();

    private Vector audioClips_ = new Vector();

    private PropertyChangeSupport listeners_ = new PropertyChangeSupport(this);

    private ArrayList<ColorCycle> colorCycles = new ArrayList<ColorCycle>();

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners_.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners_.removePropertyChangeListener(listener);
    }


    public void setProperty(String name, Object newValue) {
        Object oldValue = properties_.get(name);
        if (newValue == null) {
            properties_.remove(name);
        } else {
            properties_.put(name, newValue);
        }
        listeners_.firePropertyChange(name, oldValue, newValue);
    }




    public boolean isPlayWrapupFrames() {
        return true;

    }


    public int getInterleave() {
        return frames_.size() > 0 && ((SEQFrame) frames_.get(frames_.size()-1)).getInterleave() == 1 ? 1 : 2;
    }



    public void setSwapSpeakers(boolean newValue) {
        boolean oldValue = isSwapSpeakers;
        isSwapSpeakers = newValue;

        if (newValue != oldValue) {
            for (Enumeration i = frames_.elements(); i.hasMoreElements();) {
                SEQAudioCommand[] aac = ((SEQFrame) i.nextElement()).getAudioCommands();
                if (aac != null) {
                    for (int j = 0; j < aac.length; j++) {
                        aac[j].dispose();
                    }
                }
            }
        }

    }


    public boolean isSwapSpeakers() {
        return isSwapSpeakers;
    }


    public Object getProperty(String name) {
        return properties_.get(name);
    }

    private void firePropertyChange(String name, int oldValue, int newValue) {
        listeners_.firePropertyChange(name, new Integer(oldValue), new Integer(newValue));
    }

    public void setJiffies(int newValue) {
        int oldValue = jiffies;
        jiffies = newValue;
        firePropertyChange("jiffies", oldValue, newValue);
    }

    public void setCompression(int newValue) {
        int oldValue = compression_;
        compression_ = newValue;
        firePropertyChange("compression", oldValue, newValue);
    }

    public void setWidth(int newValue) {
        int oldValue = width;
        width = newValue;
        firePropertyChange("width", oldValue, newValue);
    }

    public void setHeight(int newValue) {
        int oldValue = height;
        height = newValue;
        firePropertyChange("height", oldValue, newValue);
    }

    public void setXPosition(int newValue) {
        int oldValue = xPosition;
        xPosition = newValue;
        firePropertyChange("xPosition", oldValue, newValue);
    }

    public void setYPosition(int newValue) {
        int oldValue = yPosition;
        yPosition = newValue;
        firePropertyChange("yPosition", oldValue, newValue);
    }

    public void setNbPlanes(int newValue) {
        int oldValue = nbPlanes;
        nbPlanes = newValue;
        firePropertyChange("nbPlanes", oldValue, newValue);
    }

    public void setMasking(int newValue) {
        int oldValue = masking;
        masking = newValue;
        firePropertyChange("masking", oldValue, newValue);
    }

    public void setTransparentColor(int newValue) {
        int oldValue = transparentColor;
        transparentColor = newValue;
        firePropertyChange("transparentColor", oldValue, newValue);
    }

    public void setXAspect(int newValue) {
        int oldValue = xAspect;
        xAspect = newValue;
        firePropertyChange("xAspect", oldValue, newValue);
    }

    public void setYAspect(int newValue) {
        int oldValue = yAspect;
        yAspect = newValue;
        firePropertyChange("yAspect", oldValue, newValue);
    }

    public void setPageWidth(int newValue) {
        int oldValue = pageWidth;
        pageWidth = newValue;
        firePropertyChange("pageWidth", oldValue, newValue);
    }

    public void setPageHeight(int newValue) {
        int oldValue = pageHeight;
        pageHeight = newValue;
        firePropertyChange("pageHeight", oldValue, newValue);
    }

    public void setScreenMode(int newValue) {
        int oldValue = screenMode;
        screenMode = newValue;
        firePropertyChange("screenMode", oldValue, newValue);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getXPosition() {
        return xPosition;
    }

    public int getYPosition() {
        return yPosition;
    }

    public int getNbPlanes() {
        return nbPlanes;
    }

    public int getNbPalettes() {
        if (nbPalettes_ == -1) {
            int n = getFrameCount();
            if (n > 0) {
                ColorModel cm = getFrame(0).getColorModel();
                if (cm instanceof IndexColorModel) {
                    IndexColorModel prev = (IndexColorModel) cm;
                    int size = prev.getMapSize();
                    byte[] pr = new byte[size];
                    byte[] pg = new byte[size];
                    byte[] pb = new byte[size];
                    byte[] cr = new byte[size];
                    byte[] cg = new byte[size];
                    byte[] cb = new byte[size];
                    prev.getReds(pr);
                    prev.getGreens(pg);
                    prev.getBlues(pb);
                    nbPalettes_ = 1;
                    for (int i = 1; i < n; i++) {
                        IndexColorModel cur = (IndexColorModel) getFrame(i).getColorModel();
                        if (cur != prev) {
                            cur.getReds(cr);
                            cur.getGreens(cg);
                            cur.getBlues(cb);
                            if (!Arrays.equals(cr, pr) || !Arrays.equals(cg, pg) || !Arrays.equals(cb, pb)) {
                                nbPalettes_++;
                                prev = cur;
                                System.arraycopy(cr, 0, pr, 0, cr.length);
                                System.arraycopy(cg, 0, pg, 0, cg.length);
                                System.arraycopy(cb, 0, pb, 0, cb.length);
                            }
                        }
                    }
                } else {
                    nbPalettes_ = 1;
                    ColorModel prev = cm;
                    for (int i = 1; i < n; i++) {
                        ColorModel cur = getFrame(i).getColorModel();
                        if (cur != prev) {
                            nbPalettes_++;
                            prev = cur;
                        }
                    }

                }
            }
        }
        return nbPalettes_;
    }

    public int getMasking() {
        return masking;
    }

    public int getTransparentColor() {
        return transparentColor;
    }

    public int getXAspect() {
        return xAspect;
    }

    public int getYAspect() {
        return yAspect;
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public int getPageHeight() {
        return pageHeight;
    }

    public int getScreenMode() {
        return screenMode;
    }

    public int getJiffies() {
        return jiffies;
    }

    public int getCompression() {
        return compression_;
    }


    public int getDeltaOperation() {
        int lastFrame = frames_.size() - 1;
        if (lastFrame < 0) {
            return -1;
        }
        return ((SEQFrame) frames_.elementAt(lastFrame)).getOperation();
    }

    ;

    public void addFrame(SEQFrame frame) {
        int oldValue;
        synchronized (this) {
            oldValue = frames_.size();
            frames_.addElement(frame);


            totalDuration_ = -1;
        }
        firePropertyChange("frameCount", oldValue, oldValue + 1);
    }

    public void addAudioClip(AudioClip clip) {
        int oldValue = audioClips_.size();
        audioClips_.addElement(clip);
        firePropertyChange("audioClipCount", oldValue, oldValue + 1);
    }

    public int getAudioClipCount() {
        return audioClips_.size();
    }

    public AudioClip getAudioClip(int index) {
        return (AudioClip) audioClips_.elementAt(index);
    }

    public int getFrameCount() {
        int size = frames_.size();
        if (isPlayWrapupFrames) {
            return size;
        } else {
            int interleave = getInterleave();
            return (size > 1+interleave) ? size - interleave : size;
        }
    }

    public SEQFrame getFrame(int index) {
        return (SEQFrame) frames_.elementAt(index);
    }


    public long getFrameDuration(int index) {
        return ((SEQFrame) frames_.elementAt(index)).getRelTime();
    }


    public synchronized long getTotalDuration() {
        if (totalDuration_ == -1) {
            totalDuration_ = 0;
            for (int i = getFrameCount() - 1; i > -1; i--) {
                totalDuration_ += max(getFrameDuration(i), 1);
            }
        }
        return totalDuration_;
    }

    public void addColorCycle(ColorCycle cc) {
        int oldCount = colorCycles.size();
        colorCycles.add(cc);
        firePropertyChange("colorCyclesCount", oldCount, colorCycles.size());
    }
    public List<ColorCycle> getColorCycles() {
        return colorCycles;
    }
    public int getColorCyclesCount() {
        return colorCycles.size();
    }

    public void removeFrame(int i) {
        frames_.remove(i);
        totalDuration_=-1;
    }
}
