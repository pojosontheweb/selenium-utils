
package org.monte.media;


import org.monte.media.concurrent.SequentialDispatcher;
import java.beans.*;

import javax.swing.event.*;


public abstract class AbstractPlayer
implements Player, Runnable {

    private int state = UNREALIZED;


    private int targetState = UNREALIZED;


    protected EventListenerList listenerList = new EventListenerList();


    protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


    protected SequentialDispatcher dispatcher = new SequentialDispatcher();


    public AbstractPlayer() {
    }


    @Override
    public int getState() {
        return state;
    }


    @Override
    public int getTargetState() {
        return targetState;
    }


    @Override
    public void setTargetState(final int state) {
        synchronized (this) {
            if (targetState != CLOSED) {
                targetState = state;
                AbstractPlayer.this.notifyAll();
                dispatcher.dispatch(this);
            }
        }
    }


    @Override
    public void realize() {
        switch (getState()) {
            case CLOSED :
                throw new IllegalStateException("Realize closed player.");

            case STARTED :
                throw new IllegalStateException("Realize started player.");

        }
        setTargetState(REALIZED);
    }


    public void prefetch() {
        switch (getState()) {
            case CLOSED :
                throw new IllegalStateException("Prefetch closed player.");

            case STARTED :
                throw new IllegalStateException("Prefetch started player.");

        }
        setTargetState(PREFETCHED);
    }


    public void deallocate() {
        switch (getState()) {
            case CLOSED :
                throw new IllegalStateException("Deallocate closed player.");

            case REALIZING :
                setTargetState(UNREALIZED);
                break;
            case PREFETCHING :
                setTargetState(REALIZED);
                break;
            case PREFETCHED :
                setTargetState(REALIZED);
                break;
            case STARTED :
                throw new IllegalStateException("Deallocate started player.");

        }
    }



    public void start() {
        switch (getState()) {
            case CLOSED :
                throw new IllegalStateException("Can't start closed player.");

        }
        setTargetState(STARTED);
    }


    public void stop() {
        switch (getState()) {
            case CLOSED :


                break;
            case STARTED :
                setTargetState(PREFETCHED);
                break;
        }
    }


    public void close() {
        setTargetState(CLOSED);
    }


    public void addStateListener(StateListener l) {
        listenerList.add(StateListener.class, l);
    }


    public void removeStateListener(StateListener l) {
        listenerList.remove(StateListener.class, l);
    }


    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }


    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }



    protected void fireStateChanged(int newState) {
        StateEvent stateEvent = null;
        ChangeEvent changeEvent = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -=2 ) {
            if (listeners[i] == StateListener.class) {

                if (stateEvent == null) stateEvent = new StateEvent(this, newState);
                ((StateListener)listeners[i+1]).stateChanged(stateEvent);
            }
            if (listeners[i] == ChangeListener.class) {

                if (changeEvent == null) changeEvent = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }

    protected void fireStateChanged() {
        StateEvent stateEvent = null;
        ChangeEvent changeEvent = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -=2 ) {
            if (listeners[i] == ChangeListener.class) {

                if (changeEvent == null) changeEvent = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }

    public void run() {
        while (state != targetState) {
            if (targetState > state) {
                state++;
            } else {
                state = targetState;
            }
            fireStateChanged(state);

            switch (state) {
                case CLOSED :
                    doClosed();
                    break;
                case UNREALIZED :
                    doUnrealized();
                    break;
                case REALIZING :
                    doRealizing();
                    break;
                case REALIZED :
                    doRealized();
                    break;
                case PREFETCHING :
                    doPrefetching();
                    break;
                case PREFETCHED :
                    doPrefetched();
                    break;
                case STARTED :
                    doStarted();
                    setTargetState(PREFETCHED);
                    break;
            }
        }
    }


    abstract protected void doClosed();


    abstract protected void doUnrealized();


    abstract protected void doRealizing();


    abstract protected void doRealized();


    abstract protected void doPrefetching();


    abstract protected void doPrefetched();


    abstract protected void doStarted();


    public void addChangeListener(ChangeListener listener) {
        listenerList.add(ChangeListener.class, listener);
    }


    public void removeChangeListener(ChangeListener listener) {
        listenerList.remove(ChangeListener.class, listener);
    }


    public boolean isActive() {
        return getTargetState() == STARTED;
    }

}
