
package org.monte.media;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import javax.swing.BoundedRangeModel;
import javax.swing.event.*;


public interface Player
extends StateModel {

  public final static int UNREALIZED = 0;

  public final static int REALIZING = 1;

  public final static int REALIZED = 2;

  public final static int PREFETCHING = 3;

  public final static int PREFETCHED = 4;

  public final static int STARTED = 5;


  public final static int CLOSED = -1;


  public void setAudioEnabled(boolean b);

  public boolean isAudioEnabled();

  public boolean isAudioAvailable();


  public int getState();

  public int getTargetState();

  public void setTargetState(int state);

  public void realize();

  public void prefetch();

  public void deallocate();


  public void start();


  public void stop();


  public void close();


  public void addStateListener(StateListener listener);


  public void removeStateListener(StateListener listener);


  public void addChangeListener(ChangeListener listener);


  public void removeChangeListener(ChangeListener listener);

  public void addPropertyChangeListener(PropertyChangeListener listener);


  public void removePropertyChangeListener(PropertyChangeListener listener);


  public BoundedRangeModel getTimeModel();


  public BoundedRangeModel getCachingModel();

    public boolean isCached();

  public Component getVisualComponent();
  public Component getControlPanelComponent();

  public long getTotalDuration();


  public boolean isActive();
}
