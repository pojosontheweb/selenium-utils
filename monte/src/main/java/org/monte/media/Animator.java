

package org.monte.media;

import java.util.*;
import javax.swing.event.*;

public interface Animator extends Runnable {

    public void setLock(Object lock);
    public boolean isActive();
    public void start();

    public void stop();


    public void dispatch(Interpolator interpolator);

    public void animateStep();

    public void run();
    public void addChangeListener(ChangeListener listener);

    public void removeChangeListener(ChangeListener listener);

    public boolean isSynchronous();
}
