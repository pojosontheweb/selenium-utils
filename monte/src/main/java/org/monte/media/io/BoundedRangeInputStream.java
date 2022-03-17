
package org.monte.media.io;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.BoundedRangeModel;
import java.io.*;


public class BoundedRangeInputStream
        extends FilterInputStream
        implements BoundedRangeModel {

    private int nread = 0;
    private int size = 0;
    private boolean valueIsAdjusting;

    protected transient ChangeEvent changeEvent_ = null;

    protected EventListenerList listenerList_ = new EventListenerList();


    public BoundedRangeInputStream(InputStream in) {
        super(in);
        try {
            size = in.available();
        } catch (IOException ioe) {
            size = 0;
        }
    }


    public int read()
            throws IOException {
        int c = in.read();
        if (c >= 0) {
            incrementValue(1);
        }
        return c;
    }


    public int read(byte b[])
            throws IOException {
        int nr = in.read(b);
        incrementValue(nr);
        return nr;
    }


    public int read(byte b[], int off, int len)
            throws IOException {
        int nr = in.read(b, off, len);
        incrementValue(nr);
        return nr;
    }


    public long skip(long n) throws IOException {
        long nr = in.skip(n);
        incrementValue((int) nr);
        return nr;
    }


    @Override
    public synchronized void reset()
            throws IOException {
        in.reset();
        nread = size - in.available();
        fireStateChanged();
    }


    private void incrementValue(int inc) {
        if (inc > 0) {
            nread += inc;
            if (nread > size) {
                size = nread;
            }
            fireStateChanged();
        }
    }


    @Override
    public int getMinimum() {
        return 0;
    }


    @Override
    public void setMinimum(int newMinimum) {
    }


    @Override
    public int getMaximum() {
        return size;
    }


    @Override
    public void setMaximum(int newMaximum) {
        size = newMaximum;
        fireStateChanged();
    }


    public int getValue() {
        return nread;
    }


    @Override
    public void setValue(int newValue) {
        nread=newValue;
    }


    public void setValueIsAdjusting(boolean b) {
        valueIsAdjusting = b;
    }


    public boolean getValueIsAdjusting() {
        return valueIsAdjusting;
    }


    public int getExtent() {
        return 0;
    }


    public void setExtent(int newExtent) {
    }


    public void setRangeProperties(int value, int extent, int min, int max, boolean adjusting) {
    }


    public void addChangeListener(ChangeListener l) {
        listenerList_.add(ChangeListener.class, l);
    }


    public void removeChangeListener(ChangeListener l) {
        listenerList_.remove(ChangeListener.class, l);
    }


    protected void fireStateChanged() {
        Object[] listeners = listenerList_.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent_ == null) {
                    changeEvent_ = new ChangeEvent(this);
                }
                ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent_);
            }
        }
    }
}
