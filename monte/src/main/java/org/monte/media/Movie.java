
package org.monte.media;

import org.monte.media.math.Rational;
import java.beans.PropertyChangeListener;


public interface Movie {

    public final static String PLAYHEAD_PROPERTY = "playhead";
    public final static String IN_PROPERTY = "in";
    public final static String OUT_PROPERTY = "out";


    public Rational getDuration();


    public void setInsertionPoint(Rational seconds);


    public Rational getInsertionPoint();


    public Rational getSelectionStart();


    public void setSelectionStart(Rational in);


    public Rational getSelectionEnd();


    public void setSelectionEnd(Rational out);


    public long timeToSample(int track, Rational seconds);

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);

    public Rational sampleToTime(int track, long sample);

    public int getTrackCount();

    public Format getFormat(int track);

    public Format getFileFormat();

    public MovieReader getReader();
}
