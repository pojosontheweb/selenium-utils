

package org.monte.media;

import java.util.*;
import javax.swing.event.*;

public class DefaultAnimator implements Animator {
    protected EventListenerList listenerList = new EventListenerList();
    
    private Vector activeInterpolators;
    
    private Vector newInterpolators;
    
    
    private Thread animationThread;
    private boolean isAnimating;
    private Object lock = new Object();
    protected ChangeEvent changeEvent;
    
    
    private int sleep = 33;
    
    
    public DefaultAnimator() {
        activeInterpolators = new Vector();
        newInterpolators = new Vector();
    }
    
    
    public void setLock(Object lock) {
        this.lock = lock;
    }
    
    public boolean isActive() {
        return animationThread != null;
    }
    
    public void start() {
        stop();
        animationThread = new Thread(this);
        animationThread.start();
    }
    
    public void stop() {
        if (animationThread != null) {
            Thread t = animationThread;
            animationThread = null;
            t.interrupt();
            try {
                t.join();
            } catch (InterruptedException e) {
            }
        }
    }
    
    
    public void dispatch(Interpolator interpolator) {
        synchronized (newInterpolators) {
            newInterpolators.addElement(interpolator);
            if (! isActive()) start();
        }
    }
    
    public void animateStep() {
        long now = System.currentTimeMillis();
        



        OuterLoop: for (int i=0; i < newInterpolators.size(); i++) {
            Interpolator candidate = (Interpolator) newInterpolators.elementAt(i);
            boolean isEnqueueable = true;
            for (int j=0; j < i; j++) {
                Interpolator before = (Interpolator) newInterpolators.elementAt(j);
                if (candidate.isSequential(before)) {
                    isEnqueueable = false;
                    break;
                }
            }
            if (isEnqueueable) {
                for (int j=0; j < activeInterpolators.size(); j++) {
                    Interpolator before = (Interpolator) activeInterpolators.elementAt(j);
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
                activeInterpolators.addElement(candidate);
                newInterpolators.removeElementAt(i--);
            }
        }
        


        for (int i=0; i < activeInterpolators.size(); i++) {
            Interpolator active = (Interpolator) activeInterpolators.elementAt(i);
            if (active.isFinished()) {
                activeInterpolators.removeElementAt(i--);
            } else if (active.isElapsed(now)) {
                active.finish(now);
                activeInterpolators.removeElementAt(i--);
            } else {
                active.interpolate(now);
            }
        }
    }
    
    public void run() {

        


        while (Thread.currentThread() == animationThread) {
            synchronized (lock) {
                animateStep();
            }
            
            boolean hasFinished = false;
            synchronized (newInterpolators) {
                if (activeInterpolators.size() == 0 && newInterpolators.size() == 0) {
                    animationThread = null;
                    hasFinished = true;
                }
            }
            if (hasFinished) {
                fireStateChanged();
                return;
            }
            
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
            }
        }
        




        synchronized (newInterpolators) {
            synchronized (lock) {
                long now = System.currentTimeMillis();
                for (int i=0; i < activeInterpolators.size(); i++) {
                    Interpolator active = (Interpolator) activeInterpolators.elementAt(i);
                        active.finish(now);
                }
                for (int i=0; i < newInterpolators.size(); i++) {
                    Interpolator candidate = (Interpolator) newInterpolators.elementAt(i);
                    candidate.initialize(now);
                    candidate.finish(now);
                }
                activeInterpolators.removeAllElements();
                newInterpolators.removeAllElements();
            }
        }
        
        fireStateChanged();
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
        return false;
    }

}
