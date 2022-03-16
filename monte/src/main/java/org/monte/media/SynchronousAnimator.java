

package org.monte.media;

import java.util.*;
import javax.swing.event.*;


public class SynchronousAnimator implements Animator {
    protected EventListenerList listenerList = new EventListenerList();
    protected ChangeEvent changeEvent;
    private Object lock;
    private long currentTimeMillis;

    private ArrayList<Interpolator> activeInterpolators = new ArrayList<Interpolator>();

    private ArrayList<Interpolator> newInterpolators = new ArrayList<Interpolator>();

    public void setLock(Object lock) {
        this.lock = lock;
    }

    public boolean isActive() {
        return ! newInterpolators.isEmpty() || ! activeInterpolators.isEmpty();
    }

    public void start() {
    }

    public void stop() {
        newInterpolators.clear();
        activeInterpolators.clear();
    }

    public void setTime(long currentTimeMillis) {
        this.currentTimeMillis = currentTimeMillis;
    }

    public void dispatch(Interpolator interpolator) {
        newInterpolators.add(interpolator);
    }

    public void animateStep() {
        long now = currentTimeMillis;




        OuterLoop: for (int i=0; i < newInterpolators.size(); i++) {
            Interpolator candidate = (Interpolator) newInterpolators.get(i);
            boolean isEnqueueable = true;
            for (int j=0; j < i; j++) {
                Interpolator before = (Interpolator) newInterpolators.get(j);
                if (candidate.isSequential(before)) {
                    isEnqueueable = false;
                    break;
                }
            }
            if (isEnqueueable) {
                for (int j=0; j < activeInterpolators.size(); j++) {
                    Interpolator before = (Interpolator) activeInterpolators.get(j);
                    if (candidate.replaces(before)) {
                        before.finish(now);
                    }
                    if (candidate.isSequential(before)) {
                        isEnqueueable = false;
                        break;
                    }
                }
            }
            if (isEnqueueable) {
                candidate.initialize(now);
                activeInterpolators.add(candidate);
                if (newInterpolators.size() > 0) {
                    newInterpolators.remove(i--);
                }
            }
        }



        for (int i=0; i < activeInterpolators.size(); i++) {
            Interpolator active = (Interpolator) activeInterpolators.get(i);
            if (active.isFinished()) {
                activeInterpolators.remove(i--);
            } else if (active.isElapsed(now)) {
                active.finish(now);
                activeInterpolators.remove(i--);
            } else {
                active.interpolate(now);
            }
        }
    }

    public void run() {
    }

    public void addChangeListener(ChangeListener listener) {
        listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listenerList.remove(ChangeListener.class, listener);
    }

    protected void fireStateChanged() {

        Object[] listeners = listenerList.getListenerList();


        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ChangeListener.class) {

                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }

    public boolean isSynchronous() {
        return true;
    }

}
